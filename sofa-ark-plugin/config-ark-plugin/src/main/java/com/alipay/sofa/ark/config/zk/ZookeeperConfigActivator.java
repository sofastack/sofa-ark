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
import com.alipay.sofa.ark.config.ConfigListener;
import com.alipay.sofa.ark.config.RegistryConfig;
import com.alipay.sofa.ark.config.util.NetUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
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

    private final static ArkLogger LOGGER         = ArkLoggerFactory.getDefaultLogger();

    /**
     * Zookeeper zkClient
     */
    private CuratorFramework       zkClient;

    /**
     * Root path of zk registry resource
     */
    private String                 rootPath       = Constants.ZOOKEEPER_CONTEXT_SPLIT;

    private String                 resourcePath;

    private PathChildrenCache      pathChildrenCache;

    private ConfigListener         configListener = new ZookeeperConfigListener();

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
        if (zkClient != null && resourcePath != null) {
            try {
                zkClient.delete().forPath(resourcePath);
            } catch (Exception e) {
                // ignore
            }
        }
        if (pathChildrenCache != null) {
            try {
                pathChildrenCache.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    protected void subscribeConfig() {
        AssertUtils
            .isFalse(StringUtils.isEmpty(resourcePath), "resource path should not be empty.");
        if (pathChildrenCache == null) {
            pathChildrenCache = new PathChildrenCache(zkClient, resourcePath, true);
            try {
                pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            } catch (Exception e) {
                throw new ArkRuntimeException("Failed to register resource to zookeeper registry!",
                    e);
            }
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event)
                                                                                             throws Exception {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Receive zookeeper event: " + "type=[" + event.getType() + "]");
                    }
                    switch (event.getType()) {
                        case CHILD_ADDED:
                        case CHILD_REMOVED:
                        case CHILD_UPDATED:
                            configListener.configChanged(new String(event.getData().getData()));
                        default:
                            break;
                    }
                }
            });
        }
    }

    protected void registryResource() {
        try {
            if (resourcePath == null) {
                resourcePath = buildConfigPath();
            }
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath(resourcePath);
        } catch (KeeperException.NodeExistsException nodeExistsException) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("context path has exists in zookeeper, path=" + resourcePath);
            }
        } catch (Exception e) {
            throw new ArkRuntimeException("Failed to register resource to zookeeper registry!", e);
        }

    }

    public String buildConfigPath() {
        return rootPath + "sofa-ark" + Constants.ZOOKEEPER_CONTEXT_SPLIT
               + NetUtils.getLocalHostAddress();
    }

    /**
     * 创建认证信息
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