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
import sun.misc.CompoundEnumeration;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author ruoshan
 * @version $Id: AbstractClasspathClassloader.java, v 0.1 2018年04月08日 8:14 PM ruoshan Exp $
 */
public abstract class AbstractClasspathClassloader extends URLClassLoader {

    protected static final String CLASS_RESOURCE_SUFFIX = ".class";

    protected ClassloaderService  classloaderService    = ArkServiceContainerHolder.getContainer()
                                                            .getService(ClassloaderService.class);

    public AbstractClasspathClassloader(URL[] urls) {
        super(urls, null);
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

    /**
     * Real logic to load class，need to implement by Sub Classloader
     * @param name
     * @param resolve
     * @return
     * @throws ArkLoaderException
     */
    abstract protected Class<?> loadClassInternal(String name, boolean resolve)
                                                                               throws ArkLoaderException;

    @Override
    public URL getResource(String name) {
        Handler.setUseFastConnectionExceptions(true);
        try {
            URL url;
            // first find import resource
            if (shouldFindResourceInExport(name)) {
                List<ClassLoader> exportResourceClassloadersInOrder = classloaderService
                    .findExportResourceClassloadersInOrder(name);
                if (exportResourceClassloadersInOrder != null) {
                    for (ClassLoader exportResourceClassloader : exportResourceClassloadersInOrder) {
                        url = exportResourceClassloader.getResource(name);
                        if (url != null) {
                            return url;
                        }
                    }
                }

            }

            url = super.getResource(name);

            if (url != null) {
                return url;
            }

            if (name.endsWith(CLASS_RESOURCE_SUFFIX)) {
                url = findClassResource(name);
            }

            return url;

        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    protected URL findClassResource(String resourceName) {
        String className = transformClassName(resourceName);
        if (shouldFindClassInExport(className)) {
            ClassLoader classLoader = classloaderService.findExportClassloader(className);
            return classLoader == null ? null : classLoader.getResource(resourceName);
        }
        return null;
    }

    private String transformClassName(String name) {
        if (name.endsWith(CLASS_RESOURCE_SUFFIX)) {
            name = name.substring(0, name.length() - CLASS_RESOURCE_SUFFIX.length());
        }
        return name.replace("/", ".");
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return getResources(name, true);
    }

    @SuppressWarnings("unchecked")
    private Enumeration<URL> getResources(String name, boolean withExport) throws IOException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            Enumeration<URL> urlEnumeration = new UseFastConnectionExceptionsEnumeration(
                super.getResources(name));

            if (!withExport || !shouldFindResourceInExport(name)) {
                return urlEnumeration;
            }

            List<Enumeration<URL>> enumerationList = new ArrayList<>();
            enumerationList.add(urlEnumeration);

            List<ClassLoader> exportResourceClassloadersInOrder = classloaderService
                .findExportResourceClassloadersInOrder(name);
            if (exportResourceClassloadersInOrder != null) {
                for (ClassLoader exportResourceClassloader : exportResourceClassloadersInOrder) {
                    enumerationList.add(((AbstractClasspathClassloader) exportResourceClassloader)
                        .getResources(name, false));
                }
            }

            return new CompoundEnumeration<>(
                enumerationList.toArray((Enumeration<URL>[]) new Enumeration<?>[0]));
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Whether to find class that exported by other classloader
     * @param className class name
     * @return
     */
    abstract boolean shouldFindClassInExport(String className);

    /**
     * Whether to find resource that exported by other classloader
     * @param resourceName
     * @return
     */
    abstract boolean shouldFindResourceInExport(String resourceName);

    /**
     * Load ark spi class
     * @param name
     * @return
     */
    protected Class<?> resolveArkClass(String name) {
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
     * Load JDK class
     * @param name class name
     * @return
     */
    protected Class<?> resolveJDKClass(String name) {
        try {
            return classloaderService.getJDKClassloader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    /**
     * Load biz classpath class
     * @param name
     * @return
     */
    protected Class<?> resolveLocalClass(String name) {
        try {
            return super.findClass(name);
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
    protected Class<?> resolveJavaAgentClass(String name) {
        try {
            return classloaderService.getAgentClassloader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

}