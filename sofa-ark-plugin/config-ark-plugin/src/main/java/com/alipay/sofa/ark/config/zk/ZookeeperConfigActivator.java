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
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.config.ConfigCommand;
import com.alipay.sofa.ark.config.ConfigListener;
import com.alipay.sofa.ark.config.RegistryConfig;
import com.alipay.sofa.ark.config.util.NetUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import com.alipay.sofa.ark.spi.service.biz.BizFileGenerator;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author qilong.zql
 * @author GengZhang
 * @since 0.6.0
 */
public class ZookeeperConfigActivator implements PluginActivator {

    private final static ArkLogger LOGGER            = ArkLoggerFactory
                                                         .getLogger("com.alipay.sofa.ark.config");

    /**
     * Zookeeper zkClient
     */
    private CuratorFramework       zkClient;

    /**
     * Root path of zk registry resource
     */
    private String                 rootPath          = Constants.ZOOKEEPER_CONTEXT_SPLIT;

    private String                 ipResourcePath    = buildIpConfigPath();

    private String                 bizResourcePath   = buildMasterBizConfigPath();

    private NodeCache              ipNodeCache;

    private NodeCache              bizNodeCache;

    private ConfigListener         ipConfigListener  = new ZookeeperConfigListener.IpConfigListener();

    private ConfigListener         bizConfigListener = new ZookeeperConfigListener.BizConfigListener();

    @Override
    public void start(PluginContext context) {
        String config = ArkConfigs.getStringValue(Constants.CONFIG_SERVER_ADDRESS);
        AssertUtils.isFalse(StringUtils.isEmpty(config), "Zookeeper config should not be empty.");
        RegistryConfig registryConfig = ZookeeperConfigurator.buildConfig(config);

        // host:port/context
        String address = registryConfig.getAddress();
        AssertUtils.isFalse(StringUtils.isEmpty(address), "Zookeeper address should not be empty.");

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
            .retryPolicy(retryPolicy).defaultData("".getBytes());

        List<AuthInfo> authInfos = buildAuthInfo(registryConfig);
        if (!authInfos.isEmpty()) {
            zkClientBuilder = zkClientBuilder.aclProvider(getDefaultAclProvider()).authorization(
                authInfos);
        }

        zkClient = zkClientBuilder.build();
        zkClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("reconnect to zookeeper, re-register config resource.");
                }
                if (newState == ConnectionState.RECONNECTED) {
                    registryResource();
                }
            }
        });

        zkClient.start();
        registryResource();
        subscribeConfig();
    }

    @Override
    public void stop(PluginContext context) {
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
        if (zkClient != null) {
            try {
                zkClient.delete().forPath(ipResourcePath);
            } catch (Exception e) {
                // ignore
            }
            zkClient.close();
        }
    }

    protected void subscribeConfig() {
        ipNodeCache = new NodeCache(zkClient, ipResourcePath);
        bizNodeCache = new NodeCache(zkClient, bizResourcePath);
        ipNodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                List<ConfigCommand> commands = ipConfigListener.configUpdated(new String(
                    ipNodeCache.getCurrentData().getData()));

            }
        });
        bizNodeCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                List<ConfigCommand> commands = bizConfigListener.configUpdated(new String(
                    bizNodeCache.getCurrentData().getData()));
            }
        });

        try {
            bizNodeCache.start(true);
            ipNodeCache.start(true);
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to subscribe resource path.", e);
        }
    }

    protected void registryResource() {
        registryResource(bizResourcePath, CreateMode.PERSISTENT);
        registryResource(ipResourcePath, CreateMode.EPHEMERAL);
    }

    protected void registryResource(String path, CreateMode createMode) {
        try {
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

        //addAuth=user1:password1,user2:passwor  d2
        String addAuth = registryConfig.getParameter("addAuth");

        if (!StringUtils.isEmpty(addAuth)) {
            String[] addAuths = addAuth.split(",");
            for (String singleAuthInfo : addAuths) {
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