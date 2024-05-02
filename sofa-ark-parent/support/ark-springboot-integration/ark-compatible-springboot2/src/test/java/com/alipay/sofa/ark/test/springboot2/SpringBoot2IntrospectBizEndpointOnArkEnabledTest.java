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
package com.alipay.sofa.ark.test.springboot2;

import com.alipay.sofa.ark.support.runner.ArkJUnit4EmbedRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@RunWith(ArkJUnit4EmbedRunner.class)
public class SpringBoot2IntrospectBizEndpointOnArkEnabledTest {

    @After
    public void removeTomcatInit() {
        try {
            Field urlFactory = URL.class.getDeclaredField("factory");
            urlFactory.setAccessible(true);
            urlFactory.set(null, null);
        } catch (Throwable t) {
            // ignore
        }
    }

    //    @Test
    public void testIntrospectBizEndpoint() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("management.endpoints.web.exposure.include", "*");
        SpringApplication springApplication = new SpringApplication(EmptyConfiguration.class);
        springApplication.setDefaultProperties(properties);
        ConfigurableApplicationContext applicationContext = springApplication.run(new String[] {});
        Assert.assertTrue(applicationContext.containsBean("introspectBizEndpoint"));
        applicationContext.close();
    }

    //    @Test
    public void testDisableBizStateEndpoint() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("management.endpoint.bizState.enabled", "false");
        SpringApplication springApplication = new SpringApplication(EmptyConfiguration.class);
        springApplication.setDefaultProperties(properties);
        ConfigurableApplicationContext applicationContext = springApplication.run(new String[] {});
        Assert.assertFalse(applicationContext.containsBean("introspectBizEndpoint"));
        applicationContext.close();
    }

    @Configuration
    @EnableAutoConfiguration
    static class EmptyConfiguration {
    }
}
