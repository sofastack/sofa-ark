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

import com.alipay.sofa.ark.config.zk.ZookeeperConfigurator;
import com.alipay.sofa.ark.spi.constant.Constants;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ZookeeperConfiguratorTest {

    @Test
    public void testInvalidZookeeperAddress() {
        Throwable throwable = null;
        try {
            ZookeeperConfigurator.buildConfig("xxx");
        } catch (Throwable t) {
            throwable = t;
        }
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable.getMessage().equals(
            "Zookeeper config should start with 'zookeeper://'."));
    }

    @Test
    public void testRegistryConfig() {
        RegistryConfig registryConfig = ZookeeperConfigurator
            .buildConfig("zookeeper://localhost:2181?k1=v1&k2=v2");
        Assert.assertEquals(Constants.CONFIG_PROTOCOL_ZOOKEEPER, registryConfig.getProtocol());
        Assert.assertEquals("localhost:2181", registryConfig.getAddress());
        Assert.assertEquals(2, registryConfig.getParameters().size());
        Assert.assertEquals("v1", registryConfig.getParameter("k1"));
        Assert.assertEquals("v2", registryConfig.getParameter("k2"));
    }

    @Test
    public void testZookeeperAddress() {
        String address = ZookeeperConfigurator
            .parseAddress("zookeeper://localhost:2181?k1=v1&k2=v2");
        Assert.assertEquals("localhost:2181", address);
    }

    @Test
    public void testZookeeperParameter() {
        Map<String, String> parameters = ZookeeperConfigurator
            .parseParam("zookeeper://localhost:2181?k1=v1&k2=v2");
        Assert.assertEquals(2, parameters.size());
        Assert.assertEquals("v1", parameters.get("k1"));
        Assert.assertEquals("v2", parameters.get("k2"));
    }

    @Test
    public void testInvalidZookeeperParameter() {
        Throwable throwable = null;
        try {
            ZookeeperConfigurator.parseParam("zookeeper://localhost:2181?k1");
        } catch (Throwable t) {
            throwable = t;
        }
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable.getMessage().contains("invalid format"));
    }
}