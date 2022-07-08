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

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClassLoaderServiceTest extends BaseTest {

    private ClassLoaderService   classloaderService;
    private BizManagerService    bizManagerService;
    private PluginManagerService pluginManagerService;

    @Before
    public void before() {
        super.before();
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassLoaderService.class);
        bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
    }

    @Test
    public void testIsSunReflect() {
        Assert.assertTrue(classloaderService
            .isSunReflectClass("sun.reflect.GeneratedMethodAccessor100"));
    }

    @Test
    public void testIsNotSunReflect() {
        Assert.assertFalse(classloaderService.isSunReflectClass("test"));
    }

    @Test
    public void testIsArkSpiClass() {
        Assert.assertTrue(classloaderService
            .isArkSpiClass("com.alipay.sofa.ark.spi.service.ArkService"));
    }

    @Test
    public void testIsNotArkSpiClass() {
        Assert.assertFalse(classloaderService.isArkSpiClass("test"));
    }

    @Test
    public void testJDKClassLoader() {
        String sunToolClass = "sun.tools.attach.BsdVirtualMachine";
        ClassLoader jdkClassLoader = classloaderService.getJDKClassLoader();
        Assert.assertNotNull(jdkClassLoader);
        try {
            // only when this class can be loaded from system classloader,
            // then it should be loaded successfully from jdkClassLoader
            classloaderService.getSystemClassLoader().loadClass(sunToolClass);
            Assert.assertNotNull(jdkClassLoader.loadClass(sunToolClass));
        } catch (ClassNotFoundException e) {
            // ignore
        }

    }

    @Test
    public void testArkClassLoader() {
        ClassLoader arkClassLoader = classloaderService.getArkClassLoader();
        Assert.assertNotNull(arkClassLoader);
    }

    @Test
    public void testSystemClassLoader() {
        ClassLoader systemClassLoader = classloaderService.getSystemClassLoader();
        Assert.assertNotNull(systemClassLoader);
    }

    @Test
    public void testAgentClassLoader() throws ClassNotFoundException {
        ClassLoader agentClassLoader = classloaderService.getAgentClassLoader();
        Assert.assertNotNull(agentClassLoader);
        Assert.assertTrue(((URLClassLoader) agentClassLoader).getURLs().length == 2);
        Assert.assertNotNull(agentClassLoader.loadClass("SampleClass"));
    }

    @Test
    public void testIsDeniedImportClass() {
        Biz biz = new BizModel().setBizName("mockBiz").setBizVersion("1.0.0")
            .setDenyImportPackages("a.c, a.b.c.*, a.b.c").setDenyImportClasses("")
            .setBizState(BizState.RESOLVED);
        bizManagerService.registerBiz(biz);
        AssertUtils.isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c"),
            "Exception error");

        AssertUtils.isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c.E"),
            "Exception error");
        AssertUtils.isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c.e.G"),
            "Exception error");

        AssertUtils.isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c.E"),
            "Exception error");
        AssertUtils.isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c.e.G"),
            "Exception error");
        AssertUtils.isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c"),
            "Exception error");
    }

    @Test
    public void testIsClassImport() {
        Plugin plugin = new PluginModel().setPluginName("mockPlugin").setImportClasses(null)
            .setImportPackages("a.c,a.b.c.*,a.b.c");
        pluginManagerService.registerPlugin(plugin);

        Assert.assertTrue(classloaderService.isClassInImport("mockPlugin", "a.c.e"));
        Assert.assertFalse(classloaderService.isClassInImport("mockPlugin", "a.c"));
        Assert.assertFalse(classloaderService.isClassInImport("mockPlugin", "a.c.e.f"));

        Assert.assertFalse(classloaderService.isClassInImport("mockPlugin", "a.b.c"));
        Assert.assertTrue(classloaderService.isClassInImport("mockPlugin", "a.b.c.e"));
        Assert.assertTrue(classloaderService.isClassInImport("mockPlugin", "a.b.c.e.f"));
    }

    @Test
    public void testFindExportClass() {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("mockPlugin", new URL[] {});
        Plugin plugin = new PluginModel().setPluginName("mockPlugin")
            .setExportPackages("a.b.*,a.f,a.b.f").setExportClasses("a.e.f.G")
            .setPluginClassLoader(pluginClassLoader).setExportResources("");
        pluginManagerService.registerPlugin(plugin);
        classloaderService.prepareExportClassAndResourceCache();
        Assert.assertNull(classloaderService.findExportClassLoader("a.b"));
        Assert.assertTrue(pluginClassLoader.equals(classloaderService
            .findExportClassLoader("a.b.e.f")));
        Assert.assertTrue(pluginClassLoader.equals(classloaderService
            .findExportClassLoader("a.f.g")));
        Assert.assertTrue(pluginClassLoader.equals(classloaderService
            .findExportClassLoader("a.e.f.G")));
        Assert.assertTrue(pluginClassLoader.equals(classloaderService
            .findExportClassLoader("a.b.f.m")));
        Assert.assertTrue(pluginClassLoader.equals(classloaderService
            .findExportClassLoader("a.b.f.m.g")));
        Assert.assertNull(classloaderService.findExportClassLoader("a.f.h.m"));
        Assert.assertNull(classloaderService.findExportClassLoader("a"));
        pluginManagerService.getPluginsInOrder().remove(plugin);
    }

    @Test
    public void testFindExportResources() {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("mockPlugin", new URL[] {});
        String exportResources = "spring-beans.xsd,*.xsd,com/alipay/sofa/*,xml-test.xml";
        Plugin plugin = new PluginModel().setPluginName("mockPlugin").setExportPackages("")
            .setExportClasses("").setPluginClassLoader(pluginClassLoader)
            .setExportResources(exportResources);
        pluginManagerService.registerPlugin(plugin);
        classloaderService.prepareExportClassAndResourceCache();

        Set<String> exportPrefixResourceStems = plugin.getExportPrefixResourceStems();
        Assert.assertTrue(exportPrefixResourceStems.contains("com/alipay/sofa/"));

        Set<String> exportSuffixResourceStems = plugin.getExportSuffixResourceStems();
        Assert.assertTrue(exportSuffixResourceStems.contains(".xsd"));

        Set<String> resources = plugin.getExportResources();
        Assert.assertTrue(resources.contains("xml-test.xml"));
        Assert.assertTrue(resources.contains("spring-beans.xsd"));

        plugin.getExportPrefixResourceStems().clear();
        plugin.getExportSuffixResourceStems().clear();
        plugin.getExportResources().clear();
        pluginManagerService.getPluginsInOrder().remove(plugin);
    }
}