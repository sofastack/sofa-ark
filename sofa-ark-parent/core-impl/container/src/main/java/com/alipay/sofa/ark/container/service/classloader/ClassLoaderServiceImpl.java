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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.bootstrap.AgentClassLoader;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ClassLoader Service Implementation
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class ClassLoaderServiceImpl implements ClassLoaderService {

    private static final String                     ARK_SPI_PACKAGES                          = "com.alipay.sofa.ark.spi";
    private static final String                     ARK_API_PACKAGES                          = "com.alipay.sofa.ark.api";
    private static final String                     ARK_LOG_PACKAGES                          = "com.alipay.sofa.ark.common.log";
    private static final String                     ARK_EXCEPTION_PACKAGES                    = "com.alipay.sofa.ark.exception";

    private static final List<String>               SUN_REFLECT_GENERATED_ACCESSOR            = new ArrayList<>();

    /* export class and classloader relationship cache */
    private ConcurrentHashMap<String, Plugin>       exportClassAndClassLoaderMap              = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Plugin>       exportNodeAndClassLoaderMap               = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Plugin>       exportStemAndClassLoaderMap               = new ConcurrentHashMap<>();

    /* export cache and classloader relationship cache */
    private ConcurrentHashMap<String, List<Plugin>> exportResourceAndClassLoaderMap           = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Plugin>> exportPrefixStemResourceAndClassLoaderMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<Plugin>> exportSuffixStemResourceAndClassLoaderMap = new ConcurrentHashMap<>();

    private ClassLoader                             jdkClassLoader;
    private ClassLoader                             arkClassLoader;
    private ClassLoader                             systemClassLoader;
    private ClassLoader                             agentClassLoader;

    @Inject
    private PluginManagerService                    pluginManagerService;

    @Inject
    private BizManagerService                       bizManagerService;

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
    public boolean isArkApiClass(String className) {
        return className.startsWith(ARK_API_PACKAGES);
    }

    @Override
    public boolean isArkLogClass(String className) {
        return className.startsWith(ARK_LOG_PACKAGES);
    }

    @Override
    public boolean isArkExceptionClass(String className) {
        return className.startsWith(ARK_EXCEPTION_PACKAGES);
    }

    @Override
    public void prepareExportClassAndResourceCache() {
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            for (String exportIndex : plugin.getExportPackageNodes()) {
                exportNodeAndClassLoaderMap.putIfAbsent(exportIndex, plugin);
            }
            for (String exportIndex : plugin.getExportPackageStems()) {
                exportStemAndClassLoaderMap.putIfAbsent(exportIndex, plugin);
            }
            for (String exportIndex : plugin.getExportClasses()) {
                exportClassAndClassLoaderMap.putIfAbsent(exportIndex, plugin);
            }
            for (String resource : plugin.getExportResources()) {
                exportResourceAndClassLoaderMap.putIfAbsent(resource, new LinkedList<>());
                exportResourceAndClassLoaderMap.get(resource).add(plugin);
            }
            for (String resource : plugin.getExportPrefixResourceStems()) {
                exportPrefixStemResourceAndClassLoaderMap.putIfAbsent(resource, new LinkedList<>());
                exportPrefixStemResourceAndClassLoaderMap.get(resource).add(plugin);
            }
            for (String resource : plugin.getExportSuffixResourceStems()) {
                exportSuffixStemResourceAndClassLoaderMap.putIfAbsent(resource, new LinkedList<>());
                exportSuffixStemResourceAndClassLoaderMap.get(resource).add(plugin);
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
        for (String pattern : plugin.getImportPackageNodes()) {
            if (pkg.equals(pattern)) {
                return true;
            }
        }

        for (String pattern : plugin.getImportPackageStems()) {
            if (pkg.startsWith(pattern)) {
                return true;
            }
        }

        return false;
    }

    public String getExportMode(String className) {
        Plugin plugin = findExportPlugin(className);
        if (plugin == null) {
            return PluginModel.EXPORTMODE_UNKNOWN;
        }

        return plugin.getExportMode();
    }

    @Override
    public ClassLoader findExportClassLoader(String className) {
        Plugin plugin = findExportPlugin(className);
        if (plugin != null) {
            return plugin.getPluginClassLoader();
        } else {
            return null;
        }
    }

    @Override
    public ClassLoader findExportClassLoaderByBiz(Biz biz, String className) {
        BizModel bizModel = (BizModel) biz;
        Plugin plugin = bizModel.getExportClassAndClassLoaderMap().get(className);
        String packageName = ClassUtils.getPackageName(className);
        if (plugin == null) {
            plugin = bizModel.getExportNodeAndClassLoaderMap().get(packageName);
        }
        while (!Constants.DEFAULT_PACKAGE.equals(packageName) && plugin == null) {
            plugin = bizModel.getExportStemAndClassLoaderMap().get(packageName);
            packageName = ClassUtils.getPackageName(packageName);
        }
        if (plugin != null) {
            return plugin.getPluginClassLoader();
        } else {
            return null;
        }
    }

    @Override
    public Plugin findExportPlugin(String className) {
        Plugin plugin = exportClassAndClassLoaderMap.get(className);
        String packageName = ClassUtils.getPackageName(className);
        if (plugin == null) {
            plugin = exportNodeAndClassLoaderMap.get(packageName);
        }
        while (!Constants.DEFAULT_PACKAGE.equals(packageName) && plugin == null) {
            plugin = exportStemAndClassLoaderMap.get(packageName);
            packageName = ClassUtils.getPackageName(packageName);
        }
        return plugin;
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

        for (String importResource : plugin.getImportPrefixResourceStems()) {
            if (resourceName.startsWith(importResource)) {
                return true;
            }
        }

        for (String importResource : plugin.getImportSuffixResourceStems()) {
            if (resourceName.endsWith(importResource)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<ClassLoader> findExportResourceClassLoadersInOrder(String resourceName) {
        List<Plugin> plugins = findExportResourcePluginsInOrder(resourceName);

        if (plugins != null) {
            return plugins.stream().map(Plugin::getPluginClassLoader).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public List<ClassLoader> findExportResourceClassLoadersInOrderByBiz(Biz biz, String resourceName) {
        BizModel bizModel = (BizModel) biz;
        List<Plugin> plugins = findExportResourcePluginsInOrderByBiz(bizModel, resourceName);

        if (plugins != null) {
            return plugins.stream().map(Plugin::getPluginClassLoader).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private List<Plugin> findExportResourcePluginsInOrderByBiz(BizModel bizModel,
                                                               String resourceName) {
        if (bizModel.getExportResourceAndClassLoaderMap().containsKey(resourceName)) {
            return bizModel.getExportResourceAndClassLoaderMap().get(resourceName);
        }

        for (String stemResource : bizModel.getExportPrefixStemResourceAndClassLoaderMap().keySet()) {
            if (resourceName.startsWith(stemResource)) {
                return bizModel.getExportPrefixStemResourceAndClassLoaderMap().get(stemResource);
            }
        }

        for (String stemResource : bizModel.getExportSuffixStemResourceAndClassLoaderMap().keySet()) {
            if (resourceName.endsWith(stemResource)) {
                return bizModel.getExportSuffixStemResourceAndClassLoaderMap().get(stemResource);
            }
        }
        return null;
    }

    private List<Plugin> findExportResourcePluginsInOrder(String resourceName) {
        if (exportResourceAndClassLoaderMap.containsKey(resourceName)) {
            return exportResourceAndClassLoaderMap.get(resourceName);
        }

        for (String stemResource : exportPrefixStemResourceAndClassLoaderMap.keySet()) {
            if (resourceName.startsWith(stemResource)) {
                return exportPrefixStemResourceAndClassLoaderMap.get(stemResource);
            }
        }

        for (String stemResource : exportSuffixStemResourceAndClassLoaderMap.keySet()) {
            if (resourceName.endsWith(stemResource)) {
                return exportSuffixStemResourceAndClassLoaderMap.get(stemResource);
            }
        }
        return null;
    }

    @Override
    public ClassLoader getJDKClassLoader() {
        return jdkClassLoader;
    }

    @Override
    public ClassLoader getArkClassLoader() {
        return arkClassLoader;
    }

    @Override
    public ClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }

    @Override
    public ClassLoader getAgentClassLoader() {
        return agentClassLoader;
    }

    @Override
    public ClassLoader getBizClassLoader(String bizIdentity) {
        Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
        return biz == null ? null : biz.getBizClassLoader();
    }

    @Override
    public ClassLoader getMasterBizClassLoader() {
        Biz biz = ArkClient.getMasterBiz();
        return biz == null ? null : biz.getBizClassLoader();
    }

    @Override
    public ClassLoader getPluginClassLoader(String pluginName) {
        Plugin plugin = pluginManagerService.getPluginByName(pluginName);
        return plugin == null ? null : plugin.getPluginClassLoader();
    }

    @Override
    public void init() throws ArkRuntimeException {
        arkClassLoader = this.getClass().getClassLoader();
        systemClassLoader = ClassLoader.getSystemClassLoader();
        agentClassLoader = createAgentClassLoader();

        ClassLoader extClassLoader = systemClassLoader;
        while (extClassLoader.getParent() != null) {
            extClassLoader = extClassLoader.getParent();
        }
        List<URL> jdkUrls = new ArrayList<>();
        try {
            String javaHome = System.getProperty("java.home").replace(File.separator + "jre", "");
            URL[] urls = ClassLoaderUtils.getURLs(systemClassLoader);
            for (URL url : urls) {
                if (url.getPath().startsWith(javaHome)) {
                    if (ArkLoggerFactory.getDefaultLogger().isDebugEnabled()) {
                        ArkLoggerFactory.getDefaultLogger().debug(
                            String.format("Find JDK Url: %s", url));
                    }
                    jdkUrls.add(url);
                }
            }
        } catch (Throwable e) {
            ArkLoggerFactory.getDefaultLogger().warn("Meet exception when parse JDK urls", e);
        }

        jdkClassLoader = new JDKDelegateClassLoader(jdkUrls.toArray(new URL[0]), extClassLoader);
    }

    @Override
    public void dispose() throws ArkRuntimeException {

    }

    private ClassLoader createAgentClassLoader() throws ArkRuntimeException {
        return new AgentClassLoader(ClassLoaderUtils.getAgentClassPath(), null);
    }

    @Override
    public boolean isDeniedImportClass(String bizIdentity, String className) {
        Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
        if (biz == null) {
            return false;
        }

        for (String pattern : biz.getDenyImportClasses()) {
            if (pattern.equals(className)) {
                return true;
            }
        }

        String pkg = ClassUtils.getPackageName(className);
        for (String pattern : biz.getDenyImportPackageNodes()) {
            if (pkg.equals(pattern)) {
                return true;
            }
        }

        for (String pattern : biz.getDenyImportPackageStems()) {
            if (pkg.startsWith(pattern)) {
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

        for (String resource : biz.getDenyPrefixImportResourceStems()) {
            if (resourceName.startsWith(resource)) {
                return true;
            }
        }

        for (String resource : biz.getDenySuffixImportResourceStems()) {
            if (resourceName.endsWith(resource)) {
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
