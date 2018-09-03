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

import com.alipay.sofa.ark.bootstrap.UseFastConnectionExceptionsEnumeration;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.loader.jar.Handler;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import sun.misc.CompoundEnumeration;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 * Abstract Classpath Classloader, basic logic to load class/resource, sub class need to implement
 *
 * @author ruoshan
 * @since 0.1.0
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
            definePackageIfNecessary(name);
            return loadClassInternal(name, resolve);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Define a package before a {@code findClass} call is made. This is necessary to
     * ensure that the appropriate manifest for nested JARs is associated with the
     * package.
     * @param className the class name being found
     */
    private void definePackageIfNecessary(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot >= 0) {
            String packageName = className.substring(0, lastDot);
            if (getPackage(packageName) == null) {
                try {
                    definePackage(className, packageName);
                } catch (IllegalArgumentException ex) {
                    // Tolerate race condition due to being parallel capable
                }
            }
        }
    }

    private void definePackage(final String className, final String packageName) {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                @Override
                public Object run() throws Exception {
                    StringBuilder pen = new StringBuilder(packageName.length() + 10);
                    StringBuilder cen = new StringBuilder(className.length() + 10);
                    String packageEntryName = pen.append(packageName.replace('.', '/')).append("/")
                        .toString();
                    String classEntryName = cen.append(className.replace('.', '/'))
                        .append(".class").toString();
                    for (URL url : getURLs()) {
                        try {
                            URLConnection connection = url.openConnection();
                            if (connection instanceof JarURLConnection) {
                                JarFile jarFile = ((JarURLConnection) connection).getJarFile();
                                if (jarFile.getEntry(classEntryName) != null
                                    && jarFile.getEntry(packageEntryName) != null
                                    && jarFile.getManifest() != null) {
                                    definePackage(packageName, jarFile.getManifest(), url);
                                    return null;
                                }
                            }
                        } catch (IOException ex) {
                            // Ignore
                        }
                    }
                    return null;
                }
            }, AccessController.getContext());
        } catch (java.security.PrivilegedActionException ex) {
            // Ignore
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
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        Handler.setUseFastConnectionExceptions(true);
        try {
            return getResourceInternal(name);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Real logic to get resource，need to implement by Sub Classloader
     * @param name
     * @return
     */
    abstract protected URL getResourceInternal(String name);

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            return getResourcesInternal(name);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Real logic to get resources，need to implement by Sub Classloader
     * @param name
     * @return
     * @throws IOException
     */
    abstract protected Enumeration<URL> getResourcesInternal(String name) throws IOException;

    /**
     * Whether to find class that exported by other classloader
     * @param className class name
     * @return
     */
    abstract boolean shouldFindExportedClass(String className);

    /**
     * Whether to find resource that exported by other classloader
     * @param resourceName
     * @return
     */
    abstract boolean shouldFindExportedResource(String resourceName);

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
     * Load export class
     * @param name
     * @return
     */
    protected Class<?> resolveExportClass(String name) {
        if (shouldFindExportedClass(name)) {
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
     * Load classpath class
     * @param name
     * @return
     */
    protected Class<?> resolveLocalClass(String name) {
        try {
            return super.loadClass(name, false);
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

    /**
     * Find export resource
     * @param resourceName
     * @return
     */
    protected URL getExportResource(String resourceName) {
        if (shouldFindExportedResource(resourceName)) {
            URL url;
            List<ClassLoader> exportResourceClassloadersInOrder = classloaderService
                .findExportResourceClassloadersInOrder(resourceName);
            if (exportResourceClassloadersInOrder != null) {
                for (ClassLoader exportResourceClassloader : exportResourceClassloadersInOrder) {
                    url = exportResourceClassloader.getResource(resourceName);
                    if (url != null) {
                        return url;
                    }
                }
            }

        }
        return null;
    }

    /**
     * Find .class resource
     * @param resourceName
     * @return
     */
    protected URL getClassResource(String resourceName) {
        if (resourceName.endsWith(CLASS_RESOURCE_SUFFIX)) {
            String className = transformClassName(resourceName);
            if (shouldFindExportedClass(className)) {
                ClassLoader classLoader = classloaderService.findExportClassloader(className);
                return classLoader == null ? null : classLoader.getResource(resourceName);
            }

        }
        return null;
    }

    /**
     * Find local resource
     * @param resourceName
     * @return
     */
    protected URL getLocalResource(String resourceName) {
        return super.getResource(resourceName);
    }

    private String transformClassName(String name) {
        if (name.endsWith(CLASS_RESOURCE_SUFFIX)) {
            name = name.substring(0, name.length() - CLASS_RESOURCE_SUFFIX.length());
        }
        return name.replace("/", ".");
    }

    /**
     * Find export resources
     * @param resourceName
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Enumeration<URL> getExportResources(String resourceName) throws IOException {
        if (shouldFindExportedResource(resourceName)) {
            List<ClassLoader> exportResourceClassloadersInOrder = classloaderService
                .findExportResourceClassloadersInOrder(resourceName);
            if (exportResourceClassloadersInOrder != null) {
                List<Enumeration<URL>> enumerationList = new ArrayList<>();
                for (ClassLoader exportResourceClassloader : exportResourceClassloadersInOrder) {
                    enumerationList.add(((AbstractClasspathClassloader) exportResourceClassloader)
                        .getLocalResources(resourceName));
                }
                return new CompoundEnumeration<>(
                    enumerationList.toArray((Enumeration<URL>[]) new Enumeration<?>[0]));
            }
        }
        return Collections.emptyEnumeration();
    }

    protected Enumeration<URL> getLocalResources(String resourceName) throws IOException {
        return new UseFastConnectionExceptionsEnumeration(super.getResources(resourceName));
    }

}