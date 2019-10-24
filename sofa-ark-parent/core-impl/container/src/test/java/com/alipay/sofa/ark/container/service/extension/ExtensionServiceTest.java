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
package com.alipay.sofa.ark.container.service.extension;

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.container.ArkContainerTest;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.extension.spi.ServiceA;
import com.alipay.sofa.ark.container.service.extension.spi.ServiceB;
import com.alipay.sofa.ark.container.service.extension.spi.ServiceC;
import com.alipay.sofa.ark.container.service.extension.spi.ServiceD;
import com.alipay.sofa.ark.container.service.extension.spi.impl.ServiceBImpl4;
import com.alipay.sofa.ark.container.service.extension.spi.impl.ServiceBImpl3;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.extension.Extensible;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ExtensionServiceTest extends BaseTest {

    private URL  jarURL = ArkContainerTest.class.getClassLoader().getResource("test.jar");
    ArkContainer arkContainer;

    @Override
    public void before() {
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        arkContainer = (ArkContainer) ArkContainer.main(args);
    }

    @Override
    public void after() {
        arkContainer.stop();
    }

    @Test
    public void testExtensionServiceNotInstance() {
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        PluginModel pluginModel = new PluginModel().setPluginClassLoader(
            this.getClass().getClassLoader()).setPluginName("mock-plugin");
        pluginManagerService.registerPlugin(pluginModel);
        try {
            ArkServiceLoader.loadExtensionFromArkPlugin(ServiceA.class, "", "mock-plugin");
        } catch (ArkRuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains("not type of"));
        }
    }

    @Test
    public void testNotExtensibleService() {
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        PluginModel pluginModel = new PluginModel().setPluginClassLoader(
            this.getClass().getClassLoader()).setPluginName("mock-plugin");
        pluginManagerService.registerPlugin(pluginModel);
        try {
            ArkServiceLoader.loadExtensionFromArkPlugin(ServiceC.class, "", "mock-plugin");
        } catch (ArkRuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains(
                String.format("is not annotated by %s.", Extensible.class)));
        }
    }

    @Test
    public void testNoExtensionAnnotation() {
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        PluginModel pluginModel = new PluginModel().setPluginClassLoader(
            this.getClass().getClassLoader()).setPluginName("mock-plugin");
        pluginManagerService.registerPlugin(pluginModel);
        try {
            ArkServiceLoader.loadExtensionFromArkPlugin(ServiceD.class, "", "mock-plugin");
        } catch (ArkRuntimeException ex) {
            Assert.assertTrue(ex.getMessage().contains(
                String.format("is not annotated by %s.", Extension.class)));
        }
    }

    @Test
    public void testExtensionServiceLoader() {
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        PluginModel pluginModel = new PluginModel().setPluginClassLoader(
            this.getClass().getClassLoader()).setPluginName("mock-plugin");
        pluginManagerService.registerPlugin(pluginModel);
        ServiceB impl1 = ArkServiceLoader.loadExtensionFromArkPlugin(ServiceB.class, "type1",
            "mock-plugin");
        Assert.assertTrue(impl1 instanceof ServiceBImpl3);
        ServiceB impl2 = ArkServiceLoader.loadExtensionFromArkPlugin(ServiceB.class, "type2",
            "mock-plugin");
        Assert.assertTrue(impl2 instanceof ServiceBImpl4);
        ServiceB impl3 = ArkServiceLoader.loadExtensionFromArkPlugin(ServiceB.class, "type1",
            "mock-plugin");
        Assert.assertTrue(impl3 instanceof ServiceBImpl3);
        ServiceB impl4 = ArkServiceLoader.loadExtensionFromArkPlugin(ServiceB.class, "type2",
            "mock-plugin");
        Assert.assertTrue(impl4 instanceof ServiceBImpl4);
        Assert.assertFalse(impl1 == impl3);
        Assert.assertFalse(impl2 == impl4);
    }

}