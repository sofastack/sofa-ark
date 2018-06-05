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
package com.alipay.sofa.ark.spi.service.classloader;

import com.alipay.sofa.ark.spi.service.ArkService;

import java.util.List;

/**
 * Classloader Service
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface ClassloaderService extends ArkService {

    /**
     * prepare plugin exported class and resource index Cache
     */
    void prepareExportClassAndResourceCache();

    /**
     * Whether class is sun reflect related class
     * @param className class name
     * @return
     */
    boolean isSunReflectClass(String className);

    /**
     * Whether class is ark spi class
     * @param className class name
     * @return
     */
    boolean isArkSpiClass(String className);

    /**
     * Whether class is in import-class
     * @param pluginName plugin name
     * @param className class name
     * @return
     */
    boolean isClassInImport(String pluginName, String className);

    /**
     * Find classloader which export class for import class
     * @param className class name
     * @return
     */
    ClassLoader findExportClassloader(String className);

    /**
     * Whether resource is in import-resources
     * @param pluginName
     * @param resourceName
     * @return
     */
    boolean isResourceInImport(String pluginName, String resourceName);

    /**
     * Find classloaders which export resource for import resource in priority orders for import-resources
     * @param resourceName resource name
     * @return classloader list
     */
    List<ClassLoader> findExportResourceClassloadersInOrder(String resourceName);

    /**
     * Get JDK Related class classloader
     * @return
     */
    ClassLoader getJDKClassloader();

    /**
     * Get Ark Container classloader
     * @return
     */
    ClassLoader getArkClassloader();

    /**
     * Get system classloader
     * @return
     */
    ClassLoader getSystemClassloader();

    /**
     * Get java agent classloader
     * @return
     */
    ClassLoader getAgentClassloader();

    /**
     * Whether class is denied by biz
     * @param bizIdentity biz identity
     * @param className class name
     * @return
     */
    boolean isDeniedImportClass(String bizIdentity, String className);

    /**
     * Whether resource is denied by biz
     * @param bizIdentity biz identity
     * @param resourceName resource name
     * @return
     */
    boolean isDeniedImportResource(String bizIdentity, String resourceName);
}