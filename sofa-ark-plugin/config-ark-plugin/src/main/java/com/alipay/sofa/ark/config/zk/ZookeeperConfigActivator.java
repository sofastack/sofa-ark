/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.config.zk;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.config.ConfigProcessor;
import com.alipay.sofa.ark.config.util.OperationTransformer;
import com.alipay.sofa.ark.config.OperationProcessor;
import com.alipay.sofa.ark.config.RegistryConfig;
import com.alipay.sofa.ark.config.util.NetUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AfterFinishDeployEvent;
import com.alipay.sofa.ark.spi.event.AfterFinishStartupEvent;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_SERVER_ENABLE;

/**
 * @author qilong.zql
 * @author GengZhang
 * @since 0.6.0
 */
public class ZookeeperConfigActivator implements PluginActivator {

    private final static ArkLogger LOGGER          = ArkLoggerFactory
                                                       .getLogger(ZookeeperConfigActivator.class);

    private boolean                enableZkServer  = EnvironmentUtils.getProperty(
                                                       CONFIG_SERVER_ENABLE, "true")
                                                       .equalsIgnoreCase("true");

    /**
     * Zookeeper zkClient
     */
    private CuratorFramework       zkClient;

    /**
     * Root path of zk registry resource
     */
    private String                 rootPath        = Constants.ZOOKEEPER_CONTEXT_SPLIT;

    private String                 ipResourcePath  = buildIpConfigPath();

    private String                 bizResourcePath = buildMasterBizConfigPath();

    private Deque<String>          ipConfigDeque   = new ArrayDeque<>(5);

    private Deque<String>          bizConfigDeque  = new ArrayDeque<>(5);

    private NodeCache              ipNodeCache;

    private NodeCache              bizNodeCache;

