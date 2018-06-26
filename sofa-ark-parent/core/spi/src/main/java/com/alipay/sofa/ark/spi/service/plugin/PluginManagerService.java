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
package com.alipay.sofa.ark.spi.service.plugin;

import com.alipay.sofa.ark.spi.model.Plugin;

import java.util.List;
import java.util.Set;

/**
 * Service to manage ark plugin
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface PluginManagerService {

    /**
     * Register ark plugin
     * @param plugin ark plugin info
     */
    void registerPlugin(Plugin plugin);

    /**
     * Get plugin info by plugin name
     * @param pluginName plugin name
     * @return
     */
    Plugin getPluginByName(String pluginName);

    /**
     * Get all plugin names
     * @return
     */
    Set<String> getAllPluginNames();

    /**
     * Get all plugins in priority PriorityOrdered
     * @return
     */
    List<Plugin> getPluginsInOrder();

}