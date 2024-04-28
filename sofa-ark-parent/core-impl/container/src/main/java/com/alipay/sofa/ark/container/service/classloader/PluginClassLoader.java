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

import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_CLASS_LOADER_HOOK;

/**
 * Ark Plugin ClassLoader
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginClassLoader extends AbstractClasspathClassLoader {

    private String                  pluginName;
    private ClassLoaderHook<Plugin> pluginClassLoaderHook;
    private AtomicBoolean           isHookLoaded         = new AtomicBoolean(false);
    private AtomicBoolean           skipLoadHook         = new AtomicBoolean(false);
    private PluginManagerService    pluginManagerService = ArkServiceContainerHolder
                                                             .getContainer()
                                                             .getService(PluginManagerService.class);
    private final Object            lock                 = new Object();

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public PluginClassLoader(String pluginName, URL[] urls) {
        super(urls);
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }

    @Override
    protected Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {
        Class<?> clazz = null;

        // 0. sun reflect related class throw exception directly
        if (classloaderService.isSunReflectClass(name)) {
            throw new ArkLoaderException(
                String
                    .format(
                        "[ArkPlugin Loader] %s : can not load class: %s, this class can only be loaded by sun.reflect.DelegatingClassLoader",
                        pluginName, name));
        }

        // 1. findLoadedClass
        if (clazz == null) {
            clazz = findLoadedClass(name);
        }

        // 2. JDK related class
        if (clazz == null) {
            clazz = resolveJDKClass(name);
        }

        // 3. Ark Spi class
        if (clazz == null) {
            clazz = resolveArkClass(name);
        }

        // 4. pre find class
        if (clazz == null) {
            clazz = preLoadClass(name);
        }

        // 5. Import class export by other plugins
        if (clazz == null) {
            clazz = resolveExportClass(name);
        }

        // 6. Plugin classpath class
        if (clazz == null) {
            clazz = resolveLocalClass(name);
        }

        // 7. Java Agent ClassLoader for agent problem
        if (clazz == null) {
            clazz = resolveJavaAgentClass(name);
        }

        // 8. Post find class
        if (clazz == null) {
            clazz = postLoadClass(name);
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
    boolean shouldFindExportedClass(String className) {
        return classloaderService.isClassInImport(pluginName, className);
    }

    @Override
    boolean shouldFindExportedResource(String resourceName) {
        return classloaderService.isResourceInImport(pluginName, resourceName);
    }

    private void loadPluginClassLoaderHook() {
        if (!skipLoadHook.get()) {
            synchronized (lock) {
                if (isHookLoaded.compareAndSet(false, true)) {
                    pluginClassLoaderHook = ArkServiceLoader.loadExtensionFromArkPlugin(
                        ClassLoaderHook.class, PLUGIN_CLASS_LOADER_HOOK, pluginName);
                    skipLoadHook.set(true);
                }
            }
        }
    }

    @Override
    protected Class<?> preLoadClass(String className) throws ArkLoaderException {
        try {
            loadPluginClassLoaderHook();
            return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.preFindClass(
                className, classloaderService, pluginManagerService.getPluginByName(pluginName));
        } catch (Throwable throwable) {
            throw new ArkLoaderException(String.format(
                "Pre find class %s occurs an error via plugin ClassLoaderHook: %s.", className,
                pluginClassLoaderHook), throwable);
        }
    }

    @Override
    protected Class<?> postLoadClass(String className) throws ArkLoaderException {
        try {
            loadPluginClassLoaderHook();
            return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.postFindClass(
                className, classloaderService, pluginManagerService.getPluginByName(pluginName));
        } catch (Throwable throwable) {
            throw new ArkLoaderException(String.format(
                "Post find class %s occurs an error via plugin ClassLoaderHook: %s.", className,
                pluginClassLoaderHook), throwable);
        }
    }

    @Override
    protected URL preFindResource(String resourceName) {
        loadPluginClassLoaderHook();
        return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.preFindResource(
            resourceName, classloaderService, pluginManagerService.getPluginByName(pluginName));
    }

    @Override
    protected URL postFindResource(String resourceName) {
        loadPluginClassLoaderHook();
        return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.postFindResource(
            resourceName, classloaderService, pluginManagerService.getPluginByName(pluginName));
    }

    @Override
    protected Enumeration<URL> preFindResources(String resourceName) throws IOException {
        loadPluginClassLoaderHook();
        return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.preFindResources(
            resourceName, classloaderService, pluginManagerService.getPluginByName(pluginName));
    }

    @Override
    protected Enumeration<URL> postFindResources(String resourceName) throws IOException {
        loadPluginClassLoaderHook();
        return pluginClassLoaderHook == null ? null : pluginClassLoaderHook.postFindResources(
            resourceName, classloaderService, pluginManagerService.getPluginByName(pluginName));
    }
}
