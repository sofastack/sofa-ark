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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.config.apollo.ApolloConfigActivator;
import com.alipay.sofa.ark.config.zk.ZookeeperConfigActivator;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * @author zsk
 * @version $Id: ConfigBaseActivator.java, v 0.1 2023年09月28日 16:56 zsk Exp $
 */
public class ConfigBaseActivator implements PluginActivator {

    private final static ArkLogger                                     LOGGER             = ArkLoggerFactory
                                                                                              .getLogger(ConfigBaseActivator.class);

    private boolean                                                    enableConfigServer = EnvironmentUtils
                                                                                              .getProperty(
                                                                                                  CONFIG_SERVER_ENABLE,
                                                                                                  "true")
                                                                                              .equalsIgnoreCase(
                                                                                                  "true");

    private String                                                     configCenterType   = ArkConfigs
                                                                                              .getStringValue(CONFIG_SERVER_TYPE);

    private Map<ConfigTypeEnum, LazyActivatorWrapper<PluginActivator>> configTypeMap      = ImmutableMap
                                                                                              .of(ConfigTypeEnum.zookeeper,
                                                                                                  new LazyActivatorWrapper(
                                                                                                      ZookeeperConfigActivator.class),
                                                                                                  ConfigTypeEnum.apollo,
                                                                                                  new LazyActivatorWrapper(
                                                                                                      ApolloConfigActivator.class));

    protected PluginActivator getConfigActivator() {
        ConfigTypeEnum configType = ConfigTypeEnum.getByNameWithDefault(configCenterType,
            ConfigTypeEnum.zookeeper);
        return configTypeMap.get(configType).getLazyActivator();
    }

    @Override
    public void start(PluginContext context) {
        if (!enableConfigServer) {
            LOGGER.warn("config server is disabled.");
            return;
        }

        getConfigActivator().start(context);
    }

    @Override
    public void stop(PluginContext context) {
        if (!enableConfigServer) {
            return;
        }

        getConfigActivator().stop(context);
    }
}
