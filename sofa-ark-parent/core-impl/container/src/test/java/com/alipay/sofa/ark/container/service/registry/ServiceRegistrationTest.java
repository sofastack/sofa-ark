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

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.PluginContextImpl;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.registry.DefaultServiceFilter;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.registry.ServiceMetadataImpl;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.PluginClassLoader;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.testdata.activator.PluginActivatorA;
import com.alipay.sofa.ark.container.testdata.activator.PluginActivatorADup;
import com.alipay.sofa.ark.container.testdata.activator.PluginActivatorB;
import com.alipay.sofa.ark.container.testdata.activator.PluginActivatorC;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectA;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectB;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectC;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceProvider;
import com.alipay.sofa.ark.spi.registry.ServiceProviderType;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author ruoshan
 * @since 0.1.0
 */
public class ServiceRegistrationTest extends BaseTest {

    private RegistryService      registryService;

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassLoaderService   classloaderService;

    private URL                  classPathURL    = ServiceRegistrationTest.class.getClassLoader()
                                                     .getResource("");

    private static final String  INTERFACE_CLASS = ITest.class.getName();

    @Before
    public void before() {
        super.before();
        registryService = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
        pluginDeployService = ArkServiceContainerHolder.getContainer().getService(
            PluginDeployService.class);
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassLoaderService.class);
    }

    @After
    @SuppressWarnings("unchecked")
    public void after() {
        registryService.unPublishServices(new DefaultServiceFilter()
            .setServiceInterface(ITest.class));
        super.after();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishService() {
        ServiceReference<ITest> iTestServiceReference = registryService.publishService(ITest.class,
            new TestObjectA(), new ContainerServiceProvider());
        Assert.assertNotNull(iTestServiceReference);
        assertEquals(TestObjectA.OUTPUT, iTestServiceReference.getService().test());

        int c = registryService.unPublishServices(new DefaultServiceFilter().setServiceInterface(
            ITest.class).setProviderType(ServiceProviderType.ARK_CONTAINER));
        assertTrue(c == 1);

        iTestServiceReference = registryService.referenceService(ITest.class);
        Assert.assertNull(iTestServiceReference);
    }

    @Test
    public void testReferenceService() {
        registryService.publishService(ITest.class, new TestObjectA(),
            new ContainerServiceProvider());
        ServiceReference<ITest> iTestServiceReference = registryService
            .referenceService(ITest.class);
        Assert.assertNotNull(iTestServiceReference);
        assertEquals(TestObjectA.OUTPUT, iTestServiceReference.getService().test());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testPublishDuplicateService() {
        registryService.publishService(ITest.class, new TestObjectA(),
            new ContainerServiceProvider());
        registryService.publishService(ITest.class, new TestObjectB(),
            new ContainerServiceProvider());

        // 只有第一个服务发布成功
        assertEquals(1, registryService.referenceServices(ITest.class).size());
        assertEquals(TestObjectA.OUTPUT, registryService.referenceService(ITest.class).getService()
            .test());

        registryService.unPublishServices(new DefaultServiceFilter()
            .setServiceInterface(ITest.class));

        assertEquals(0, registryService.referenceServices(ITest.class).size());

        registryService.publishService(ITest.class, new TestObjectA(), "testA",
            new ContainerServiceProvider());
        registryService.publishService(ITest.class, new TestObjectB(), "testB",
            new ContainerServiceProvider());

        assertEquals(
            2,
            registryService.referenceServices(
                new DefaultServiceFilter().setServiceInterface(ITest.class)).size());
        assertEquals(TestObjectA.OUTPUT, registryService.referenceService(ITest.class, "testA")
            .getService().test());
        assertEquals(TestObjectB.OUTPUT, registryService.referenceService(ITest.class, "testB")
            .getService().test());

        int c = registryService.unPublishServices(new DefaultServiceFilter().setUniqueId("testA"));
        assertTrue(c == 1);

        c = registryService.unPublishServices(new DefaultServiceFilter().setProviderType(
            ServiceProviderType.ARK_CONTAINER).setServiceInterface(ITest.class));
        assertTrue(c == 1);

        assertEquals(0, registryService.referenceServices(ITest.class).size());
    }

    @Test
    public void testPublishDuplicateServiceInPlugin() throws Exception {

        PluginModel pluginA = new PluginModel();
        PluginContextImpl pluginContext = new PluginContextImpl(pluginA);
        pluginA
            .setPluginName("plugin A")
            .setPriority("10")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(INTERFACE_CLASS))
            .setExportClasses(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginActivator(PluginActivatorADup.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginA));

        pluginManagerService.registerPlugin(pluginA);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        assertEquals(
            1,
            registryService.referenceServices(
                pluginA.getPluginClassLoader().loadClass(ITest.class.getCanonicalName())).size());

        int c = registryService.unPublishServices(new DefaultServiceFilter()
            .setProviderType(ServiceProviderType.ARK_PLUGIN));
        assertEquals(1, c);

        assertEquals(pluginA, pluginContext.getPlugin());
        assertEquals(null, pluginContext.getPlugin("notexists"));
        assertEquals(PluginClassLoader.class, pluginContext.getClassLoader().getClass());
        assertEquals(newHashSet("plugin A"), pluginContext.getPluginNames());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleService() throws Exception {
        // 非插件发布的服务，优先级别最低
        registryService.publishService(ITest.class, new TestObjectA(),
            new ContainerServiceProvider());

        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setPriority("10")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(INTERFACE_CLASS))
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginActivator(PluginActivatorA.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginA));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(INTERFACE_CLASS)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginActivator(PluginActivatorB.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginB));

        PluginModel pluginC = new PluginModel();
        pluginC
            .setPluginName("plugin C")
            .setPriority("100")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(INTERFACE_CLASS)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginActivator(PluginActivatorC.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginC.getPluginName(), pluginC.getClassPath()))
            .setPluginContext(new PluginContextImpl(pluginC));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginManagerService.registerPlugin(pluginC);

        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Class iTest = pluginA.getPluginClassLoader().loadClass(ITest.class.getCanonicalName());
        assertEquals(
            3,
            pluginA.getPluginContext()
                .referenceServices(new DefaultServiceFilter().setServiceInterface(iTest)).size());

        // 应该获取到优先级别比较高的服务
        ServiceReference reference = pluginC.getPluginContext().referenceService(iTest);
        PluginServiceProvider provider = (PluginServiceProvider) reference.getServiceMetadata()
            .getServiceProvider();
        assertEquals(pluginB.getPluginName(), provider.getPluginName());

        List<ServiceReference> references = pluginC.getPluginContext().referenceServices(
            new DefaultServiceFilter().setServiceInterface(iTest));

        provider = (PluginServiceProvider) references.get(0).getServiceMetadata()
            .getServiceProvider();
        assertEquals(pluginB.getPluginName(), provider.getPluginName());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testFilter() {
        final PluginModel pluginA = new PluginModel();
        pluginA.setPluginName("plugin A").setPriority("10");

        PluginModel pluginB = new PluginModel();
        pluginB.setPluginName("plugin B").setPriority("1");

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);

        registryService.publishService(ITest.class, new TestObjectA(), new PluginServiceProvider(
            pluginA));

        registryService.publishService(ITest.class, new TestObjectB(), new PluginServiceProvider(
            pluginB));

        registryService.publishService(ITest.class, new TestObjectC(),
            new ContainerServiceProvider());

        List<ServiceReference> references = registryService
            .referenceServices(new DefaultServiceFilter().setServiceInterface(ITest.class)
                .setProviderType(ServiceProviderType.ARK_PLUGIN));
        assertTrue(2 == references.size());

        PluginServiceProvider provider = (PluginServiceProvider) references.get(0)
            .getServiceMetadata().getServiceProvider();
        assertEquals(pluginB.getPluginName(), provider.getPluginName());

        references = registryService.referenceServices(new ServiceFilter() {
            @Override
            public boolean match(ServiceReference serviceReference) {
                ServiceProvider serviceProvider = serviceReference.getServiceMetadata()
                    .getServiceProvider();
                if (serviceProvider instanceof PluginServiceProvider) {
                    if (((PluginServiceProvider) serviceProvider).getPluginName().equals(
                        pluginA.getPluginName())) {
                        return true;
                    }
                }
                return false;
            }
        });
        assertTrue(1 == references.size());
        provider = (PluginServiceProvider) references.get(0).getServiceMetadata()
            .getServiceProvider();
        assertEquals(pluginA.getPluginName(), provider.getPluginName());

        references = registryService.referenceServices(new DefaultServiceFilter()
            .setServiceInterface(ITest.class));
        assertTrue(3 == references.size());

        references = registryService.referenceServices(new DefaultServiceFilter().setProviderType(
            ServiceProviderType.ARK_CONTAINER).setServiceInterface(ITest.class));
        assertTrue(1 == references.size());

        assertEquals("TestObject C", ((TestObjectC) references.get(0).getService()).test());
    }

    @Test
    public void testContainerService() {
        registryService.publishService(ITest.class, new TestObjectA(),
            new ContainerServiceProvider(20000));
        registryService.publishService(ITest.class, new TestObjectB(),
            new ContainerServiceProvider(100));
        registryService.publishService(ITest.class, new TestObjectC(),
            new ContainerServiceProvider(200));

        assertEquals(TestObjectB.OUTPUT, registryService.referenceService(ITest.class).getService()
            .test());
        assertEquals(3, registryService.referenceServices(ITest.class).size());
    }

    @Test
    public void testEqualsHashCode() {

        ContainerServiceProvider containerServiceProvider = new ContainerServiceProvider();
        containerServiceProvider.hashCode();
        assertEquals(containerServiceProvider, containerServiceProvider);

        ServiceMetadataImpl serviceMetadataImpl = new ServiceMetadataImpl(this.getClass(), "a",
            containerServiceProvider);
        serviceMetadataImpl.hashCode();
        assertEquals(serviceMetadataImpl, serviceMetadataImpl);
        assertFalse(serviceMetadataImpl.equals(null));

        PluginServiceProvider pluginServiceProvider = new PluginServiceProvider(mock(Plugin.class));
        pluginServiceProvider.hashCode();
    }
}
