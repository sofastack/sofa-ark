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
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import sun.misc.URLClassPath;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

/**
 * @author ruoshan
 * @since 0.1.0
 */
public class BizClassLoaderTest extends BaseTest {

    private URL                  classPathURL = PluginClassLoaderTest.class.getClassLoader()
                                                  .getResource("");

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassLoaderService   classloaderService;

    private BizManagerService    bizManagerService;

    @Before
    public void before() {
        super.before();
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
        pluginDeployService = ArkServiceContainerHolder.getContainer().getService(
            PluginDeployService.class);
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassLoaderService.class);
        bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
    }

    @Test
    public void testImport() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportClasses("")
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getName()))
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("biz A").setBizVersion("1.0.0")
            .setClassPath(new URL[] { classPathURL })
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);

        Assert.assertEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()),
            bizModel.getBizClassLoader().loadClass(ITest.class.getName()));
    }

    @Test
    public void testAgentClass() throws ClassNotFoundException {
        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("biz A").setBizVersion("1.0.0").setClassPath(new URL[] {})
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()))
            .setDenyImportResources("").setDenyImportClasses("");
        bizManagerService.registerBiz(bizModel);
        Class clazz = bizModel.getBizClassLoader().loadClass("SampleClass");
        Assert.assertFalse(clazz.getClassLoader() instanceof AgentClassLoader);
        Assert.assertTrue(clazz.getClassLoader().getClass().getCanonicalName()
            .contains("Launcher.AppClassLoader"));
    }

    @Test
    public void testGetPluginClassResource() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportPackages("")
            .setExportClasses(ITest.class.getName())
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources(StringUtils.EMPTY_STRING)
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("biz A").setBizVersion("1.0.0").setClassPath(new URL[0])
            .setDenyImportClasses(StringUtils.EMPTY_STRING)
            .setDenyImportResources(StringUtils.EMPTY_STRING)
            .setDenyImportPackages(StringUtils.EMPTY_STRING)
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));

        bizManagerService.registerBiz(bizModel);

        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            ITest.class.getName().replace(".", "/") + ".class"));
    }

    @Test
    public void testLoadClassFromAgentClassLoader() throws ClassNotFoundException {
        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("MockBiz").setBizVersion("1.0.0").setClassPath(new URL[] {})
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);
        BizClassLoader bizClassLoader = (BizClassLoader) bizModel.getBizClassLoader();
        Assert.assertNotNull(bizClassLoader.loadClass("SampleClass", false));

        Class clazz = bizClassLoader.loadClass(ArkClient.class.getCanonicalName());
        Assert.assertTrue(clazz.getClassLoader().equals(classloaderService.getArkClassLoader()));
    }

    @Test
    public void testDenyImport() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("pluginA_export_resource1.xml,pluginA_export_resource2.xml")
            .setExportClasses(ITest.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("bizA").setBizVersion("1.0.0").setClassPath(new URL[] {})
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);

        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            "pluginA_export_resource1.xml"));
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            "pluginA_export_resource2.xml"));
        bizModel.setDenyImportResources("pluginA_export_resource2.xml");
        Cache<String, Optional<URL>> urlResourceCache = getUrlResourceCache(bizModel.getBizClassLoader());
        urlResourceCache.invalidateAll();
        Assert.assertNull(bizModel.getBizClassLoader().getResource("pluginA_export_resource2.xml"));

        Assert.assertTrue(bizModel.getBizClassLoader().loadClass(ITest.class.getName())
            .getClassLoader() instanceof PluginClassLoader);

        bizModel.setDenyImportPackages("com.alipay.sofa.ark.container.testdata");
        Assert.assertFalse(bizModel.getBizClassLoader().loadClass(ITest.class.getName())
            .getClassLoader() instanceof PluginClassLoader);

        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(ITest.class.getCanonicalName());
        Assert.assertFalse(bizModel.getBizClassLoader().loadClass(ITest.class.getName())
            .getClassLoader() instanceof PluginClassLoader);

    }

    @Test
    public void testDenyImportResourceStems() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("pluginA")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("export/folderA/*,export/folderB/*")
            .setExportClasses(ITest.class.getName())
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("bizA").setBizVersion("1.0.0").setClassPath(new URL[0])
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizModel.setDenyImportResources("export/folderA/*,export/folderB/test3.xml");
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);

        String testResource1 = "export/folderA/test1.xml";
        String testResource2 = "export/folderA/test2.xml";
        String testResource3 = "export/folderB/test3.xml";
        String testResource4 = "export/folderB/test4.xml";

        Assert.assertNull(bizModel.getBizClassLoader().getResource(testResource1));
        Assert.assertNull(bizModel.getBizClassLoader().getResource(testResource2));
        Assert.assertNull(bizModel.getBizClassLoader().getResource(testResource3));
        // testResource4 not in deny-import
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(testResource4));
        Assert.assertEquals(pluginA.getPluginClassLoader().getResource(testResource4), bizModel
            .getBizClassLoader().getResource(testResource4));
    }

    @Test
    public void testSlashResource() throws Throwable {
        registerMockBiz();
        URLClassLoader urlClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        Field ucpFiled = URLClassLoader.class.getDeclaredField("ucp");
        ucpFiled.setAccessible(true);
        URLClassPath ucp = (URLClassPath) ucpFiled.get(urlClassLoader);
        BizClassLoader bizClassLoader = new BizClassLoader("mock:1.0", ucp.getURLs());
        URL url = bizClassLoader.getResource("");
        Assert.assertNotNull(url);
        Assert.assertEquals(url, this.getClass().getResource("/"));
    }

    @Test
    public void testGetJdkResource() throws IOException {
        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("biz A").setBizVersion("1.0.0").setClassPath(new URL[] {})
            .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizManagerService.registerBiz(bizModel);

        ClassLoader cl = bizModel.getBizClassLoader();
        String name = "META-INF/services/javax.script.ScriptEngineFactory";

        URL res1 = cl.getResource(name);
        Assert.assertNotNull(res1);

        URL res2 = ClassLoader.getSystemClassLoader().getResource(name);
        Assert.assertEquals(res2, res1);

        Enumeration<URL> enu1 = cl.getResources(name);
        Assert.assertTrue(enu1.hasMoreElements());

        Enumeration<URL> enu2 = ClassLoader.getSystemClassLoader().getResources(name);
        Assert.assertEquals(Sets.newHashSet(Collections.list(enu2)),
            Sets.newHashSet(Collections.list(enu1)));

    }

    @Test
    public void testCacheResource() throws NoSuchFieldException, IllegalAccessException {
        BizModel bizModel = new BizModel().setBizState(BizState.RESOLVED);
        bizModel.setBizName("biz A").setBizVersion("1.0.0").setClassPath(new URL[] {})
                .setClassLoader(new BizClassLoader(bizModel.getIdentity(), bizModel.getClassPath()));
        bizManagerService.registerBiz(bizModel);

        ClassLoader cl = bizModel.getBizClassLoader();
        String name = "META-INF/services/javax.script.ScriptEngineFactory";
        URL res1 = cl.getResource(name);
        Assert.assertNotNull(res1);
        Assert.assertNotNull(cl.getResource(name));
        Cache<String, Optional<URL>> urlResourceCache = getUrlResourceCache(cl);
        Assert.assertNotNull(urlResourceCache.getIfPresent(name));
        Assert.assertNotNull(urlResourceCache.getIfPresent(name).get());

        // not existing url
        String notExistingName = "META-INF/services/javax.script.ScriptEngineFactory/NotExisting";
        URL notExistingRes = cl.getResource(notExistingName);
        Assert.assertNull(notExistingRes);
        Assert.assertNull(cl.getResource(notExistingName));
        Assert.assertNotNull(urlResourceCache.getIfPresent(notExistingName));
        Assert.assertFalse(urlResourceCache.getIfPresent(notExistingName).isPresent());
    }

    private Cache<String, Optional<URL>> getUrlResourceCache(Object classloader) throws NoSuchFieldException, IllegalAccessException {
        Field field = AbstractClasspathClassLoader.class.getDeclaredField("urlResourceCache");
        field.setAccessible(true);
        return (Cache<String, Optional<URL>>) field.get(classloader);
    }
}