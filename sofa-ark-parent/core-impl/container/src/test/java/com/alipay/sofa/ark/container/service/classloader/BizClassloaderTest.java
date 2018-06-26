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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author ruoshan
 * @since 0.1.0
 */
public class BizClassloaderTest extends BaseTest {

    private URL                  classPathURL = PluginClassloaderTest.class.getClassLoader()
                                                  .getResource("");

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassloaderService   classloaderService;

    private BizManagerService    bizManagerService;

    @Before
    public void before() {
        super.before();
        pluginManagerService = ArkServiceContainerHolder.getContainer().getService(
            PluginManagerService.class);
        pluginDeployService = ArkServiceContainerHolder.getContainer().getService(
            PluginDeployService.class);
        classloaderService = ArkServiceContainerHolder.getContainer().getService(
            ClassloaderService.class);
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
            .setExportIndex(new HashSet<>(Collections.singletonList(ITest.class.getName())))
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
    public void testGetPluginClassResource() {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(StringUtils.EMPTY_STRING)
            .setImportPackages(StringUtils.EMPTY_STRING)
            .setExportIndex(new HashSet<>(Collections.singletonList(ITest.class.getName())))
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
            .setExportIndex(new HashSet<>(Collections.singletonList(ITest.class.getName())))
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

}