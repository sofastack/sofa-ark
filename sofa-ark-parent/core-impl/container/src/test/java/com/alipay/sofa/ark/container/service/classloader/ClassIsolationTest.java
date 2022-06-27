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
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ClassIsolationTest extends BaseTest {

    private PluginManagerService pluginManagerService;

    private PluginDeployService  pluginDeployService;

    private ClassLoaderService   classloaderService;

    private BizManagerService    bizManagerService;

    private BizFactoryService    bizFactoryService;

    private PluginFactoryService pluginFactoryService;

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
        bizFactoryService = ArkServiceContainerHolder.getContainer().getService(
            BizFactoryService.class);
        pluginFactoryService = ArkServiceContainerHolder.getContainer().getService(
            PluginFactoryService.class);

    }

    @Test
    public void testClassIsolation() throws IOException, ClassNotFoundException {
        System.setProperty(EMBED_ENABLE, "true");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        ArkConfigs.putStringValue(Constants.MASTER_BIZ, "master-biz");
        Biz masterBiz = bizFactoryService.createEmbedMasterBiz(cl);

        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL pluginUrl0 = this.getClass().getClassLoader().getResource("pojo-ark-plugin-1.0.0.jar");
        URL pluginUrl1 = this.getClass().getClassLoader()
            .getResource("hessian3-ark-plugin-1.0.0.jar");
        URL pluginUrl2 = this.getClass().getClassLoader()
            .getResource("hessian4-ark-plugin-1.0.0.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {
                bizUrl, pluginUrl1, pluginUrl2 });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDeclaredLibraries("pojo-ark-plugin,hessian3-ark-plugin,hessian4-ark-plugin");

        bizManagerService.registerBiz(bizModel);
        bizManagerService.registerBiz(masterBiz);

        ArkClient.setMasterBiz(masterBiz);

        PluginArchive archive0 = new JarPluginArchive(new JarFileArchive(new File(
            pluginUrl0.getFile())));
        PluginArchive archive1 = new JarPluginArchive(new JarFileArchive(new File(
            pluginUrl1.getFile())));
        PluginArchive archive2 = new JarPluginArchive(new JarFileArchive(new File(
            pluginUrl2.getFile())));

        Attributes manifestMainAttributes = new Attributes();
        manifestMainAttributes.putValue(PLUGIN_NAME_ATTRIBUTE, "web-ark-plugin");
        manifestMainAttributes.putValue(GROUP_ID_ATTRIBUTE, "com.alipay.sofa");
        manifestMainAttributes.putValue(ARTIFACT_ID_ATTRIBUTE, "web-ark-plugin");
        manifestMainAttributes.putValue(PLUGIN_VERSION_ATTRIBUTE, "mock-version");
        manifestMainAttributes.putValue(PRIORITY_ATTRIBUTE, "100");
        manifestMainAttributes.putValue(ACTIVATOR_ATTRIBUTE, "");
        manifestMainAttributes.putValue(EXPORT_CLASSES_ATTRIBUTE, "");
        manifestMainAttributes.putValue(EXPORT_PACKAGES_ATTRIBUTE, "javax.*,org.apache.*");
        manifestMainAttributes.putValue(EXPORT_RESOURCES_ATTRIBUTE, "");
        manifestMainAttributes.putValue(IMPORT_CLASSES_ATTRIBUTE, "");
        manifestMainAttributes.putValue(IMPORT_PACKAGES_ATTRIBUTE, "");
        manifestMainAttributes.putValue(IMPORT_RESOURCES_ATTRIBUTE, "");

        Manifest manifest = Mockito.mock(Manifest.class);
        when(manifest.getMainAttributes()).thenReturn(manifestMainAttributes);
        PluginArchive basePluginArchive = Mockito.mock(PluginArchive.class);
        when(basePluginArchive.getManifest()).thenReturn(manifest);
        when(basePluginArchive.isEntryExist(any(Archive.EntryFilter.class))).thenReturn(true);

        System.setProperty(PLUGIN_EXPORT_CLASS_ENABLE, "true");
        System.setProperty(PLUGIN_CLASSLOADER_ENABLE, "true");
        Plugin plugin0 = pluginFactoryService.createEmbedPlugin(archive0, cl);
        Plugin plugin1 = pluginFactoryService.createEmbedPlugin(archive1, cl);
        Plugin plugin2 = pluginFactoryService.createEmbedPlugin(archive2, cl);
        Plugin basePlugin = pluginFactoryService.createEmbedPlugin(basePluginArchive, cl);

        pluginManagerService.registerPlugin(plugin0);
        pluginManagerService.registerPlugin(plugin1);
        pluginManagerService.registerPlugin(plugin2);
        pluginManagerService.registerPlugin(basePlugin);
        classloaderService.prepareExportClassAndResourceCache();
        pluginDeployService.deploy();

        BizClassLoader bizClassLoader = (BizClassLoader) bizModel.getBizClassLoader();
        Assert.assertEquals(bizClassLoader.loadClass("com.alipay.sofa.demo.pojo.SamplePoJo")
            .getClassLoader(), plugin0.getPluginClassLoader());
        Assert.assertEquals(
            bizClassLoader.loadClass("com.alipay.sofa.demo.hessian3.Hessian3Service")
                .getClassLoader(), plugin1.getPluginClassLoader());
        Assert.assertEquals(
            bizClassLoader.loadClass("com.alipay.sofa.demo.hessian4.Hessian4Service")
                .getClassLoader(), plugin2.getPluginClassLoader());

        Assert.assertEquals(plugin0.getPluginClassLoader().getClass(), PluginClassLoader.class);
        Assert.assertEquals(plugin1.getPluginClassLoader().getClass(), PluginClassLoader.class);
        Assert.assertEquals(plugin2.getPluginClassLoader().getClass(), PluginClassLoader.class);
        Assert.assertEquals(basePlugin.getPluginClassLoader().getClass(), cl.getClass());
    }
}
