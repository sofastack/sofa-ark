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
import com.alipay.sofa.ark.bootstrap.AgentClassLoader;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.exception.ArkLoaderException;
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
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

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED,
            new URL[] { classPathURL });
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);

        Assert.assertEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()),
            bizModel.getBizClassLoader().loadClass(ITest.class.getName()));
    }

    @Test
    public void testAgentClass() throws ClassNotFoundException {
        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {});
        bizModel.setDenyImportResources("").setDenyImportClasses("");
        bizManagerService.registerBiz(bizModel);
        Class clazz = bizModel.getBizClassLoader().loadClass("SampleClass");
        Assert.assertFalse(clazz.getClassLoader() instanceof AgentClassLoader);

        Assert.assertTrue(clazz.getClassLoader().getClass().getCanonicalName()
            .contains("Launcher.AppClassLoader")
                          || clazz.getClassLoader().getClass().getCanonicalName()
                              .contains("ClassLoaders.AppClassLoader"));
    }

    @Test
    public void testLoadClassFromPluginClassLoader() throws Exception {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL pluginUrl1 = this.getClass().getClassLoader().getResource("sample-ark-plugin-common-0.5.1.jar");
        URL pluginUrl2 = this.getClass().getClassLoader().getResource("sofa-ark-sample-springboot-ark-0.3.0.jar");
        URL pluginUrl3 = this.getClass().getClassLoader().getResource("aopalliance-1.0.jar");
        URL pluginUrl4 = this.getClass().getClassLoader().getResource("com.springsource.org.aopalliance-1.0.0.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED,
            new URL[] { bizUrl });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDeclaredLibraries("sample-ark-plugin-common,com.springsource.org.aopalliance");

        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportClasses("")
            .setExportPackages(ClassUtils.getPackageName(ITest.class.getName()))
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("META-INF/services/sofa-ark/com.alipay.sofa.ark.container.service.extension.spi.ServiceB, Sample_Resource_Exported_A")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setClassPath(new URL[] { pluginUrl1, pluginUrl2, pluginUrl3, pluginUrl4 })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportClasses("com.alipay.sofa.ark.sample.common.SampleClassExported,org.aopalliance.aop.Advice")
            .setExportPackages("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("Sample_Resource_Exported, META-INF/spring/service.xml, Sample_Resource_Exported_A")
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        bizManagerService.registerBiz(bizModel);

        // case 1: find class from multiple libs in plugin classloader
        Class<?> adviceClazz = bizModel.getBizClassLoader().loadClass("org.aopalliance.aop.Advice");
        Assert.assertEquals(adviceClazz.getClassLoader(), pluginB.getPluginClassLoader());

        // case 2: find class from plugin but not set provided in biz model
        Assert.assertThrows(ArkLoaderException.class, () -> bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.facade.SampleService"));

        // case 3: find class from plugin in classpath
        Class<?> itest = bizModel.getBizClassLoader().loadClass(ITest.class.getName());
        Assert.assertEquals(itest.getClassLoader(), pluginA.getPluginClassLoader());

        // case 4: find class from plugin in jar
        Class<?> sampleClassExported = bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.common.SampleClassExported");
        Assert.assertEquals(sampleClassExported.getClassLoader(), pluginB.getPluginClassLoader());

        // case 5: find class but not exported
        Assert.assertThrows(ArkLoaderException.class, () -> bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.common.SampleClassNotExported"));

        // case 6: find resource from plugin but not set provided in biz model
        Assert.assertNull(bizModel.getBizClassLoader().getResource("Sample_Resource_Not_Exported"));

        // case 7: find sofa-ark resources from plugin in biz model
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource("META-INF/spring/service.xml"));

        // case 8: find resource from plugin in classpath
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource("META-INF/services/sofa-ark/com.alipay.sofa.ark.container.service.extension.spi.ServiceB"));

        // case 9: find resource from plugin in jar
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource("Sample_Resource_Exported"));

        // case 10: find resource but not exproted
        Assert.assertNull(bizModel.getBizClassLoader().getResource("Sample_Resource_Not_Exported"));

        // case 10: find resources from plugin but not set provided in biz model
        Assert.assertFalse(bizModel.getBizClassLoader().getResources("Sample_Resource_Not_Exported").hasMoreElements());

        // case 11: find resource from plugin in classpath
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("META-INF/services/sofa-ark/com.alipay.sofa.ark.container.service.extension.spi.ServiceB").hasMoreElements());

        // case 12: find resource from plugin in jar
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("Sample_Resource_Exported").hasMoreElements());

        // case 13: find resource but not exproted
        Assert.assertFalse(bizModel.getBizClassLoader().getResources("Sample_Resource_Not_Exported").hasMoreElements());
    }

    @Test
    public void testLoadOverrideClassFromPluginClassLoader() throws Exception {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL pluginUrl1 = this.getClass().getClassLoader()
            .getResource("sample-ark-plugin-common-0.5.1.jar");
        URL pluginUrl2 = this.getClass().getClassLoader()
            .getResource("sofa-ark-sample-springboot-ark-0.3.0.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED,
            new URL[] { bizUrl });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDeclaredLibraries("sample-ark-plugin-common, sofa-ark-sample-springboot-ark");

        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL, pluginUrl1, pluginUrl2 })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportMode(PluginModel.EXPORTMODE_OVERRIDE)
            .setExportClasses("com.alipay.sofa.ark.sample.common.SampleClassExported")
            .setExportPackages("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("")
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        PluginModel pluginB = new PluginModel();
        pluginB
            .setPluginName("plugin B")
            .setClassPath(new URL[] { classPathURL, pluginUrl1, pluginUrl2 })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportMode(PluginModel.EXPORTMODE_CLASSLOADER)
            .setExportClasses("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication")
            .setExportPackages("")
            .setImportResources(StringUtils.EMPTY_STRING)
            .setExportResources("")
            .setPluginClassLoader(
                new PluginClassLoader(pluginB.getPluginName(), pluginB.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginManagerService.registerPlugin(pluginB);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassAndResourceCache();

        bizManagerService.registerBiz(bizModel);

        // case 1: find class from multiple libs in plugin classloader
        Class<?> adviceClazz1 = bizModel.getBizClassLoader().loadClass(
            "com.alipay.sofa.ark.sample.common.SampleClassExported");
        Assert.assertEquals(adviceClazz1.getClassLoader(), bizModel.getBizClassLoader());

        Class<?> adviceClazz2 = bizModel.getBizClassLoader().loadClass(
            "com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication");
        Assert.assertEquals(adviceClazz2.getClassLoader(), pluginB.getPluginClassLoader());
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

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[0]);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING)
            .setDenyImportResources(StringUtils.EMPTY_STRING)
            .setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizManagerService.registerBiz(bizModel);

        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            ITest.class.getName().replace(".", "/") + ".class"));
    }

    @Test
    public void testLoadClassFromAgentClassLoader() throws ClassNotFoundException {
        BizModel bizModel = createTestBizModel("MockBiz", "1.0.0", BizState.RESOLVED, new URL[] {});
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

        BizModel bizModel = createTestBizModel("bizA", "1.0.0", BizState.RESOLVED, new URL[] {});
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);

        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            "pluginA_export_resource1.xml"));
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            "pluginA_export_resource2.xml"));
        bizModel.setDenyImportResources("pluginA_export_resource2.xml");
        invalidClassLoaderCache(bizModel.getBizClassLoader());
        Assert.assertNull(bizModel.getBizClassLoader().getResource("pluginA_export_resource2.xml"));

        Assert.assertTrue(bizModel.getBizClassLoader().loadClass(ITest.class.getName())
            .getClassLoader() instanceof PluginClassLoader);

        bizModel.setDenyImportPackages("com.alipay.sofa.ark.container.testdata");
        invalidClassLoaderCache(bizModel.getBizClassLoader());
        Assert.assertFalse(bizModel.getBizClassLoader().loadClass(ITest.class.getName())
            .getClassLoader() instanceof PluginClassLoader);

        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportClasses(ITest.class.getCanonicalName());
        invalidClassLoaderCache(bizModel.getBizClassLoader());
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

        BizModel bizModel = createTestBizModel("bizA", "1.0.0", BizState.RESOLVED, new URL[0]);
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
        //        URLClassLoader urlClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        //        Field ucpFiled = URLClassLoader.class.getDeclaredField("ucp");
        //        ucpFiled.setAccessible(true);
        //        URLClassPath ucp = (URLClassPath) ucpFiled.get(urlClassLoader);
        //        BizClassLoader bizClassLoader = new BizClassLoader("mock:1.0", ucp.getURLs());
        ClassLoader classLoader = this.getClass().getClassLoader();
        BizClassLoader bizClassLoader = new BizClassLoader("mock:1.0",
            ClassLoaderUtils.getURLs(classLoader));
        URL url = bizClassLoader.getResource("");
        Assert.assertNotNull(url);
        Assert.assertEquals(url, this.getClass().getResource("/"));
    }

    @Test
    public void testGetJdkResource() throws IOException {
        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {});
        bizManagerService.registerBiz(bizModel);

        ClassLoader cl = bizModel.getBizClassLoader();
        //        String name = "META-INF/services/javax.script.ScriptEngineFactory";
        String name = "javax/lang/model/element/Modifier.class";

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
        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {});
        bizManagerService.registerBiz(bizModel);

        ClassLoader cl = bizModel.getBizClassLoader();
        //        String name = "META-INF/services/javax.script.ScriptEngineFactory";
        String name = "javax/lang/model/element/Modifier.class";
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

    @Test
    public void testPublicDefineClass() {
        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {});
        bizManagerService.registerBiz(bizModel);

        BizClassLoader cl = (BizClassLoader) bizModel.getBizClassLoader();
        try {
            cl.publicDefineClass("NoExistClass", new byte[] {}, null);
            Assert.fail();
        } catch (Throwable t) {
            Assert.assertTrue(t instanceof java.lang.ClassFormatError);
        }
    }

    private Cache<String, Optional<URL>> getUrlResourceCache(Object classloader)
                                                                                throws NoSuchFieldException,
                                                                                IllegalAccessException {
        Field field = AbstractClasspathClassLoader.class.getDeclaredField("urlResourceCache");
        field.setAccessible(true);
        return (Cache<String, Optional<URL>>) field.get(classloader);
    }

    private void invalidClassLoaderCache(ClassLoader classloader) {
        if (classloader instanceof AbstractClasspathClassLoader) {
            ((AbstractClasspathClassLoader) classloader).invalidAllCache();
        }
    }
}
