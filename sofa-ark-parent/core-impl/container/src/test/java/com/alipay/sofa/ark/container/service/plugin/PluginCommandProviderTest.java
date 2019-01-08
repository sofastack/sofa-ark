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

import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class PluginCommandProviderTest {

    @Test
    public void testPluginCommandFormat() {
        PluginCommandProvider pluginCommandProvider = new PluginCommandProvider();
        Assert.assertTrue(pluginCommandProvider.validate(" plugin "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -h "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -m pluginA "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -s pluginB pluginA "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -d plugin* "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -m -d -s plugin* "));
        Assert.assertTrue(pluginCommandProvider.validate(" plugin -msd pluginA "));

        Assert.assertFalse(pluginCommandProvider.validate(" plu"));
        Assert.assertFalse(pluginCommandProvider.validate(" plugin -h pluginA "));
        Assert.assertFalse(pluginCommandProvider.validate(" plugin -hm pluginA "));
        Assert.assertFalse(pluginCommandProvider.validate(" plugin -mb pluginA "));
        Assert.assertFalse(pluginCommandProvider.validate(" plugin -m -b pluginA "));
        Assert.assertFalse(pluginCommandProvider.validate(" plugin -m  "));
    }

    @Test
    public void testPluginCommandProcess(@Mocked final PluginManagerService pluginManagerService) {
        URL[] urls = ((URLClassLoader) this.getClass().getClassLoader()).getURLs();
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

        new Expectations() {
            {
                pluginManagerService.getAllPluginNames();
                result = set;
                minTimes = 0;

                pluginManagerService.getPluginByName("pluginA");
                result = pluginA;
                minTimes = 0;

                pluginManagerService.getPluginByName("pluginB");
                result = pluginB;
                minTimes = 0;
            }
        };

        PluginCommandProvider pluginCommandProvider = new PluginCommandProvider();
        try {
            Field field = PluginCommandProvider.class.getDeclaredField("pluginManagerService");
            field.setAccessible(true);
            field.set(pluginCommandProvider, pluginManagerService);
        } catch (Throwable throwable) {
            // ignore
        }

        String errorMessage = "Error command format. Pls type 'plugin -h' to get help message\n";

        Assert.assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plu")));
        Assert.assertTrue(errorMessage.equals(pluginCommandProvider
            .handleCommand("plugin -h pluginA")));
        Assert.assertTrue(errorMessage.equals(pluginCommandProvider
            .handleCommand("plugin -b pluginA")));
        Assert.assertTrue(errorMessage.equals(pluginCommandProvider.handleCommand("plu")));

        Assert.assertTrue(pluginCommandProvider.getHelp().equals(
            pluginCommandProvider.handleCommand("plugin -h")));
        Assert.assertTrue("pluginA\npluginB\n\n".equals(pluginCommandProvider
            .handleCommand("plugin ")));

        String details = pluginCommandProvider.handleCommand("plugin -m -d -s pluginA");
        Assert.assertTrue(details.contains("Activator"));
        Assert.assertTrue(details.contains("GroupId"));

        details = pluginCommandProvider.handleCommand("plugin -d pluginB");
        Assert.assertTrue(details.contains("GroupId"));
        Assert.assertFalse(details.contains("Activator"));

        details = pluginCommandProvider.handleCommand("plugin -m plugin.");
        Assert.assertTrue(details.contains("pluginA"));
        Assert.assertTrue(details.contains("pluginB"));

        details = pluginCommandProvider.handleCommand("plugin -m pluginC");
        Assert.assertTrue(details.contains("no matched plugin candidates."));
    }

}