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
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getName()))
            .setExportClasses("")
            .setExportResources(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(ITest.class.getName())
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setExportResources(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Assert.assertEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()),
            pluginB.getPluginClassLoader().loadClass(ITest.class.getName()));
    }

    @Test
    public void testExportAndNotImport() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("com.alipay.sofa.ark")
            .setExportClasses("")
            .setExportResources(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setExportResources(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
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
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("pluginA_export_resource1.xml,pluginA_export_resource2.xml")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[0])
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources("pluginA_export_resource1.xml")
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Assert.assertNotNull(pluginB.getPluginClassLoader().getResource(
            "pluginA_export_resource1.xml"));
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
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        PluginModel pluginC = new PluginModel();
        pluginC
            .setPluginName("pluginC")
            .setPriority("1000")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(resourceName)
            .setPluginClassLoader(
                new PluginClassLoader(pluginC.getPluginName(), pluginC.getClassPath()));

        PluginModel pluginD = new PluginModel();
        pluginD
            .setPluginName("pluginD")
            .setClassPath(new URL[0])
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses("")
            .setImportResources(resourceName)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginD.getPluginName(), pluginD.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginManagerService.registerPlugin(pluginC);
        pluginManagerService.registerPlugin(pluginD);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        Enumeration<URL> urlEnumeration = pluginD.getPluginClassLoader().getResources(resourceName);
        Assert.assertEquals(3, Collections.list(urlEnumeration).size());

        List<ClassLoader> classLoaders = classloaderService
            .findExportResourceClassLoadersInOrder(resourceName);
        Assert.assertEquals(3, classLoaders.size());
        Assert.assertEquals(pluginB.getPluginClassLoader(), classLoaders.get(0));
        Assert.assertEquals(pluginA.getPluginClassLoader(), classLoaders.get(1));
        Assert.assertEquals(pluginC.getPluginClassLoader(), classLoaders.get(2));
    }

    @Test
    public void testExportResourceStems() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setPriority("100")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(StringUtils.EMPTY_STRING)
            .setExportClasses(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("export/folderA/*,export/folderB/*")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("pluginB")
            .setPriority("1")
            .setClassPath(new URL[0])
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(StringUtils.EMPTY_STRING)
            .setExportClasses(StringUtils.EMPTY_STRING)
            .setImportResources("export/folderA/*,export/folderB/test3.xml")
            .setExportResources(StringUtils.EMPTY_STRING)
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

        Assert.assertEquals(pluginA.getPluginClassLoader().getResource(testResource1), pluginB
            .getPluginClassLoader().getResource(testResource1));
        Assert.assertEquals(pluginA.getPluginClassLoader().getResource(testResource2), pluginB
            .getPluginClassLoader().getResource(testResource2));
        Assert.assertEquals(pluginA.getPluginClassLoader().getResource(testResource3), pluginB
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
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ITest.class.getCanonicalName())
            .setPluginClassLoader(
                new PluginClassLoader(mockPlugin.getPluginName(), mockPlugin.getClassPath()));
        pluginManagerService.registerPlugin(mockPlugin);

        PluginClassLoader pluginClassLoader = (PluginClassLoader) mockPlugin.getPluginClassLoader();
        Assert.assertNotNull(pluginClassLoader.loadClass("SampleClass", false));

        Class clazz = pluginClassLoader.loadClass(ArkClient.class.getCanonicalName());
        Assert.assertTrue(clazz.getClassLoader().equals(classloaderService.getArkClassLoader()));
    }

    @Test
    public void testGetJdkResource() throws IOException {

        PluginModel mockPlugin = new PluginModel();
        mockPlugin
            .setPluginName("Mock plugin")
            .setClassPath(new URL[] {})
            .setImportResources(StringUtils.EMPTY_STRING)
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ITest.class.getCanonicalName())
            .setPluginClassLoader(
                new PluginClassLoader(mockPlugin.getPluginName(), mockPlugin.getClassPath()));
        pluginManagerService.registerPlugin(mockPlugin);

        ClassLoader cl = mockPlugin.getPluginClassLoader();
        //         String name = "META-INF/services/javax.script.ScriptEngineFactory";

        String name = "javax/lang/model/element/Modifier.class";

        URL res1 = cl.getResource(name);
        Assert.assertNotNull(res1);

        URL res2 = ClassLoader.getSystemClassLoader().getResource(name);
        Assert.assertNotNull(res2);
        Assert.assertEquals(res2, res1);

        Enumeration<URL> enu1 = cl.getResources(name);
        Assert.assertTrue(enu1.hasMoreElements());

        Enumeration<URL> enu2 = ClassLoader.getSystemClassLoader().getResources(name);
        Assert.assertEquals(Sets.newHashSet(Collections.list(enu2)),
            Sets.newHashSet(Collections.list(enu1)));
    }

    @Test
    public void testSlashResource() throws Throwable {
        ClassLoader classLoader = this.getClass().getClassLoader();
        PluginClassLoader pluginClassLoader = new PluginClassLoader("pluginName",
            ClassLoaderUtils.getURLs(classLoader));
        PluginModel mockPlugin = new PluginModel();
        mockPlugin.setPluginName("pluginName").setClassPath(new URL[] {})
            .setImportResources(StringUtils.EMPTY_STRING)
            .setImportClasses(StringUtils.EMPTY_STRING).setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getCanonicalName()))
            .setExportClasses(StringUtils.EMPTY_STRING).setPluginClassLoader(pluginClassLoader);
        pluginManagerService.registerPlugin(mockPlugin);
        URL url = pluginClassLoader.getResource("");
        Assert.assertNotNull(url);
        Assert.assertEquals(url, this.getClass().getResource("/"));
    }

}