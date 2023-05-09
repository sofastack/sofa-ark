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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.test.springboot.BaseSpringApplication;
import com.alipay.sofa.ark.test.springboot.TestValueHolder;
import com.alipay.sofa.ark.test.springboot.facade.SampleService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringBootWebfluxRunnerTest {
    @Autowired
    public SampleService sampleService;

    @ArkInject
    PluginManagerService pluginManagerService;

    @ArkInject
    EventAdminService    eventAdminService;

    @Before
    public void before() {
        ClassLoaderUtils.pushContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(Constants.EMBED_ENABLE, "true");
    }

    @After
    public void after() {
        System.setProperty(Constants.EMBED_ENABLE, "");
    }

    @Test
    public void test() {
        BaseSpringApplication.main(new String[] {});
        ArkClient.getInjectionService().inject(this);
        Assert.assertNotNull(pluginManagerService);
        Assert.assertEquals(0, TestValueHolder.getTestValue());
        eventAdminService.sendEvent(() -> "test-event-A");
        Assert.assertEquals(10, TestValueHolder.getTestValue());
        eventAdminService.sendEvent(() -> "test-event-B");
        Assert.assertEquals(20, TestValueHolder.getTestValue());
    }
}
