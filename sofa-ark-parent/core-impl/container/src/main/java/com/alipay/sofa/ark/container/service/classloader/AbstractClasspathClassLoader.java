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

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.bootstrap.UseFastConnectionExceptionsEnumeration;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.loader.jar.Handler;
import com.alipay.sofa.ark.loader.jar.JarUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.google.common.cache.Cache;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.jar.JarFile;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * Abstract Classpath ClassLoader, basic logic to load class/resource, sub class need to implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
public abstract class AbstractClasspathClassLoader extends URLClassLoader {

    protected static final String              CLASS_RESOURCE_SUFFIX = ".class";

    protected ClassLoaderService               classloaderService    = ArkServiceContainerHolder
                                                                         .getContainer()
                                                                         .getService(
                                                                             ClassLoaderService.class);

    protected Cache<String, LoadClassResult>   classCache;

    protected Cache<String, Optional<Package>> packageCache;

    protected Cache<String, Optional<URL>>     urlResourceCache      = newBuilder()
                                                                         .expireAfterWrite(10,
                                                                             SECONDS).build();
    protected boolean                          exploded              = false;

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public AbstractClasspathClassLoader(URL[] urls) {
        super(urls, null);
        classCache = newBuilder()
            .initialCapacity(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CLASS_SIZE_INITIAL, 2500))
            .maximumSize(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CLASS_SIZE_MAX, 2500))
            .concurrencyLevel(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CONCURRENCY_LEVEL, 16))
            .expireAfterWrite(30, SECONDS).recordStats().build();

        packageCache = newBuilder()
            .initialCapacity(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CLASS_SIZE_INITIAL, 2000))
            .maximumSize(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CLASS_SIZE_MAX, 2000))
            .concurrencyLevel(
                ArkConfigs.getIntValue(Constants.ARK_CLASSLOADER_CACHE_CONCURRENCY_LEVEL, 16))
            .expireAfterWrite(30, SECONDS).recordStats().build();
    }

    public AbstractClasspathClassLoader(URL[] urls, boolean exploded) {
        this(urls);
        this.exploded = exploded;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        Handler.setUseFastConnectionExceptions(true);
        try {
            if (!exploded) {
                definePackageIfNecessary(name);
            }
            return loadClassWithCache(name, resolve);
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
            Optional<Package> pkgInCache = packageCache.getIfPresent(packageName);
            // null means not cached, package haven't been defined yet, try to define it now
            if (pkgInCache == null) {
                try {
                    definePackage(className, packageName);
                } catch (IllegalArgumentException ex) {
                    // Tolerate race condition due to being parallel capable
                } finally {
                    // cache define result
                    Package pkgAfterDefined = super.getPackage(packageName);
                    packageCache.put(packageName, pkgAfterDefined == null ? Optional.empty()
                        : Optional.of(pkgAfterDefined));
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

    @Override
    protected Package getPackage(String name) {
        Optional<Package> pkgInCache = packageCache.getIfPresent(name);
        if (pkgInCache != null && pkgInCache.isPresent()) {
            return pkgInCache.orElse(null);
        }

        Package pkg = super.getPackage(name);
        // don't cache null here because we may define pkg successfully later,
        // only cache null after define fail
        if (pkg != null) {
            packageCache.put(name, Optional.of(pkg));
        }
        return pkg;
    }

    /**
     * cache load results of classes recently loaded
     * @param name
     * @param resolve
     * @return
     * @throws ArkLoaderException
     */
    protected Class<?> loadClassWithCache(String name, boolean resolve) throws ArkLoaderException {
        try {
            LoadClassResult resultInCache = classCache.get(name, () -> {
                LoadClassResult r = new LoadClassResult();
                try {
                    r.setClazz(loadClassInternal(name, resolve));
                } catch (ArkLoaderException ex) {
                    r.setEx(ex);
                }
                return r;
            });

            if (resultInCache.getEx() != null) {
                throw resultInCache.getEx();
            }

            return resultInCache.getClazz();
        } catch (ExecutionException e) {
            throw new ArkLoaderException(
                    String.format("[Ark Loader] unexpected exception when load class: %s", name),
                    e.getCause());
        }
    }

    /**
     * Real logic to load class，need to implement by Sub ClassLoader
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
        Optional<URL> urlOptional = urlResourceCache.getIfPresent(name);
        try {
            if (urlOptional != null) {
                return urlOptional.orElse(null);
            }
            URL ret = preFindResource(name);
            if (ret != null) {
                return ret;
            }
            ret = getResourceInternal(name);
            URL url = ret != null ? ret : postFindResource(name);
            urlResourceCache.put(name, url != null ? Optional.of(url) : Optional.empty());
            return url;
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Real logic to get resource
     * @param name
     * @return
     */
    protected URL getResourceInternal(String name) {
        // 1. find jdk resource
        URL url = getJdkResource(name);

        // 2. find export resource
        if (url == null) {
            url = getExportResource(name);
        }

        // 3. get .class resource
        if (url == null) {
            url = getClassResource(name);
        }

        // 4. get local resource
        if (url == null) {
            url = getLocalResource(name);
        }

        return url;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            if (isDeclaredMode()) {
                List<Enumeration<URL>> enumerationList = new ArrayList<>();
                // 1. get resources from ClassLoaderHook.
                enumerationList.add(preFindResources(name));
                // 2. get jdk resources, plugin resources declared by the biz and resources in the biz.
                enumerationList.add(getResourcesInternal(name));
                // 3. delegate master biz to get resources declared by the biz.
                enumerationList.add(postFindResources(name));
                // unique urls
                return uniqueUrls(enumerationList, name);
            } else {
                Enumeration<URL> ret = preFindResources(name);
                if (ret != null && ret.hasMoreElements()) {
                    return ret;
                }
                ret = getResourcesInternal(name);
                if (ret != null && ret.hasMoreElements()) {
                    return ret;
                }
                ret = postFindResources(name);
                return ret != null ? ret : new CompoundEnumeration<URL>(
                    (Enumeration<URL>[]) new Enumeration<?>[] {});
            }

        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    private Enumeration<URL> uniqueUrls(List<Enumeration<URL>> enumerationList, String resourceName) {
        // unique urls
        Set<String> temp = new HashSet<>();
        List<URL> uniqueUrls = new ArrayList<>();

        for (Enumeration<URL> e : enumerationList) {
            while (e != null && e.hasMoreElements()) {
                URL resourceUrl = e.nextElement();
                String filePath = resourceUrl.getFile().replace("file:", "");

                if (filePath.endsWith(resourceName)) {
                    filePath = filePath.substring(0, filePath.lastIndexOf(resourceName));
                }
                String artifactId = JarUtils.parseArtifactId(filePath);
                if (artifactId == null) {
                    uniqueUrls.add(resourceUrl);
                } else {
                    if (!temp.contains(artifactId)) {
                        uniqueUrls.add(resourceUrl);
                        temp.add(artifactId);
                    }
                }
            }
        }
        return Collections.enumeration(uniqueUrls);
    }

    /**
     * Real logic to get resources
     * @param name
     * @return
     * @throws IOException
     */
    protected Enumeration<URL> getResourcesInternal(String name) throws IOException {
        List<Enumeration<URL>> enumerationList = new ArrayList<>();
        // 1. find jdk resources
        enumerationList.add(getJdkResources(name));

        // 2. find exported resources
        enumerationList.add(getExportResources(name));

        // 3. find local resources
        enumerationList.add(getLocalResources(name));

        return new CompoundEnumeration<>(
            enumerationList.toArray((Enumeration<URL>[]) new Enumeration<?>[0]));
    }

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

    private boolean isDeclaredMode() {
        return this instanceof BizClassLoader && ((BizClassLoader) this).checkDeclaredMode();
    }

    /**
     * Load JDK class
     * @param name class name
     * @return
     */
    protected Class<?> resolveJDKClass(String name) {
        try {
            return classloaderService.getJDKClassLoader().loadClass(name);
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
        if (!PluginModel.EXPORTMODE_OVERRIDE.equals(classloaderService.getExportMode(name))) {
            return doResolveExportClass(name);
        } else {
            ClassLoader classLoader = classloaderService.findExportClassLoader(name);
            URL url = classLoader.getResource(name.replace('.', '/') + ".class");
            if (url != null) {
                String filePath = url.getFile().replaceFirst("file:", "");
                try {
                    byte[] bytes;
                    if (filePath.contains(".jar")) {
                        bytes = getClassBytesFromJar(filePath, name.replace('.', '/') + ".class");
                    } else {
                        bytes = FileUtils.readFileToByteArray(new File(filePath));
                    }
                    return defineClass(name, bytes, 0, bytes.length);
                } catch (Exception e) {
                    ArkLoggerFactory.getDefaultLogger().warn(
                        String.format("can't convert class to reLoad by bizClassLoader: %s",
                            e.getMessage()));
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        }
    }

    private byte[] getClassBytesFromJar(String jarFilePath, String className) throws IOException {
        com.alipay.sofa.ark.loader.jar.JarFile jarFile = JarUtils
            .getNestedRootJarFromJarLocation(jarFilePath);
        try (InputStream inputStream = jarFile.getInputStream(jarFile.getJarEntry(className))) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }

    private Class<?> doResolveExportClass(String name) {
        if (shouldFindExportedClass(name)) {
            ClassLoader importClassLoader = classloaderService.findExportClassLoader(name);
            if (importClassLoader != null) {
                try {
                    Class<?> clazz = importClassLoader.loadClass(name);
                    if (clazz == null) {
                        return null;
                    }
                    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
                    if (this instanceof BizClassLoader
                        && ((BizClassLoader) this).getBizModel() != null) {
                        BizModel bizModel = ((BizClassLoader) this).getBizModel();

                        if (url != null && bizModel.isDeclared(url, "")) {
                            return clazz;
                        }
                        String classResourceName = name.replace('.', '/') + ".class";
                        Enumeration<URL> urls = importClassLoader.getResources(classResourceName);
                        while (urls.hasMoreElements()) {
                            URL resourceUrl = urls.nextElement();
                            if (resourceUrl != null
                                && bizModel.isDeclared(resourceUrl, classResourceName)) {
                                ArkLoggerFactory.getDefaultLogger().warn(
                                    String.format(
                                        "find class %s from %s in multiple dependencies.", name,
                                        resourceUrl.getFile()));
                                return clazz;
                            }
                        }
                    } else {
                        return clazz;
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError | IOException e) {
                    // just log when debug level
                    if (ArkLoggerFactory.getDefaultLogger().isDebugEnabled()) {
                        // log debug message
                        ArkLoggerFactory.getDefaultLogger().debug(
                            "Fail to load export class " + name, e);
                    }
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
        if (classloaderService.isArkSpiClass(name) || classloaderService.isArkApiClass(name)
            || classloaderService.isArkLogClass(name)
            || classloaderService.isArkExceptionClass(name)) {
            try {
                return classloaderService.getArkClassLoader().loadClass(name);
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
            classloaderService.getAgentClassLoader().loadClass(name);
            return classloaderService.getSystemClassLoader().loadClass(name);
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
            List<ClassLoader> exportResourceClassLoadersInOrder = classloaderService
                .findExportResourceClassLoadersInOrder(resourceName);
            if (exportResourceClassLoadersInOrder != null) {
                for (ClassLoader exportResourceClassLoader : exportResourceClassLoadersInOrder) {
                    url = exportResourceClassLoader.getResource(resourceName);
                    if (url != null && this instanceof BizClassLoader) {
                        if (((BizClassLoader) (this)).getBizModel().isDeclared(url, resourceName)) {
                            return url;
                        } else {
                            return null;
                        }
                    }
                    return url;
                }
            }

        }
        return null;
    }

    /**
     * Find jdk dir resource
     * @param resourceName
     * @return
     */
    protected URL getJdkResource(String resourceName) {
        return classloaderService.getJDKClassLoader().getResource(resourceName);
    }

    /**
     * Find .class resource
     * @param resourceName
     * @return
     */
    protected URL getClassResource(String resourceName) {
        if (resourceName.endsWith(CLASS_RESOURCE_SUFFIX)) {
            String className = transformClassName(resourceName);
            if (resolveArkClass(className) != null) {
                return classloaderService.getArkClassLoader().getResource(resourceName);
            }

            if (shouldFindExportedClass(className)) {
                ClassLoader classLoader = classloaderService.findExportClassLoader(className);
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
            List<ClassLoader> exportResourceClassLoadersInOrder = classloaderService
                .findExportResourceClassLoadersInOrder(resourceName);
            if (exportResourceClassLoadersInOrder != null) {
                List<Enumeration<URL>> enumerationList = new ArrayList<>();
                for (ClassLoader exportResourceClassLoader : exportResourceClassLoadersInOrder) {
                    if (exportResourceClassLoader instanceof AbstractClasspathClassLoader) {
                        enumerationList
                            .add(((AbstractClasspathClassLoader) exportResourceClassLoader)
                                .getLocalResources(resourceName));
                    } else {
                        enumerationList.add(exportResourceClassLoader.getResources(resourceName));
                    }
                }

                Enumeration<URL> urls = new CompoundEnumeration<>(
                    enumerationList.toArray((Enumeration<URL>[]) new Enumeration<?>[0]));
                if (this instanceof BizClassLoader) {
                    BizModel bizModel = ((BizClassLoader) this).getBizModel();
                    List<URL> matchedResourceUrls = new ArrayList<>();
                    while (urls.hasMoreElements()) {
                        URL resourceUrl = urls.nextElement();

                        if (resourceUrl != null && bizModel.isDeclared(resourceUrl, resourceName)) {
                            matchedResourceUrls.add(resourceUrl);
                        }
                    }
                    return Collections.enumeration(matchedResourceUrls);
                }
                return urls;
            }
        }
        return Collections.emptyEnumeration();
    }

    protected Enumeration<URL> getLocalResources(String resourceName) throws IOException {
        return new UseFastConnectionExceptionsEnumeration(super.getResources(resourceName));
    }

    protected Enumeration<URL> getJdkResources(String resourceName) throws IOException {
        return new UseFastConnectionExceptionsEnumeration(classloaderService.getJDKClassLoader()
            .getResources(resourceName));
    }

    public void clearCache() {
        classCache.cleanUp();
        packageCache.cleanUp();
        urlResourceCache.cleanUp();
    }

    public void invalidAllCache() {
        classCache.invalidateAll();
        packageCache.invalidateAll();
        urlResourceCache.invalidateAll();
    }

    /**
     * invoked before {@link #loadClass(String, boolean)}
     *
     * @param className
     * @return
     * @throws ArkLoaderException
     */
    protected abstract Class<?> preLoadClass(String className) throws ArkLoaderException;

    /**
     * invoked after {@link #loadClass(String, boolean)}
     *
     * @param className
     * @return
     * @throws ArkLoaderException
     */
    protected abstract Class<?> postLoadClass(String className) throws ArkLoaderException;

    /**
     * invoked before {@link #getResource(String)}
     *
     * @param resourceName
     * @return
     */
    protected abstract URL preFindResource(String resourceName);

    /**
     * invoked after {@link #getResource(String)}
     *
     * @param resourceName
     * @return
     */
    protected abstract URL postFindResource(String resourceName);

    /**
     * invoked before {@link #getResources(String)}
     *
     * @param resourceName
     * @return
     */
    protected abstract Enumeration<URL> preFindResources(String resourceName) throws IOException;

    /**
     * invoked after {@link #getResources(String)}
     *
     * @param resourceName
     * @return
     */
    protected abstract Enumeration<URL> postFindResources(String resourceName) throws IOException;

    public static class LoadClassResult {
        private ArkLoaderException ex;
        private Class              clazz;

        public ArkLoaderException getEx() {
            return ex;
        }

        public void setEx(ArkLoaderException ex) {
            this.ex = ex;
        }

        public Class getClazz() {
            return clazz;
        }

        public void setClazz(Class clazz) {
            this.clazz = clazz;
        }
    }
}
