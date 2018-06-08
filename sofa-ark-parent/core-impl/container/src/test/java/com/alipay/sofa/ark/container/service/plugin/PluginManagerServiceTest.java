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
import com.alipay.sofa.ark.container.service.plugin.PluginManagerServiceImpl;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginManagerServiceTest {

    private PluginManagerService pluginManagerService = new PluginManagerServiceImpl();

    @Test
    public void testGetPluginByName() {
        PluginModel plugin = new PluginModel();
        plugin.setPluginName("plugin A");
        pluginManagerService.registerPlugin(plugin);
        Assert.assertNotNull(pluginManagerService.getPluginByName(plugin.getPluginName()));
        Assert.assertNull(pluginManagerService.getPluginByName("test"));

    }

    @Test
    public void testGetAllPluginNames() {
        PluginModel pluginA = new PluginModel();
        pluginA.setPluginName("plugin A");
        pluginManagerService.registerPlugin(pluginA);

        PluginModel pluginB = new PluginModel();
        pluginB.setPluginName("plugin B");
        pluginManagerService.registerPlugin(pluginB);

        Assert.assertTrue(pluginManagerService.getAllPluginNames()
            .contains(pluginA.getPluginName()));
        Assert.assertTrue(pluginManagerService.getAllPluginNames()
            .contains(pluginB.getPluginName()));
        Assert.assertEquals(2, pluginManagerService.getAllPluginNames().size());
    }

    @Test
    public void testGetPluginsInOrder() {
        PluginModel pluginA = new PluginModel();
        pluginA.setPluginName("plugin A").setPriority("100");
        pluginManagerService.registerPlugin(pluginA);

        PluginModel pluginB = new PluginModel();
        pluginB.setPluginName("plugin B").setPriority("10");
        pluginManagerService.registerPlugin(pluginB);

        PluginModel pluginC = new PluginModel();
        pluginC.setPluginName("plugin C").setPriority("1000");
        pluginManagerService.registerPlugin(pluginC);

        Assert.assertEquals(3, pluginManagerService.getPluginsInOrder().size());
        Assert.assertEquals(pluginB, pluginManagerService.getPluginsInOrder().get(0));
        Assert.assertEquals(pluginA, pluginManagerService.getPluginsInOrder().get(1));
        Assert.assertEquals(pluginC, pluginManagerService.getPluginsInOrder().get(2));
    }
}