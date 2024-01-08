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
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static com.alipay.sofa.ark.common.util.ClassLoaderUtils.getURLs;
import static com.alipay.sofa.ark.common.util.ClassUtils.getPackageName;
import static com.alipay.sofa.ark.common.util.StringUtils.EMPTY_STRING;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.Assert.*;

/**
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginClassLoaderTest extends BaseTest {

    private URL                  classPathURL = PluginClassLoaderTest.class.getClassLoader()
                                                  .getResource("");

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassLoaderService   classloaderService;

    @Before
    public void before() {
        super.before();
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
        pluginDeployService = ArkServiceContainerHolder.getContainer().getService(
            PluginDeployService.class);
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassLoaderService.class);
    }

    @Test
    public void testExportAndImport() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(getPackageName(ITest.class.getName()))
            .setExportClasses("")
            .setExportResources(EMPTY_STRING)
            .setImportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(ITest.class.getName())
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setExportResources(EMPTY_STRING)
            .setImportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        assertEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()), pluginB
            .getPluginClassLoader().loadClass(ITest.class.getName()));
    }

    @Test
    public void testExportAndNotImport() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("com.alipay.sofa.ark")
            .setExportClasses("")
            .setExportResources(EMPTY_STRING)
            .setImportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setExportResources(EMPTY_STRING)
            .setImportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Assert.assertNotEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()),
            pluginB.getPluginClassLoader().loadClass(ITest.class.getName()));

    }

    @Test
    public void testExportResource() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses("")
            .setImportResources(EMPTY_STRING)
            .setExportResources("pluginA_export_resource1.xml,pluginA_export_resource2.xml")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[0])
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources("pluginA_export_resource1.xml")
            .setExportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        assertNotNull(pluginB.getPluginClassLoader().getResource("pluginA_export_resource1.xml"));
        Assert.assertNull(pluginB.getPluginClassLoader()
            .getResource("pluginA_export_resource2.xml"));
        Assert.assertNull(pluginB.getPluginClassLoader().getResource(
            "pluginA_not_export_resource.xml"));

    }

    @Test
    public void testMultiExportResource() throws Exception {
        String resourceName = "multi_export.xml";

        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setPriority("100")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses("")
            .setImportResources(EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        PluginModel pluginC = new PluginModel();
        pluginC
            .setPluginName("pluginC")
            .setPriority("1000")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginC.getPluginName(), pluginC.getClassPath()));

        PluginModel pluginD = new PluginModel();
        pluginD
            .setPluginName("pluginD")
            .setClassPath(new URL[0])
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(resourceName)
            .setExportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginD.getPluginName(), pluginD.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginManagerService.registerPlugin(pluginC);
        pluginManagerService.registerPlugin(pluginD);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Enumeration<URL> urlEnumeration = pluginD.getPluginClassLoader().getResources(resourceName);
        assertEquals(3, Collections.list(urlEnumeration).size());

        List<ClassLoader> classLoaders = classloaderService
            .findExportResourceClassLoadersInOrder(resourceName);
        assertEquals(3, classLoaders.size());
        assertEquals(pluginB.getPluginClassLoader(), classLoaders.get(0));
        assertEquals(pluginA.getPluginClassLoader(), classLoaders.get(1));
        assertEquals(pluginC.getPluginClassLoader(), classLoaders.get(2));
    }

    @Test
    public void testExportResourceStems() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setPriority("100")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(EMPTY_STRING)
            .setExportClasses(EMPTY_STRING)
            .setImportResources(EMPTY_STRING)
            .setExportResources("export/folderA/*,export/folderB/*")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[0])
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(EMPTY_STRING)
            .setExportClasses(EMPTY_STRING)
            .setImportResources("export/folderA/*,export/folderB/test3.xml")
            .setExportResources(EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        String testResource1 = "export/folderA/test1.xml";
        String testResource2 = "export/folderA/test2.xml";
        String testResource3 = "export/folderB/test3.xml";
        String testResource4 = "export/folderB/test4.xml";

        assertEquals(pluginA.getPluginClassLoader().getResource(testResource1), pluginB
            .getPluginClassLoader().getResource(testResource1));
        assertEquals(pluginA.getPluginClassLoader().getResource(testResource2), pluginB
            .getPluginClassLoader().getResource(testResource2));
        assertEquals(pluginA.getPluginClassLoader().getResource(testResource3), pluginB
            .getPluginClassLoader().getResource(testResource3));
        // export/folderB/test4.xml not import
        Assert.assertNull(pluginB.getPluginClassLoader().getResource(testResource4));
    }

    @Test
    public void testLoadClassFromAgentClassLoader() throws ClassNotFoundException {
        PluginModel mockPlugin = new PluginModel();
        mockPlugin
            .setPluginName("Mock plugin")
            .setClassPath(new URL[] {})
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(ITest.class.getCanonicalName())
            .setPluginClassLoader(
                new PluginClassLoader(mockPlugin.getPluginName(), mockPlugin.getClassPath()));
        pluginManagerService.registerPlugin(mockPlugin);

        PluginClassLoader pluginClassLoader = (PluginClassLoader) mockPlugin.getPluginClassLoader();
        assertNotNull(pluginClassLoader.loadClass("SampleClass", false));

        Class clazz = pluginClassLoader.loadClass(ArkClient.class.getCanonicalName());
        assertTrue(clazz.getClassLoader().equals(classloaderService.getArkClassLoader()));
    }

    @Test
    public void testGetJdkResource() throws IOException {

        PluginModel mockPlugin = new PluginModel();
        mockPlugin
            .setPluginName("Mock plugin")
            .setClassPath(new URL[] {})
            .setImportResources(EMPTY_STRING)
            .setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(ITest.class.getCanonicalName())
            .setPluginClassLoader(
                new PluginClassLoader(mockPlugin.getPluginName(), mockPlugin.getClassPath()));
        pluginManagerService.registerPlugin(mockPlugin);

        ClassLoader cl = mockPlugin.getPluginClassLoader();
        //         String name = "META-INF/services/javax.script.ScriptEngineFactory";

        String name = "javax/lang/model/element/Modifier.class";
        URL res1 = cl.getResource(name);
        assertNotNull(res1);

        URL res2 = getSystemClassLoader().getResource(name);
        assertNotNull(res2);
        assertEquals(res2, res1);

        Enumeration<URL> enu1 = cl.getResources(name);
        assertTrue(enu1.hasMoreElements());

        Enumeration<URL> enu2 = getSystemClassLoader().getResources(name);
        assertEquals(newHashSet(Collections.list(enu2)), newHashSet(Collections.list(enu1)));
    }

    @Test
    public void testSlashResource() throws Throwable {
        ClassLoader classLoader = this.getClass().getClassLoader();
        PluginClassLoader pluginClassLoader = new PluginClassLoader("pluginName",
            getURLs(classLoader));
        PluginModel mockPlugin = new PluginModel();
        mockPlugin.setPluginName("pluginName").setClassPath(new URL[] {})
            .setImportResources(EMPTY_STRING).setImportClasses(EMPTY_STRING)
            .setImportPackages(EMPTY_STRING)
            .setExportPackages(getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses(EMPTY_STRING).setPluginClassLoader(pluginClassLoader);
        pluginManagerService.registerPlugin(mockPlugin);
        URL url = pluginClassLoader.getResource("");
        assertNotNull(url);
        assertEquals(url, this.getClass().getResource("/"));
    }

    @Test(expected = ArkLoaderException.class)
    public void testLoadClassInternalWithSunClass() throws Exception {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("a", new URL[] { this
            .getClass().getResource("/") });
        assertEquals("a", pluginClassLoader.getPluginName());
        pluginClassLoader.loadClassInternal("sun.reflect.GeneratedMethodAccessor", true);
    }

    @Test(expected = ArkLoaderException.class)
    public void testLoadClassInternalWithResolve() throws Exception {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("a", new URL[] { this
            .getClass().getResource("/") });
        assertEquals("a", pluginClassLoader.getPluginName());
        pluginClassLoader.loadClassInternal("java.lang.a", true);
    }

    @Test(expected = ArkLoaderException.class)
    public void testPreLoadClassWithException() throws Exception {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("a", new URL[] { this
            .getClass().getResource("/") });
        pluginClassLoader.preLoadClass("a");
    }

    @Test(expected = ArkLoaderException.class)
    public void testPostLoadClassWithException() throws Exception {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("a", new URL[] { this
            .getClass().getResource("/") });
        pluginClassLoader.postLoadClass("a");
    }
}