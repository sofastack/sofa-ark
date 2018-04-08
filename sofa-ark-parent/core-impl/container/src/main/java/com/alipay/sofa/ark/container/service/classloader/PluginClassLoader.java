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
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.exception.ArkLoaderException;

import java.net.URL;

/**
 * Ark Plugin Classloader
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginClassLoader extends AbstractClasspathClassloader {

    private String pluginName;

    public PluginClassLoader(String pluginName, URL[] urls) {
        super(urls);
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    @Override
    protected Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {

        // 1. sun reflect related class throw exception directly
        if (classloaderService.isSunReflectClass(name)) {
            throw new ArkLoaderException(
                String
                    .format(
                        "[ArkPlugin Loader] %s : can not load class: %s, this class can only be loaded by sun.reflect.DelegatingClassLoader",
                        pluginName, name));
        }

        // 2. findLoadedClass
        Class<?> clazz = findLoadedClass(name);

        // 3. JDK related class
        if (clazz == null) {
            clazz = resolveJDKClass(name);
        }

        // 4. Ark Spi class
        if (clazz == null) {
            clazz = resolveArkClass(name);
        }

        // 5. Import class
        if (clazz == null) {
            clazz = resolveImportClass(name);
        }

        // 6. Plugin classpath class
        if (clazz == null) {
            clazz = resolveLocalClass(name);
        }

        // 7. Java Agent ClassLoader for agent problem
        if (clazz == null) {
            clazz = resolveJavaAgentClass(name);
        }

        if (clazz != null) {
            if (resolve) {
                super.resolveClass(clazz);
            }
            return clazz;
        }

        throw new ArkLoaderException(String.format(
            "[ArkPlugin Loader] %s : can not load class: %s", pluginName, name));
    }

    @Override
    boolean shouldFindClassInExport(String className) {
        return classloaderService.isResourceInImport(pluginName, className);
    }

    @Override
    boolean shouldFindResourceInExport(String resourceName) {
        return classloaderService.isResourceInImport(pluginName, resourceName);
    }

    /**
     * Load import class
     * @param name
     * @return
     */
    private Class<?> resolveImportClass(String name) {
        if (classloaderService.isClassInImport(pluginName, name)) {
            ClassLoader importClassloader = classloaderService.findExportClassloader(name);
            if (importClassloader != null) {
                try {
                    return importClassloader.loadClass(name);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return null;
    }

}