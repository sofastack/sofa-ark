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
package com.alipay.sofa.ark.container.service.plugin;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_EXTENSION_FORMAT;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class PluginFactoryServiceTest extends BaseTest {
    private PluginFactoryService pluginFactoryService = new PluginFactoryServiceImpl();

    @Test
    public void test() throws Throwable {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-plugin.jar");
        Plugin plugin = pluginFactoryService.createPlugin(new File(samplePlugin.getFile()));
        Assert.assertNotNull(plugin);
    }

    @Test
    public void testCreatePluginWithExtensions() throws Throwable {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-plugin.jar");
        File file = new File(samplePlugin.getFile());
        JarFile pluginFile = new JarFile(file);
        JarFileArchive jarFileArchive = new JarFileArchive(pluginFile);
        JarPluginArchive jarPluginArchive = new JarPluginArchive(jarFileArchive);

        // inject
        URL[] extensions = new URL[1];
        URL sampleBiz = cl.getResource("sample-biz.jar");
        JarFile bizFile = new JarFile(new File(sampleBiz.getFile()));
        extensions[0] = bizFile.getUrl();

        // export
        Set<String> exportPackages = new HashSet<>();
        exportPackages.add("com.alipay.test.export.*");

        ArkConfigs.putStringValue(String.format(PLUGIN_EXTENSION_FORMAT,
                "sample-ark-plugin"), "tracer-core:3.0.10");

        Plugin plugin = pluginFactoryService.createPlugin(jarPluginArchive, extensions, exportPackages);
        Assert.assertNotNull(plugin);
        Assert.assertEquals(plugin.getExportPackages().size(), 2);
        Assert.assertTrue(Arrays.asList(plugin.getClassPath()).contains(bizFile.getUrl()));
    }
}