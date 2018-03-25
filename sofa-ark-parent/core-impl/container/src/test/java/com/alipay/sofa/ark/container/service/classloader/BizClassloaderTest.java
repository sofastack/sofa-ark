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
import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
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
 *
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
        ArkServiceContainer arkServiceContainer = new ArkServiceContainer();
        arkServiceContainer.start();
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
            .setImportClasses(Collections.<String> emptySet())
            .setImportPackages(Collections.<String> emptySet())
            .setExportIndex(new HashSet<>(Collections.singletonList(ITest.class.getName())))
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassCache();

        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz A").setClassPath(new URL[] { classPathURL })
            .setClassLoader(new BizClassLoader(bizModel.getBizName(), bizModel.getClassPath()));

        bizManagerService.registerBiz(bizModel);

        Assert.assertEquals(pluginA.getPluginClassLoader().loadClass(ITest.class.getName()),
            bizModel.getBizClassLoader().loadClass(ITest.class.getName()));
    }

    @Test
    public void testGetPluginClassResource() throws Exception {
        PluginModel pluginA = new PluginModel();
        pluginA
            .setPluginName("plugin A")
            .setClassPath(new URL[] { classPathURL })
            .setImportClasses(Collections.<String> emptySet())
            .setImportPackages(Collections.<String> emptySet())
            .setExportIndex(new HashSet<>(Collections.singletonList(ITest.class.getName())))
            .setPluginClassLoader(
                new PluginClassLoader(pluginA.getPluginName(), pluginA.getClassPath()));

        pluginManagerService.registerPlugin(pluginA);
        pluginDeployService.deploy();
        classloaderService.prepareExportClassCache();

        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz A").setClassPath(new URL[] { classPathURL })
            .setClassLoader(new BizClassLoader(bizModel.getBizName(), bizModel.getClassPath()));

        bizManagerService.registerBiz(bizModel);

        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            ITest.class.getName().replace(".", "/") + ".class"));
    }

    @Test
    public void testLoadClassFromAgentClassLoader() throws ClassNotFoundException {
        BizModel bizModel = new BizModel();
        bizModel.setBizName("MockBiz").setClassPath(new URL[] {})
            .setClassLoader(new BizClassLoader(bizModel.getBizName(), bizModel.getClassPath()));

        bizManagerService.registerBiz(bizModel);
        BizClassLoader bizClassLoader = (BizClassLoader) bizModel.getBizClassLoader();
        Assert.assertNotNull(bizClassLoader.loadClass("SampleClass", false));

    }
}