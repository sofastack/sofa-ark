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
package com.alipay.sofa.ark.spi.model;

import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceReference;

import java.util.List;
import java.util.Set;

/**
 * SOFAArk Plugin Runtime Context
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface PluginContext {

    /**
     * get Plugin
     * @return plugin
     */
    Plugin getPlugin();

    /**
     * get Plugin by Plugin Name
     * @param pluginName plugin name
     * @return plugin
     */
    Plugin getPlugin(String pluginName);

    /**
     * get All Plugin Names
     * @return
     */
    Set<String> getPluginNames();

    /**
     * get Plugin ClassLoader
     * @return plugin classloader
     */
    ClassLoader getClassLoader();

    /**
     * Publish Plugin Service
     * @param ifClass service interface
     * @param implObject service implement object
     * @param <T>
     * @return
     */
    <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject);

    /**
     * Publish Plugin Service
     * @param ifClass service interface
     * @param implObject service implement object
     * @param uniqueId service implementation id
     * @param <T>
     * @return
     */
    <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject, String uniqueId);

    /**
     * Get Service publish by plugin, when there are multiple services, return the highest priority plugin service
     * @param ifClass service interface
     * @param <T>
     * @return service reference
     */
    <T> ServiceReference<T> referenceService(Class<T> ifClass);

    /**
     * Get Service publish  by one specific plugin
     * @param ifClass service interface
     * @param <T>
     * @param uniqueId service implementation
     * @return service reference
     */
    <T> ServiceReference<T> referenceService(Class<T> ifClass, String uniqueId);

    /**
     * Get Service List publish by plugin
     * @param serviceFilter service filter
     * @return
     */
    List<ServiceReference> referenceServices(ServiceFilter serviceFilter);

}