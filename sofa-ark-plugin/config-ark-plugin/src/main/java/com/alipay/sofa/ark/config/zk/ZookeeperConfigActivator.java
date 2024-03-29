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
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;

import java.util.*;

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

    private CuratorCache           ipCuratorCache;
    private CuratorCache           bizCuratorCache;

    @Override
    public void start(final PluginContext context) {
        if (!enableZkServer) {
            LOGGER.warn("config server is disabled.");
            return;
        }
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

        if (Objects.nonNull(ipCuratorCache)) {
            try {
                ipCuratorCache.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (Objects.nonNull(bizCuratorCache)) {
            try {
                bizCuratorCache.close();
            } catch (Exception e) {
                // ignore
            }
        }
        zkClient.close();
    }

    protected void registerEventHandler(final PluginContext context) {
        Optional<ChildData> currentData = this.bizCuratorCache.get(bizResourcePath);
        if (currentData.isPresent() && Objects.nonNull(currentData.get().getData())) {
            final String bizInitConfig = new String(currentData.get().getData());
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
                    ConfigProcessor.createConfigProcessor(context, ipConfigDeque,
                        "ip-zookeeper-config").start();
                    ConfigProcessor.createConfigProcessor(context, bizConfigDeque,
                        "app-zookeeper-config").start();
                }

                @Override
                public int getPriority() {
                    return 0;
                }
            });
        }

    }

    protected void subscribeIpConfig() {
        this.ipCuratorCache = CuratorCache.builder(zkClient, ipResourcePath).build();
        this.ipCuratorCache.listenable().addListener(new CuratorCacheListener() {
            private int version = -1;

            @Override
            public void event(Type type, ChildData oldChildData, ChildData currentChildData) {
                if (type == Type.NODE_CHANGED) {
                    if (Objects.nonNull(currentChildData)
                        && currentChildData.getStat().getVersion() > version) {
                        version = currentChildData.getStat().getVersion();
                        String configData = new String(currentChildData.getData());
                        ipConfigDeque.add(configData);
                        LOGGER.info("Receive ip config data: {}, version is {}.", configData,
                            version);
                    }
                }
            }
        });
        try {
            LOGGER.info("Subscribe ip config: {}.", ipResourcePath);
            ipCuratorCache.start();
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to subscribe ip resource path.", e);
        }
    }

    protected void unSubscribeIpConfig() {
        if (Objects.nonNull(ipCuratorCache)) {
            try {
                LOGGER.info("Un-subscribe ip config: {}.", ipResourcePath);
                ipCuratorCache.close();
            } catch (Throwable throwable) {
                LOGGER.error("Failed to un-subscribe ip resource path.");
            }
            ipCuratorCache = null;
        }
    }

    protected void subscribeBizConfig() {
        this.bizCuratorCache = CuratorCache.build(zkClient, bizResourcePath);

        this.bizCuratorCache.listenable().addListener(new CuratorCacheListener() {
            private int version = -1;

            @Override
            public void event(Type type, ChildData oldChildData, ChildData currentChildData) {
                if (type == Type.NODE_CHANGED) {
                    if (Objects.nonNull(currentChildData)
                        && currentChildData.getStat().getVersion() > version) {
                        version = currentChildData.getStat().getVersion();
                        String configData = new String(currentChildData.getData());
                        bizConfigDeque.add(configData);
                        LOGGER.info("Receive app config data: {}, version is {}.", configData,
                            version);
                    }
                }
            }
        });

        try {
            bizCuratorCache.start();
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
        return buildMasterBizRootPath().append(Constants.ZOOKEEPER_CONTEXT_SPLIT)
            .append(NetUtils.getLocalHostAddress()).toString();
    }

    public String buildMasterBizConfigPath() {
        return buildMasterBizRootPath().toString();
    }

    private StringBuilder buildMasterBizRootPath() {
        StringBuilder masterBizRootPath = new StringBuilder(rootPath);
        String masterBizName = ArkConfigs.getStringValue(Constants.MASTER_BIZ);
        AssertUtils.isFalse(StringUtils.isEmpty(masterBizName), "Master biz should be specified.");
        String configEnvironment = ArkConfigs.getStringValue(Constants.CONFIG_SERVER_ENVIRONMENT,
            "sofa-ark");
        masterBizRootPath.append(configEnvironment).append(Constants.ZOOKEEPER_CONTEXT_SPLIT)
            .append(masterBizName);
        return masterBizRootPath;
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