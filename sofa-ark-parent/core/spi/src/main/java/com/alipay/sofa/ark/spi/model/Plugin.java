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

import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;

import java.net.URL;
import java.util.Set;

/**
 * Ark Plugin Module Interface
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface Plugin extends PriorityOrdered {
    /**
     * get Plugin Name
     * @return plugin name
     */
    String getPluginName();

    /**
     * get Plugin GroupId
     * @return plugin groupId
     */
    String getGroupId();

    /**
     * get Plugin ArtifactId
     * @return plugin artifactId
     */
    String getArtifactId();

    /**
     * get Plugin Version
     * @return version
     */
    String getVersion();

    /**
     * get Plugin Priority, mainly used in following 3 scenarios:
     * 1. Plugin start up order
     * 2. Plugin export class load priority
     * 3. Plugin Service
     * priority is higher as the number is smaller
     * @return plugin priority
     */
    int getPriority();

    /**
     * get Plugin Activator
     * @return plugin activator
     */
    String getPluginActivator();

    /**
     * get Plugin Class Path
     * @return plugin class path
     */
    URL[] getClassPath();

    /**
     * get Plugin Classloader
     * @return plugin classloader
     */
    ClassLoader getPluginClassLoader();

    /**
     * get Plugin Runtime Context
     * @return plugin context
     */
    PluginContext getPluginContext();

    /**
     * get Plugin Export Packages
     * @return plugin export packages
     */
    Set<String> getExportPackages();

    /**
     * get plugin Export Classes
     * @return plugin export classes
     */
    Set<String> getExportClasses();

    /**
     * get Plugin Import Packages
     * @return plugin import packages
     */
    Set<String> getImportPackages();

    /**
     * get Plugin Import Classes
     * @return plugin import classes
     */
    Set<String> getImportClasses();

    /**
     * get Plugin Import Resources
     * @return plugin import resources
     */
    Set<String> getImportResources();

    /**
     * get Plugin Export Resources
     * @return get plugin export resources
     */
    Set<String> getExportResources();

    /**
     * get Plugin Export Index, contain both Plugin Export Packages and Plugin Export Classes
     * @return plugin export index
     */
    Set<String> getExportIndex();

    /**
     * get Plugin Archive URL
     * @return plugin archive url
     */
    URL getPluginURL();

    /**
     * start Plugin
     * @throws ArkException
     */
    void start() throws ArkException;

    /**
     * stop Plugin
     * @throws ArkException
     */
    void stop() throws ArkException;

}