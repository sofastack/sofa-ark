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

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.alipay.sofa.ark.api.ArkConfigs.putStringValue;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_EXTENSION_FORMAT;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
        assertNotNull(plugin);
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
        putStringValue(format(PLUGIN_EXTENSION_FORMAT, "sample-ark-plugin"), "tracer-core:3.0.10");

        Plugin plugin = pluginFactoryService.createPlugin(jarPluginArchive, extensions,
            exportPackages);
        assertNotNull(plugin);
        assertEquals(plugin.getExportPackages().size(), 2);
        assertTrue(asList(plugin.getClassPath()).contains(bizFile.getUrl()));
    }

    @Test
    public void testCreateEmbedPlugin() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-plugin.jar");
        PluginArchive archive = new JarPluginArchive(new JarFileArchive(new File(
            samplePlugin.getFile())));
        Plugin plugin = pluginFactoryService.createEmbedPlugin(archive, this.getClass()
            .getClassLoader());
        assertNotNull(plugin);
    }

    @Test(expected = ArkRuntimeException.class)
    public void testDeploy() {
        PluginDeployServiceImpl pluginDeployServiceImpl = new PluginDeployServiceImpl();
        PluginManagerService pluginManagerService = mock(PluginManagerService.class);
        Plugin plugin = mock(Plugin.class);
        doThrow(new ArkRuntimeException("test")).when(plugin).start();
        when(pluginManagerService.getPluginsInOrder()).thenReturn(asList(plugin));
        pluginDeployServiceImpl.pluginManagerService = pluginManagerService;
        pluginDeployServiceImpl.deploy();
    }

    @Test(expected = ArkRuntimeException.class)
    public void testUndeploy() {
        PluginDeployServiceImpl pluginDeployServiceImpl = new PluginDeployServiceImpl();
        PluginManagerService pluginManagerService = mock(PluginManagerService.class);
        Plugin plugin = mock(Plugin.class);
        doThrow(new ArkRuntimeException("test")).when(plugin).stop();
        when(pluginManagerService.getPluginsInOrder()).thenReturn(asList(plugin));
        pluginDeployServiceImpl.pluginManagerService = pluginManagerService;
        pluginDeployServiceImpl.unDeploy();
    }
}
