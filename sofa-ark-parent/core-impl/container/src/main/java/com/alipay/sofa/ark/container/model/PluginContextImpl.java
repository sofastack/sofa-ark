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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.registry.ServiceFilter;
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
        return publishService(ifClass, implObject, StringUtils.EMPTY_STRING);
    }

    @Override
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject, String uniqueId) {
        return registryService.publishService(ifClass, implObject, uniqueId,
            new PluginServiceProvider(plugin));
    }

    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass) {
        return referenceService(ifClass, StringUtils.EMPTY_STRING);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ServiceReference<T> referenceService(Class<T> ifClass, String uniqueId) {
        return registryService.referenceService(ifClass, uniqueId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceReference> referenceServices(ServiceFilter serviceFilter) {
        return registryService.referenceServices(serviceFilter);
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