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
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.config.apollo.ApolloConfigActivator;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import org.junit.Assert;
import org.junit.Test;

import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_SERVER_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_SERVER_TYPE;

/**
 * @author zsk
 * @version $Id: ConfigBaseActivatorTest.java, v 0.1 2023年10月11日 17:42 zsk Exp $
 */
public class ConfigBaseActivatorTest {

    @Test
    public void testGetConfigActivator() {
        EnvironmentUtils.setProperty(CONFIG_SERVER_ENABLE, "true");
        ArkConfigs.putStringValue(CONFIG_SERVER_TYPE, ConfigTypeEnum.apollo.name());
        ConfigBaseActivator baseActivator = new ConfigBaseActivator();
        PluginActivator activator = baseActivator.getConfigActivator();
        Assert.assertTrue(activator instanceof ApolloConfigActivator);
    }

    @Test
    public void testFailNewZookeeperConfigurator() {
        EnvironmentUtils.setProperty(CONFIG_SERVER_ENABLE, "true");
        ArkConfigs.putStringValue(CONFIG_SERVER_TYPE, ConfigTypeEnum.zookeeper.name());
        ConfigBaseActivator baseActivator = new ConfigBaseActivator();
        Throwable throwable = null;
        try {
            baseActivator.start(null);
        } catch (Throwable t) {
            throwable = t;
        }
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable.getMessage().equals("Master biz should be specified."));
    }

}
