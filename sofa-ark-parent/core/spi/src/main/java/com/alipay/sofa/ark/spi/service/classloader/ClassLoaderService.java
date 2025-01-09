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

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.ArkService;

import java.util.List;

/**
 * ClassLoader Service
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface ClassLoaderService extends ArkService {

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
     * Whether class is ark api class
     * @param className class name
     * @return
     */
    boolean isArkApiClass(String className);

    /**
     * Whether class is ark log class.
     * @param className
     * @return
     */
    boolean isArkLogClass(String className);

    /**
     * Whether class is ark exception class.
     * @param className
     * @return
     */
    boolean isArkExceptionClass(String className);

    /**
     * Whether class is in import-class
     * @param pluginName plugin name
     * @param className class name
     * @return
     */
    boolean isClassInImport(String pluginName, String className);

    /**
     * Find export mode for class className
     * @param className
     * @return
     */
    String getExportMode(String className);

    /**
     * Find classloader which export class for import class
     * @param className class name
     * @return
     */
    ClassLoader findExportClassLoader(String className);

    ClassLoader findExportClassLoaderByBiz(Biz biz, String className);

    Plugin findExportPlugin(String className);

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
    List<ClassLoader> findExportResourceClassLoadersInOrder(String resourceName);

    List<ClassLoader> findExportResourceClassLoadersInOrderByBiz(Biz biz, String resourceName);

    /**
     * Get JDK Related class classloader
     * @return
     */
    ClassLoader getJDKClassLoader();

    /**
     * Get Ark Container classloader
     * @return
     */
    ClassLoader getArkClassLoader();

    /**
     * Get system classloader
     * @return
     */
    ClassLoader getSystemClassLoader();

    /**
     * Get java agent classloader
     * @return
     */
    ClassLoader getAgentClassLoader();

    /**
     * Get Ark Biz ClassLoader
     * @return
     */
    ClassLoader getBizClassLoader(String bizIdentity);

    /**
     * Get Ark Master Biz ClassLoader
     * @return
     */
    ClassLoader getMasterBizClassLoader();

    /**
     * Get Ark Plugin ClassLoader
     * @param pluginName
     * @return
     */
    ClassLoader getPluginClassLoader(String pluginName);

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
