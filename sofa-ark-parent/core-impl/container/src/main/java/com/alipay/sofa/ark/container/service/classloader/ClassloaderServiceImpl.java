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

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classloader Service Implementation
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class ClassloaderServiceImpl implements ClassloaderService {

    private static final ArkLogger                       LOGGER                          = ArkLoggerFactory
                                                                                             .getDefaultLogger();

    private static final String                          JAVA_AGENT_MARK                 = "-javaagent:";

    private static final String                          JAVA_AGENT_OPTION_MARK          = "=";

    private static final String                          ARK_SPI_PACKAGES                = "com.alipay.sofa.ark.spi";

    private static final List<String>                    SUN_REFLECT_GENERATED_ACCESSOR  = new ArrayList<>();

    /* export class and classloader relationship cache */
    private ConcurrentHashMap<String, ClassLoader>       exportClassAndClassloaderMap    = new ConcurrentHashMap<>();

    /* export cache and classloader relationship cache */
    private ConcurrentHashMap<String, List<ClassLoader>> exportResourceAndClassloaderMap = new ConcurrentHashMap<>();

    private ClassLoader                                  jdkClassloader;
    private ClassLoader                                  arkClassloader;
    private ClassLoader                                  systemClassloader;
    private ClassLoader                                  agentClassLoader;

    @Inject
    private PluginManagerService                         pluginManagerService;

    @Inject
    private BizManagerService                            bizManagerService;

    static {
        SUN_REFLECT_GENERATED_ACCESSOR.add("sun.reflect.GeneratedMethodAccessor");
        SUN_REFLECT_GENERATED_ACCESSOR.add("sun.reflect.GeneratedConstructorAccessor");
        SUN_REFLECT_GENERATED_ACCESSOR.add("sun.reflect.GeneratedSerializationConstructorAccessor");
    }

    @Override
    public boolean isSunReflectClass(String className) {
        for (String sunAccessor : SUN_REFLECT_GENERATED_ACCESSOR) {
            if (className.startsWith(sunAccessor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isArkSpiClass(String className) {
        return className.startsWith(ARK_SPI_PACKAGES);
    }

    @Override
    public void prepareExportClassAndResourceCache() {
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            for (String exportIndex : plugin.getExportIndex()) {
                exportClassAndClassloaderMap
                    .putIfAbsent(exportIndex, plugin.getPluginClassLoader());
            }
            for (String resource : plugin.getExportResources()) {
                exportResourceAndClassloaderMap
                    .putIfAbsent(resource, new LinkedList<ClassLoader>());
                exportResourceAndClassloaderMap.get(resource).add(plugin.getPluginClassLoader());
            }
        }
    }

    @Override
    public boolean isClassInImport(String pluginName, String className) {
        Plugin plugin = pluginManagerService.getPluginByName(pluginName);
        AssertUtils.assertNotNull(plugin, "plugin: " + pluginName + " is null");

        for (String importName : plugin.getImportClasses()) {
            if (className.equals(importName)) {
                return true;
            }
        }

        String pkg = ClassUtils.getPackageName(className);
        for (String pattern : plugin.getImportPackages()) {
            if (ClassUtils.isAdaptedToPackagePattern(pkg, pattern)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ClassLoader findExportClassloader(String className) {
        return exportClassAndClassloaderMap.get(className);
    }

    @Override
    public boolean isResourceInImport(String pluginName, String resourceName) {
        Plugin plugin = pluginManagerService.getPluginByName(pluginName);
        AssertUtils.assertNotNull(plugin, "plugin: " + pluginName + " is null");

        for (String importResource : plugin.getImportResources()) {
            if (importResource.equals(resourceName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ClassLoader> findExportResourceClassloadersInOrder(String resourceName) {
        return exportResourceAndClassloaderMap.get(resourceName);
    }

    @Override
    public ClassLoader getJDKClassloader() {
        return jdkClassloader;
    }

    @Override
    public ClassLoader getArkClassloader() {
        return arkClassloader;
    }

    @Override
    public ClassLoader getSystemClassloader() {
        return systemClassloader;
    }

    @Override
    public ClassLoader getAgentClassloader() {
        return agentClassLoader;
    }

    @Override
    public void init() throws ArkException {
        arkClassloader = this.getClass().getClassLoader();
        systemClassloader = ClassLoader.getSystemClassLoader();
        agentClassLoader = createAgentClassLoader();

        ClassLoader extClassloader = systemClassloader;
        while (extClassloader.getParent() != null) {
            extClassloader = extClassloader.getParent();
        }

        List<URL> jdkUrls = new ArrayList<>();
        try {
            String javaHome = System.getProperty("java.home").replace(File.separator + "jre", "");
            URL[] urls = ((URLClassLoader) systemClassloader).getURLs();
            for (URL url : urls) {
                if (url.getPath().startsWith(javaHome)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("Find JDK Url: %s", url));
                    }
                    jdkUrls.add(url);
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Meet exception when parse JDK urls", e);
        }

        jdkClassloader = new JDKDelegateClassloader(jdkUrls.toArray(new URL[0]), extClassloader);
    }

    @Override
    public void dispose() throws ArkException {

    }

    private ClassLoader createAgentClassLoader() throws ArkException {

        List<String> inputArguments = AccessController
            .doPrivileged(new PrivilegedAction<List<String>>() {
                @Override
                public List<String> run() {
                    return ManagementFactory.getRuntimeMXBean().getInputArguments();
                }
            });

        List<URL> agentPaths = new ArrayList<>();
        for (String argument : inputArguments) {

            if (!argument.startsWith(JAVA_AGENT_MARK)) {
                continue;
            }

            argument = argument.substring(JAVA_AGENT_MARK.length());

            try {
                String path = argument.split(JAVA_AGENT_OPTION_MARK)[0];
                URL url = new File(path).toURI().toURL();
                agentPaths.add(url);
            } catch (Throwable e) {
                throw new ArkException("Failed to create java agent classloader", e);
            }

        }

        return new URLClassLoader(agentPaths.toArray(new URL[] {}), null);

    }

    @Override
    public boolean isDeniedImportClass(String bizIdentity, String className) {
        Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
        if (biz == null) {
            return false;
        }

        String pkg = ClassUtils.getPackageName(className);
        for (String pkgPattern : biz.getDenyImportPackages()) {
            if (ClassUtils.isAdaptedToPackagePattern(pkg, pkgPattern)) {
                return true;
            }
        }

        for (String clazz : biz.getDenyImportClasses()) {
            if (clazz.equals(className)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isDeniedImportResource(String bizIdentity, String resourceName) {
        Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
        if (biz == null) {
            return false;
        }

        for (String resource : biz.getDenyImportResources()) {
            if (resource.equals(resourceName)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRECEDENCE;
    }
}