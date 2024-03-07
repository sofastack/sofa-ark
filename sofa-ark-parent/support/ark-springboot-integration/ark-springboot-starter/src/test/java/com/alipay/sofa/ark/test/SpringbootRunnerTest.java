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
package com.alipay.sofa.ark.test;

import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.test.springboot.facade.SampleService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.alipay.sofa.ark.api.ArkClient.getInjectionService;
import static com.alipay.sofa.ark.common.util.ClassLoaderUtils.pushContextClassLoader;
import static com.alipay.sofa.ark.spi.constant.Constants.EMBED_ENABLE;
import static com.alipay.sofa.ark.test.springboot.BaseSpringApplication.main;
import static com.alipay.sofa.ark.test.springboot.TestValueHolder.getTestValue;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.System.setProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author bingjie.lbj
 */
public class SpringbootRunnerTest {

    @Autowired
    public SampleService sampleService;

    @ArkInject
    PluginManagerService pluginManagerService;

    @ArkInject
    EventAdminService    eventAdminService;

    @Before
    public void before() {
        pushContextClassLoader(getSystemClassLoader());
        setProperty(EMBED_ENABLE, "true");
    }

    @After
    public void after() {
        setProperty(EMBED_ENABLE, "");
    }

    @Test
    public void test() {
        try {
            main(new String[]{});
            getInjectionService().inject(this);
            assertNotNull(pluginManagerService);
            assertEquals(0, getTestValue());
            eventAdminService.sendEvent(() -> "test-event-A");
            assertEquals(10, getTestValue());
            eventAdminService.sendEvent(() -> "test-event-B");
            assertEquals(20, getTestValue());
        } catch (Exception e) {
        }
    }
}
