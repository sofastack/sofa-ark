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

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.sofa.ark.common.util.AssertUtils.isFalse;
import static com.alipay.sofa.ark.common.util.AssertUtils.isTrue;
import static com.alipay.sofa.ark.spi.model.BizState.RESOLVED;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
        assertTrue(classloaderService.isSunReflectClass("sun.reflect.GeneratedMethodAccessor100"));
    }

    @Test
    public void testIsNotSunReflect() {
        assertFalse(classloaderService.isSunReflectClass("test"));
    }

    @Test
    public void testIsArkSpiClass() {
        assertTrue(classloaderService.isArkSpiClass("com.alipay.sofa.ark.spi.service.ArkService"));
    }

    @Test
    public void testIsNotArkSpiClass() {
        assertFalse(classloaderService.isArkSpiClass("test"));
    }

    @Test
    public void testJDKClassLoader() {
        String sunToolClass = "sun.tools.attach.BsdVirtualMachine";
        ClassLoader jdkClassLoader = classloaderService.getJDKClassLoader();
        assertNotNull(jdkClassLoader);
        try {
            // only when this class can be loaded from system classloader,
            // then it should be loaded successfully from jdkClassLoader
            classloaderService.getSystemClassLoader().loadClass(sunToolClass);
            assertNotNull(jdkClassLoader.loadClass(sunToolClass));
        } catch (ClassNotFoundException e) {
            // ignore
        }
    }

    @Test
    public void testArkClassLoader() {
        ClassLoader arkClassLoader = classloaderService.getArkClassLoader();
        assertNotNull(arkClassLoader);
    }

    @Test
    public void testSystemClassLoader() {
        ClassLoader systemClassLoader = classloaderService.getSystemClassLoader();
        assertNotNull(systemClassLoader);
    }

    @Test
    public void testAgentClassLoader() throws ClassNotFoundException {
        ClassLoader agentClassLoader = classloaderService.getAgentClassLoader();
        assertNotNull(agentClassLoader);
        assertTrue(((URLClassLoader) agentClassLoader).getURLs().length == 2);
        assertNotNull(agentClassLoader.loadClass("SampleClass"));
    }

    @Test
    public void testIsDeniedImportClass() {

        Biz biz = new BizModel().setBizName("mockBiz").setBizVersion("1.0.0")
            .setDenyImportPackages("a.c, a.b.c.*, a.b.c").setDenyImportClasses("")
            .setBizState(RESOLVED);
        bizManagerService.registerBiz(biz);
        isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c"), "Exception error");

        isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c.E"),
            "Exception error");
        isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.c.e.G"),
            "Exception error");
        isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c.E"),
            "Exception error");
        isTrue(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c.e.G"),
            "Exception error");
        isFalse(classloaderService.isDeniedImportClass(biz.getIdentity(), "a.b.c"),
            "Exception error");
    }

    @Test
    public void testIsClassImport() {

        Plugin plugin = new PluginModel().setPluginName("mockPlugin").setImportClasses(null)
            .setImportPackages("a.c,a.b.c.*,a.b.c");
        pluginManagerService.registerPlugin(plugin);

        assertTrue(classloaderService.isClassInImport("mockPlugin", "a.c.e"));
        assertFalse(classloaderService.isClassInImport("mockPlugin", "a.c"));
        assertFalse(classloaderService.isClassInImport("mockPlugin", "a.c.e.f"));
        assertFalse(classloaderService.isClassInImport("mockPlugin", "a.b.c"));
        assertTrue(classloaderService.isClassInImport("mockPlugin", "a.b.c.e"));
        assertTrue(classloaderService.isClassInImport("mockPlugin", "a.b.c.e.f"));
    }

    @Test
    public void testFindExportClass() {

        PluginClassLoader pluginClassLoader = new PluginClassLoader("mockPlugin", new URL[] {});
        Plugin plugin = new PluginModel().setPluginName("mockPlugin")
            .setExportPackages("a.b.*,a.f,a.b.f").setExportClasses("a.e.f.G")
            .setPluginClassLoader(pluginClassLoader).setExportResources("");

        pluginManagerService.registerPlugin(plugin);
        classloaderService.prepareExportClassAndResourceCache();
        assertNull(classloaderService.findExportClassLoader("a.b"));
        assertTrue(pluginClassLoader.equals(classloaderService.findExportClassLoader("a.b.e.f")));
        assertTrue(pluginClassLoader.equals(classloaderService.findExportClassLoader("a.f.g")));
        assertTrue(pluginClassLoader.equals(classloaderService.findExportClassLoader("a.e.f.G")));
        assertTrue(pluginClassLoader.equals(classloaderService.findExportClassLoader("a.b.f.m")));
        assertTrue(pluginClassLoader.equals(classloaderService.findExportClassLoader("a.b.f.m.g")));
        assertNull(classloaderService.findExportClassLoader("a.f.h.m"));
        assertNull(classloaderService.findExportClassLoader("a"));
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
        assertTrue(exportPrefixResourceStems.contains("com/alipay/sofa/"));

        Set<String> exportSuffixResourceStems = plugin.getExportSuffixResourceStems();
        assertTrue(exportSuffixResourceStems.contains(".xsd"));

        Set<String> resources = plugin.getExportResources();
        assertTrue(resources.contains("xml-test.xml"));
        assertTrue(resources.contains("spring-beans.xsd"));

        plugin.getExportPrefixResourceStems().clear();
        plugin.getExportSuffixResourceStems().clear();
        plugin.getExportResources().clear();
        pluginManagerService.getPluginsInOrder().remove(plugin);
    }

    @Test
    public void testFindExportResourceClassLoadersInOrder() throws Exception {

        Field field = ClassLoaderServiceImpl.class
            .getDeclaredField("exportSuffixStemResourceAndClassLoaderMap");
        field.setAccessible(true);
        ConcurrentHashMap exportPrefixStemResourceAndClassLoaderMap = new ConcurrentHashMap<>();
        Plugin plugin = mock(Plugin.class);
        exportPrefixStemResourceAndClassLoaderMap.put("myaaa", asList(plugin));
        field.set(classloaderService, exportPrefixStemResourceAndClassLoaderMap);
        assertEquals(null, classloaderService.findExportResourceClassLoadersInOrder("myaaa").get(0));

        assertNull(classloaderService.getBizClassLoader("aaa:1.0"));
        assertNull(classloaderService.getPluginClassLoader("aaa:2.0"));
    }
}
