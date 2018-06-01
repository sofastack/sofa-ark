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
package com.alipay.sofa.ark.container.registry;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.registry.ServiceProviderType;

/**
 * Plugin Service Provider, when service is published by plugin, then use this provider
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginServiceProvider extends AbstractServiceProvider {

    private Plugin plugin;

    public PluginServiceProvider(Plugin plugin) {
        super(ServiceProviderType.ARK_PLUGIN);
        AssertUtils.assertNotNull(plugin, "Plugin should not be null.");
        this.plugin = plugin;
    }

    @Override
    public String getServiceProviderDesc() {
        return String.format("%s:%s", super.getServiceProviderDesc(), plugin.getPluginName());
    }

    @Override
    public int getPriority() {
        return plugin.getPriority();
    }

    public String getPluginName() {
        return plugin.getPluginName();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + plugin.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }

        PluginServiceProvider serviceProvider = (PluginServiceProvider) obj;

        return plugin.equals(serviceProvider.getPlugin());
    }
}