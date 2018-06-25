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

import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service Implementation to manager ark plugin
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class PluginManagerServiceImpl implements PluginManagerService {

    private ConcurrentHashMap<String, Plugin> plugins = new ConcurrentHashMap<>();

    @Override
    public void registerPlugin(Plugin plugin) {
        if (plugins.putIfAbsent(plugin.getPluginName(), plugin) != null) {
            throw new ArkException(String.format("duplicate plugin: %s exists.",
                plugin.getPluginName()));
        }
    }

    @Override
    public Plugin getPluginByName(String pluginName) {
        return plugins.get(pluginName);
    }

    @Override
    public Set<String> getAllPluginNames() {
        return plugins.keySet();
    }

    @Override
    public List<Plugin> getPluginsInOrder() {
        List<Plugin> pluginList = new ArrayList<>(plugins.values());
        Collections.sort(pluginList, new OrderComparator());
        return pluginList;
    }
}