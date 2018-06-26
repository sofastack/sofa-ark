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
package com.alipay.sofa.ark.container.service.injection;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class InjectionServiceTest extends BaseTest {
    @Test
    public void test() {
        RegistryService registryService = ArkServiceContainerHolder.getContainer().getService(
            RegistryService.class);
        PluginMockService pluginMockService = new PluginMockService();
        registryService.publishService(PluginMockService.class, pluginMockService,
            new ContainerServiceProvider());
        Assert.assertNotNull(pluginMockService.getBizFactoryService());
        Assert.assertNotNull(pluginMockService.getBizManagerService());
        Assert.assertNull(pluginMockService.getClassloaderService());
    }

    public class PluginMockService {
        @ArkInject
        private BizManagerService bizManagerService;

        @ArkInject
        BizFactoryService         bizFactoryService;

        @ArkInject
        public ClassloaderService classloaderService;

        public BizManagerService getBizManagerService() {
            return bizManagerService;
        }

        public BizFactoryService getBizFactoryService() {
            return bizFactoryService;
        }

        public ClassloaderService getClassloaderService() {
            return classloaderService;
        }
    }

}