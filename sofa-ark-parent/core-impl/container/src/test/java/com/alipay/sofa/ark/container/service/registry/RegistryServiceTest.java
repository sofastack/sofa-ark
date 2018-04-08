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
package com.alipay.sofa.ark.container.service.registry;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.testdata.Activator.PluginActivatorA;
import com.alipay.sofa.ark.container.testdata.Activator.PluginActivatorADup;
import com.alipay.sofa.ark.container.testdata.Activator.PluginActivatorC;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectA;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectB;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectC;
import com.alipay.sofa.ark.container.model.PluginContextImpl;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.registry.ArkContainerServiceProvider;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.registry.PluginNameServiceFilter;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.PluginClassLoader;
import com.alipay.sofa.ark.container.testdata.Activator.PluginActivatorB;
import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceProvider;
import com.alipay.sofa.ark.spi.registry.ServiceProviderType;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class RegistryServiceTest extends BaseTest {

    private RegistryService      registryService;

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassloaderService   classloaderService;

    private URL                  classPathURL    = RegistryServiceTest.class.getClassLoader()
                                                     .getResource("");

    private static final String  INTERFACE_CLASS = ITest.class.getName();

    @Before
    public void before() {
        ArkServiceContainer arkServiceContainer = new ArkServiceContainer();
        arkServiceContainer.start();
        registryService = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
        pluginDeployService = ArkServiceContainerHolder.getContainer().getService(
            PluginDeployService.class);
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassloaderService.class);
    }

    @Test
    public void testPublishService() throws Exception {
        ServiceReference<ITest> iTestServiceReference = registryService.publishService(ITest.class,
            new TestObjectA());
        Assert.assertNotNull(iTestServiceReference);
        Assert.assertEquals(TestObjectA.OUTPUT, iTestServiceReference.getService().test());
    }

    @Test
    public void testReferenceService() throws Exception {
        registryService.publishService(ITest.class, new TestObjectA());
        ServiceReference<ITest> iTestServiceReference = registryService
            .referenceService(ITest.class);
        Assert.assertNotNull(iTestServiceReference);
        Assert.assertEquals(TestObjectA.OUTPUT, iTestServiceReference.getService().test());
    }

    @Test
    public void testPublishDuplicateService() throws Exception {
        registryService.publishService(ITest.class, new TestObjectA());
        registryService.publishService(ITest.class, new TestObjectB());

        // 只有第一个服务发布成功
        Assert.assertEquals(1, registryService.referenceServices(ITest.class).size());
        Assert.assertEquals(TestObjectA.OUTPUT, registryService.referenceService(ITest.class)
            .getService().test());
    }

    @Test
    public void testPublishDuplicateServiceInPlugin() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setPriority(10)
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportIndex(new HashSet<>(Collections.singletonList(INTERFACE_CLASS)))
            .setPluginActivator(PluginActivatorADup.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginA));

        pluginManagerService.registerPlugin(pluginA);
        classloaderService.prepareExportClassCache();
        pluginDeployService.deploy();

        Assert.assertEquals(1, registryService.referenceServices(ITest.class).size());
    }

    @Test
    public void testMultipleService() throws Exception {
        // 非插件发布的服务，优先级别最低
        registryService.publishService(ITest.class, new TestObjectA());

        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setPriority(10)
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportIndex(new HashSet<>(Collections.singletonList(INTERFACE_CLASS)))
            .setPluginActivator(PluginActivatorA.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginA));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority(1)
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(INTERFACE_CLASS)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportIndex(Collections.<String> emptySet())
            .setPluginActivator(PluginActivatorB.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginB));

        PluginModel pluginc = new PluginModel();
        pluginc
            .setPluginName("plugin C")
            .setPriority(100)
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(INTERFACE_CLASS)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportIndex(Collections.<String> emptySet())
            .setPluginActivator(PluginActivatorC.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginc.getPluginName(), pluginc.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginc));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginManagerService.registerPlugin(pluginc);

        classloaderService.prepareExportClassCache();
        pluginDeployService.deploy();

        Assert.assertEquals(4, registryService.referenceServices(ITest.class).size());
        Assert.assertEquals(3, pluginA.getPluginContext().referenceServices(ITest.class).size());

        // 应该获取到优先级别比较高的服务
        Assert.assertEquals(pluginB.getPluginName(),
            pluginc.getPluginContext().referenceService(ITest.class).getServiceMetadata()
                .getServiceProvider().getServiceProviderName());

        // 通过插件名寻找服务
        Assert.assertEquals(pluginA.getPluginName(),
            pluginc.getPluginContext().referenceService(ITest.class, pluginA.getPluginName())
                .getServiceMetadata().getServiceProvider().getServiceProviderName());

    }

    @Test
    public void testFilter() {
        PluginModel pluginA = new PluginModel();
        pluginA.setPluginName("plugin A").setPriority(10);

        PluginModel pluginB = new PluginModel();
        pluginB.setPluginName("plugin B").setPriority(1);

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);

        registryService.publishService(ITest.class, new TestObjectA(), new PluginServiceProvider(
            pluginA));

        registryService.publishService(ITest.class, new TestObjectB(), new PluginServiceProvider(
            pluginB));

        registryService.publishService(ITest.class, new TestObjectC());

        Assert.assertEquals(pluginB.getPluginName(), registryService.referenceService(ITest.class)
            .getServiceMetadata().getServiceProvider().getServiceProviderName());
        Assert
            .assertEquals(
                pluginA.getPluginName(),
                registryService
                    .referenceService(ITest.class,
                        new PluginNameServiceFilter(pluginA.getPluginName())).getServiceMetadata()
                    .getServiceProvider().getServiceProviderName());
        Assert.assertNull(registryService.referenceService(ITest.class,
            new PluginNameServiceFilter("not exist")));
        Assert.assertEquals("ArkContainer",
            registryService.referenceService(ITest.class, new ServiceFilter() {
                @Override
                public boolean match(ServiceProvider serviceProvider) {
                    return ServiceProviderType.ARK_CONTAINER.equals(serviceProvider
                        .getServiceProviderType());
                }
            }).getServiceMetadata().getServiceProvider().getServiceProviderName());
    }

    @Test
    public void testContainerService() {
        registryService.publishService(ITest.class, new TestObjectA(),
            new ArkContainerServiceProvider("test1", 20000));
        registryService.publishService(ITest.class, new TestObjectB(),
            new ArkContainerServiceProvider("test2", 100));
        registryService.publishService(ITest.class, new TestObjectC());

        Assert.assertEquals(TestObjectB.OUTPUT, registryService.referenceService(ITest.class)
            .getService().test());
        Assert.assertEquals(3, registryService.referenceServices(ITest.class).size());
    }

    @Test
    public void testContainerServiceNotFoundByPlugin() {
        registryService.publishService(ITest.class, new TestObjectA());

        Assert.assertNotNull(registryService.referenceService(ITest.class));

        PluginModel pluginA = new PluginModel();
        pluginA.setPluginName("plugin A").setPriority(10)
            .setPluginContext(new PluginContextImpl(pluginA));

        pluginManagerService.registerPlugin(pluginA);

        Assert.assertNull(pluginA.getPluginContext().referenceService(ITest.class));

    }

}