    @Override
    public void start(final PluginContext context) {
        LOGGER.info("start zookeeper config activator");

        String config = ArkConfigs.getStringValue(Constants.CONFIG_SERVER_ADDRESS);
        RegistryConfig registryConfig = ZookeeperConfigurator.buildConfig(config);
        String address = registryConfig.getAddress();
        int idx = address.indexOf(Constants.ZOOKEEPER_CONTEXT_SPLIT);
        if (idx != -1) {
            rootPath = address.substring(idx);
            if (!rootPath.endsWith(Constants.ZOOKEEPER_CONTEXT_SPLIT)) {
                rootPath += Constants.ZOOKEEPER_CONTEXT_SPLIT;
            }
            address = address.substring(0, idx);
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFrameworkFactory.Builder zkClientBuilder = CuratorFrameworkFactory.builder()
            .connectString(address).sessionTimeoutMs(3 * registryConfig.getConnectTimeout())
            .connectionTimeoutMs(registryConfig.getConnectTimeout()).canBeReadOnly(false)
            .retryPolicy(retryPolicy).defaultData(null);

        List<AuthInfo> authInfos = buildAuthInfo(registryConfig);
        if (!authInfos.isEmpty()) {
            zkClientBuilder = zkClientBuilder.aclProvider(getDefaultAclProvider()).authorization(
                authInfos);
        }

        zkClient = zkClientBuilder.build();
        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                LOGGER.info("Reconnect to zookeeper, re-register config resource.");
                if (newState == ConnectionState.RECONNECTED) {
                    unSubscribeIpConfig();
                    registryResource(ipResourcePath, CreateMode.EPHEMERAL);
                    subscribeIpConfig();
                }
            }
        });

        zkClient.start();
        registryResource(bizResourcePath, CreateMode.PERSISTENT);
        registryResource(ipResourcePath, CreateMode.EPHEMERAL);
        subscribeIpConfig();
        subscribeBizConfig();
        registerEventHandler(context);
    }

    @Override
    public void stop(PluginContext context) {

        if (!enableZkServer) {
            return;
        }

        if (ipNodeCache != null) {
            try {
                ipNodeCache.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (bizNodeCache != null) {
            try {
                bizNodeCache.close();
            } catch (Exception e) {
                // ignore
            }
        }
        zkClient.close();
    }

    protected void registerEventHandler(final PluginContext context) {
        final String bizInitConfig = new String(bizNodeCache.getCurrentData().getData());
        EventAdminService eventAdminService = context.referenceService(EventAdminService.class)
            .getService();
        eventAdminService.register(new EventHandler<AfterFinishDeployEvent>() {
            @Override
            public void handleEvent(AfterFinishDeployEvent event) {
                LOGGER.info("Start to process init app config: {}", bizInitConfig);
                OperationProcessor.process(OperationTransformer.transformToBizOperation(
                    bizInitConfig, context));
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });
        eventAdminService.register(new EventHandler<AfterFinishStartupEvent>() {
            @Override
            public void handleEvent(AfterFinishStartupEvent event) {
                ConfigProcessor
                    .createConfigProcessor(context, ipConfigDeque, "ip-zookeeper-config").start();
                ConfigProcessor.createConfigProcessor(context, bizConfigDeque,
                    "app-zookeeper-config").start();
            }

            @Override
            public int getPriority() {
                return 0;
            }
        });
    }

    protected void subscribeIpConfig() {
        ipNodeCache = new NodeCache(zkClient, ipResourcePath);
        ipNodeCache.getListenable().addListener(new NodeCacheListener() {
            private int version = -1;

            @Override
            public void nodeChanged() throws Exception {
                if (ipNodeCache.getCurrentData() != null
                    && ipNodeCache.getCurrentData().getStat().getVersion() > version) {
                    version = ipNodeCache.getCurrentData().getStat().getVersion();
                    String configData = new String(ipNodeCache.getCurrentData().getData());
                    ipConfigDeque.add(configData);
                    LOGGER.info("Receive ip config data: {}, version is {}.", configData, version);
                }
            }
        });
        try {
            LOGGER.info("Subscribe ip config: {}.", ipResourcePath);
            ipNodeCache.start(true);
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to subscribe ip resource path.", e);
        }
    }

    protected void unSubscribeIpConfig() {
        if (ipNodeCache != null) {
            try {
                LOGGER.info("Un-subscribe ip config: {}.", ipResourcePath);
                ipNodeCache.close();
            } catch (Throwable throwable) {
                LOGGER.error("Failed to un-subscribe ip resource path.");
            }
            ipNodeCache = null;
        }
    }

    protected void subscribeBizConfig() {
        bizNodeCache = new NodeCache(zkClient, bizResourcePath);
        bizNodeCache.getListenable().addListener(new NodeCacheListener() {
            private int version = -1;

            @Override
            public void nodeChanged() throws Exception {
                if (bizNodeCache.getCurrentData() != null
                    && bizNodeCache.getCurrentData().getStat().getVersion() > version) {
                    version = bizNodeCache.getCurrentData().getStat().getVersion();
                    String configData = new String(bizNodeCache.getCurrentData().getData());
                    bizConfigDeque.add(configData);
                    LOGGER.info("Receive app config data: {}, version is {}.", configData, version);
                }
            }
        });

        try {
            bizNodeCache.start(true);
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to subscribe resource path.", e);
        }
    }

    protected void registryResource(String path, CreateMode createMode) {
        try {
            LOGGER.info("Registry context path: {} with mode: {}.", path, createMode);
            zkClient.create().creatingParentContainersIfNeeded().withMode(createMode).forPath(path);
        } catch (KeeperException.NodeExistsException nodeExistsException) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Context path has exists in zookeeper, path=" + path);
            }
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to register resource to zookeeper registry!", e);
        }
    }

    public String buildIpConfigPath() {
        String masterBizName = ArkConfigs.getStringValue(Constants.MASTER_BIZ);
        AssertUtils.isFalse(StringUtils.isEmpty(masterBizName), "Master biz should be specified.");
        return rootPath + "sofa-ark" + Constants.ZOOKEEPER_CONTEXT_SPLIT + masterBizName
               + Constants.ZOOKEEPER_CONTEXT_SPLIT + NetUtils.getLocalHostAddress();
    }

    public String buildMasterBizConfigPath() {
        String masterBizName = ArkConfigs.getStringValue(Constants.MASTER_BIZ);
        AssertUtils.isFalse(StringUtils.isEmpty(masterBizName), "Master biz should be specified.");
        return rootPath + "sofa-ark" + Constants.ZOOKEEPER_CONTEXT_SPLIT + masterBizName;
    }

    /**
     * build auth info
     *
     * @return
     */
    private List<AuthInfo> buildAuthInfo(RegistryConfig registryConfig) {
        List<AuthInfo> info = new ArrayList<AuthInfo>();

        String scheme = registryConfig.getParameter("scheme");

        //addAuth=user1:password1,user2:password2
        String addAuth = registryConfig.getParameter("addAuth");

        if (!StringUtils.isEmpty(addAuth)) {
            String[] authList = addAuth.split(",");
            for (String singleAuthInfo : authList) {
                info.add(new AuthInfo(scheme, singleAuthInfo.getBytes()));
            }
        }
        return info;
    }

    /**
     * Get default AclProvider
     *
     * @return
     */
    private ACLProvider getDefaultAclProvider() {
        return new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }

            @Override
            public List<ACL> getAclForPath(String path) {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }
        };
    }
}