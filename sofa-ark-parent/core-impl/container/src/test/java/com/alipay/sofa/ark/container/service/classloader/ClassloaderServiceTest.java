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
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URLClassLoader;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClassloaderServiceTest extends BaseTest {

    private ClassloaderService   classloaderService;
    private BizManagerService    bizManagerService;
    private PluginManagerService pluginManagerService;
    private ArkServiceContainer  arkServiceContainer = new ArkServiceContainer();

    @Before
    public void before() {
        arkServiceContainer.start();
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassloaderService.class);
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
    public void testJDKClassloader() throws Exception {
        String sunToolClass = "sun.tools.attach.BsdVirtualMachine";
        ClassLoader jdkClassloader = classloaderService.getJDKClassloader();
        Assert.assertNotNull(jdkClassloader);
        try {
            // only when this class can be loaded from system classloader,
            // then it should be loaded successfully from jdkClassloader
            classloaderService.getSystemClassloader().loadClass(sunToolClass);
            Assert.assertNotNull(jdkClassloader.loadClass(sunToolClass));
        } catch (ClassNotFoundException e) {
            // ignore
        }

    }

    @Test
    public void testArkClassloader() {
        ClassLoader arkClassloader = classloaderService.getArkClassloader();
        Assert.assertNotNull(arkClassloader);
    }

    @Test
    public void testSystemClassloader() {
        ClassLoader systemClassloader = classloaderService.getSystemClassloader();
        Assert.assertNotNull(systemClassloader);
    }

    @Test
    public void testAgentClassloader() throws ClassNotFoundException {
        ClassLoader agentClassLoader = classloaderService.getAgentClassloader();
        Assert.assertNotNull(agentClassLoader);
        Assert.assertTrue(((URLClassLoader) agentClassLoader).getURLs().length == 2);
        Assert.assertNotNull(agentClassLoader.loadClass("SampleClass"));
    }

    @Test
    public void testIsDeniedImportClass() {
        Biz biz = new BizModel().setBizName("mockBiz").setDenyImportPackages("a.c, a.b.c.*, a.b.c")
            .setDenyImportClasses("");
        bizManagerService.registerBiz(biz);
        AssertUtils.isFalse(classloaderService.isDeniedImportClass("mockBiz", "a.c"),
            "Exception error");

        AssertUtils.isTrue(classloaderService.isDeniedImportClass("mockBiz", "a.c.E"),
            "Exception error");
        AssertUtils.isFalse(classloaderService.isDeniedImportClass("mockBiz", "a.c.e.G"),
            "Exception error");

        AssertUtils.isTrue(classloaderService.isDeniedImportClass("mockBiz", "a.b.c.E"),
            "Exception error");
        AssertUtils.isTrue(classloaderService.isDeniedImportClass("mockBiz", "a.b.c.e.G"),
            "Exception error");
        AssertUtils.isFalse(classloaderService.isDeniedImportClass("mockBiz", "a.b.c"),
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

    @After
    public void after() {
        arkServiceContainer.stop();
    }

}