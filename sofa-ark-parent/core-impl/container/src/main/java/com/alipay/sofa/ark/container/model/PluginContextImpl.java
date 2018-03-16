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
package com.alipay.sofa.ark.container.model;

import com.alipay.sofa.ark.container.registry.PluginNameServiceFilter;
import com.alipay.sofa.ark.container.registry.PluginServiceFilter;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;

import java.util.List;
import java.util.Set;

/**
 * Standard Plugin Context Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginContextImpl implements PluginContext {

    private Plugin               plugin;

    private PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
                                                          .getService(PluginManagerService.class);

    private RegistryService      registryService      = ArkServiceContainerHolder.getContainer()
                                                          .getService(RegistryService.class);

    private PluginServiceFilter  pluginServiceFilter  = new PluginServiceFilter();

    public PluginContextImpl(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Plugin getPlugin(String pluginName) {
        return pluginManagerService.getPluginByName(pluginName);
    }

    @Override
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject) {
        return registryService.publishService(ifClass, implObject,
            new PluginServiceProvider(plugin));
    }

    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass) {
        return registryService.referenceService(ifClass, pluginServiceFilter);
    }

    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass, final String pluginName) {
        return registryService.referenceService(ifClass, new PluginNameServiceFilter(pluginName));
    }

    @Override
    public <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass) {
        return registryService.referenceServices(ifClass, pluginServiceFilter);
    }

    @Override
    public Set<String> getPluginNames() {
        return pluginManagerService.getAllPluginNames();
    }

    @Override
    public ClassLoader getClassLoader() {
        return plugin.getPluginClassLoader();
    }

}