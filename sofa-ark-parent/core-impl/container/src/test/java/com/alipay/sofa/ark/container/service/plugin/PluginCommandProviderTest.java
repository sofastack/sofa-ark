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

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.plugin.PluginCommandProvider.PluginCommand;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class PluginCommandProviderTest {

    @Test
    public void testPluginCommandFormat() {

        PluginCommandProvider pluginCommandProvider = new PluginCommandProvider();
        assertFalse(pluginCommandProvider.validate(" plugin "));
        assertTrue(pluginCommandProvider.validate(" plugin -h "));
        assertTrue(pluginCommandProvider.validate(" plugin -m pluginA "));
        assertTrue(pluginCommandProvider.validate(" plugin -s pluginB pluginA "));
        assertTrue(pluginCommandProvider.validate(" plugin -d plugin* "));
        assertTrue(pluginCommandProvider.validate(" plugin -m -d -s plugin* "));
        assertTrue(pluginCommandProvider.validate(" plugin -msd pluginA "));

        assertFalse(pluginCommandProvider.validate(" plu"));
        assertFalse(pluginCommandProvider.validate(" plugin -h pluginA "));
        assertFalse(pluginCommandProvider.validate(" plugin -hm pluginA "));
        assertFalse(pluginCommandProvider.validate(" plugin -mb pluginA "));
        assertFalse(pluginCommandProvider.validate(" plugin -m -b pluginA "));
        assertFalse(pluginCommandProvider.validate(" plugin -m  "));
    }

    @Test
    public void testPluginCommandProcess() {

        URL[] urls = ClassLoaderUtils.getURLs(this.getClass().getClassLoader());
        final Plugin pluginA = new PluginModel().setPluginName("pluginA")
            .setImportClasses("com.google.common.io.AppendableWriter")
            .setImportPackages(" com.google.common.cache, com.google.common.base ")
            .setImportResources(" import_resource1").setExportPackages("com.alipay.sofa.*")
            .setExportResources(" export_resource1, export_resource2")
            .setPluginActivator("com.alipay.sofa.pluginA.Activator").setClassPath(urls)
            .setGroupId("com.alipay.sofa").setArtifactId("ark-mock-pluginA").setVersion("1.0.0");

        final Plugin pluginB = new PluginModel().setPluginName("pluginB")
            .setImportClasses("com.google.common.io.AppendableWriter")
            .setImportPackages(" com.google.common.cache, com.google.common.base ")
            .setImportResources(" import_resource1").setExportPackages("com.alipay.sofa.*")
            .setExportResources(" export_resource1, export_resource2")
            .setPluginActivator("com.alipay.sofa.pluginA.Activator").setClassPath(urls)
            .setGroupId("com.alipay.sofa").setArtifactId("ark-mock-pluginB").setVersion("1.0.0");

        final Set<String> set = new HashSet<>();
        set.add("pluginA");
        set.add("pluginB");

        PluginManagerService pluginManagerService = mock(PluginManagerService.class);
        when(pluginManagerService.getAllPluginNames()).thenReturn(set);
        when(pluginManagerService.getPluginByName("pluginA")).thenReturn(pluginA);
        when(pluginManagerService.getPluginByName("pluginB")).thenReturn(pluginB);

        PluginCommandProvider pluginCommandProvider = new PluginCommandProvider();
        try {
            Field field = PluginCommandProvider.class.getDeclaredField("pluginManagerService");
            field.setAccessible(true);
            field.set(pluginCommandProvider, pluginManagerService);
        } catch (Throwable throwable) {
            // ignore
        }

        String errorMessage = "Error command format. Pls type 'plugin -h' to get help message\n";

        assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plu")));
        assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plugin -h pluginA")));
        assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plugin -b pluginA")));
        assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plu")));

        assertTrue(pluginCommandProvider.getHelp().equals(
            pluginCommandProvider.handleCommand("plugin -h")));
        assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plugin ")));

        String details = pluginCommandProvider.handleCommand("plugin -m -d -s pluginA");
        assertTrue(details.contains("Activator"));
        assertTrue(details.contains("GroupId"));

        details = pluginCommandProvider.handleCommand("plugin -d pluginB");
        assertTrue(details.contains("GroupId"));
        assertFalse(details.contains("Activator"));

        details = pluginCommandProvider.handleCommand("plugin -m plugin.");
        assertTrue(details.contains("pluginA"));
        assertTrue(details.contains("pluginB"));

        details = pluginCommandProvider.handleCommand("plugin -m pluginC");
        assertTrue(details.contains("no matched plugin candidates."));
    }

    @Test
    public void testPluginList() {

        PluginCommandProvider pluginCommandProvider = new PluginCommandProvider();
        PluginCommand pluginCommand = pluginCommandProvider.new PluginCommand(null);
        assertFalse(pluginCommand.isValidate());
        pluginCommand = pluginCommandProvider.new PluginCommand("plugin -");
        assertFalse(pluginCommand.isValidate());
        pluginCommand = pluginCommandProvider.new PluginCommand("plugin -a");

        PluginManagerService pluginManagerService = mock(PluginManagerService.class);
        try {
            Field field = PluginCommandProvider.class.getDeclaredField("pluginManagerService");
            field.setAccessible(true);
            field.set(pluginCommandProvider, pluginManagerService);
        } catch (Throwable throwable) {
            // ignore
        }

        assertEquals("no plugins.\nplugin count = 0\n", pluginCommand.process());
        when(pluginManagerService.getAllPluginNames()).thenReturn(newHashSet("a"));
        assertEquals("a\nplugin count = 1\n", pluginCommand.process());
    }
}