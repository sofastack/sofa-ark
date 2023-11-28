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

import com.alipay.sofa.ark.exception.ArkRuntimeException;
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
     * get Plugin ClassLoader
     * @return plugin classLoader
     */
    ClassLoader getPluginClassLoader();

    /**
     * get Plugin Runtime Context
     * @return plugin context
     */
    PluginContext getPluginContext();

    /**
     * get Plugin Export Mode
     * default exportMode = classLoader, means export class to load in this plugin classLoader
     * exportMode = override, means export class to file only, and usually will be reload in another classLoader
     * @return
     */
    String getExportMode();

    /**
     * get Plugin Export Packages Config
     * @return plugin export packages
     */
    Set<String> getExportPackages();

    /**
     * get plugin export package which is exactly matched
     * @return
     */
    Set<String> getExportPackageNodes();

    /**
     * get plugin export package which is matched by prefix
     * @return
     */
    Set<String> getExportPackageStems();

    /**
     * get plugin Export Classes
     * @return plugin export classes
     */
    Set<String> getExportClasses();

    /**
     * get Plugin Import Packages Config
     * @return plugin import packages
     */
    Set<String> getImportPackages();

    /**
     * get plugin import package which is exactly matched
     * @return
     */
    Set<String> getImportPackageNodes();

    /**
     * get plugin import package which is matched by prefix
     * @return
     */
    Set<String> getImportPackageStems();

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
     * get Plugin Import Resources matched by prefix
     * @return plugin Import Resources matched by prefix
     */
    Set<String> getImportPrefixResourceStems();

    /**
     * get Plugin Import Resources matched by suffix
     * @return plugin Import Resources matched by suffix
     */
    Set<String> getImportSuffixResourceStems();

    /**
     * get Plugin Export Resources
     * @return get plugin export resources
     */
    Set<String> getExportResources();

    /**
     * get plugin export resources matched by prefix
     * @return plugin export resources matched by prefix
     */
    Set<String> getExportPrefixResourceStems();

    /**
     * get plugin export resources matched by suffix
     * @return get plugin export resources matched by suffix
     */
    Set<String> getExportSuffixResourceStems();

    /**
     * get Plugin Archive URL
     * @return plugin archive url
     */
    URL getPluginURL();

    /**
     * start Plugin
     * @throws ArkRuntimeException
     */
    void start() throws ArkRuntimeException;

    /**
     * stop Plugin
     * @throws ArkRuntimeException
     */
    void stop() throws ArkRuntimeException;

}