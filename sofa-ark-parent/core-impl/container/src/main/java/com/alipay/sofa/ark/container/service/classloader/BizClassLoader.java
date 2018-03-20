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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.loader.jar.Handler;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * Ark Biz Classloader
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BizClassLoader extends URLClassLoader {

    private static final String CLASS_RESOURCE_SUFFIX = ".class";

    private String              bizName;

    private ClassloaderService  classloaderService    = ArkServiceContainerHolder.getContainer()
                                                          .getService(ClassloaderService.class);

    public BizClassLoader(String bizName, URL[] urls) {
        super(urls, null);
        this.bizName = bizName;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        Handler.setUseFastConnectionExceptions(true);
        try {
            return loadClassInternal(name, resolve);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    private Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {

        // 1. sun reflect related class throw exception directly
        if (classloaderService.isSunReflectClass(name)) {
            throw new ArkLoaderException(
                String
                    .format(
                        "[ArkBiz Loader] %s : can not load class: %s, this class can only be loaded by sun.reflect.DelegatingClassLoader",
                        bizName, name));
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

        // 5. Plugin Export class
        if (clazz == null) {
            clazz = resolveExportClass(name);
        }

        // 6. Biz classpath class
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

        throw new ArkLoaderException(String.format("[ArkBiz Loader] %s : can not load class: %s",
            bizName, name));
    }

    @Override
    public URL getResource(String name) {
        Handler.setUseFastConnectionExceptions(true);
        try {
            URL url = super.getResource(name);

            if (url == null && name.endsWith(CLASS_RESOURCE_SUFFIX)) {
                url = findClassResource(name);
            }

            if (url == null) {
                ClassLoader classLoader = classloaderService.findResourceExportClassloader(name);
                // find export resource classloader and not self
                if (classLoader != null && classLoader != this) {
                    url = classLoader.getResource(name);
                }
            }
            return url;
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    private String transformClassName(String name) {
        if (name.endsWith(CLASS_RESOURCE_SUFFIX)) {
            name = name.substring(0, name.length() - CLASS_RESOURCE_SUFFIX.length());
        }
        return name.replace("/", ".");
    }

    private URL findClassResource(String resourceName) {
        String className = transformClassName(resourceName);
        ClassLoader classLoader = classloaderService.findImportClassloader(className);
        return classLoader == null ? null : classLoader.getResource(resourceName);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            Enumeration<URL> urls = new UseFastConnectionExceptionsEnumeration(
                super.getResources(name));
            if (!urls.hasMoreElements()) {
                ClassLoader classLoader = classloaderService.findResourceExportClassloader(name);
                // find export resource classloader and not self
                if (classLoader != null && classLoader != this) {
                    urls = classLoader.getResources(name);
                }
            }
            return urls;
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Load JDK class
     * @param name class name
     * @return
     */
    private Class<?> resolveJDKClass(String name) {
        try {
            return classloaderService.getJDKClassloader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    /**
     * Load ark spi class
     * @param name
     * @return
     */
    private Class<?> resolveArkClass(String name) {
        if (classloaderService.isArkSpiClass(name)) {
            try {
                return classloaderService.getArkClassloader().loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Load plugin export class
     * @param name
     * @return
     */
    private Class<?> resolveExportClass(String name) {
        ClassLoader importClassloader = classloaderService.findImportClassloader(name);
        if (importClassloader != null) {
            try {
                return importClassloader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Load biz classpath class
     * @param name
     * @return
     */
    private Class<?> resolveLocalClass(String name) {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    /**
     * Load SystemClassloader class
     * @param name
     * @return
     */
    private Class<?> resolveSystemClass(String name) {
        try {
            return classloaderService.getSystemClassloader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    /**
     * Load Java Agent Class
     * @param name className
     * @return
     */
    private Class<?> resolveJavaAgentClass(String name) {
        try {
            return classloaderService.getAgentClassloader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }
}