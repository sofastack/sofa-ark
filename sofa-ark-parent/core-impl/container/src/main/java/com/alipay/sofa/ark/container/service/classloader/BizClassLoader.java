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
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;

import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_CLASS_LOADER_HOOK;

/**
 * Ark Biz ClassLoader
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BizClassLoader extends AbstractClasspathClassLoader {

    private String               bizIdentity;
    private BizManagerService    bizManagerService = ArkServiceContainerHolder.getContainer()
                                                       .getService(BizManagerService.class);
    private ClassLoaderHook<Biz> bizClassLoaderHook;
    private AtomicBoolean        isHookLoaded      = new AtomicBoolean(false);

    public BizClassLoader(String bizIdentity, URL[] urls) {
        super(urls);
        this.bizIdentity = bizIdentity;
    }

    @Override
    protected Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {

        // 1. sun reflect related class throw exception directly
        if (classloaderService.isSunReflectClass(name)) {
            throw new ArkLoaderException(
                String
                    .format(
                        "[ArkBiz Loader] %s : can not load class: %s, this class can only be loaded by sun.reflect.DelegatingClassLoader",
                        bizIdentity, name));
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
            bizIdentity, name));
    }

    @Override
    boolean shouldFindExportedClass(String className) {
        return !classloaderService.isDeniedImportClass(bizIdentity, className);
    }

    @Override
    boolean shouldFindExportedResource(String resourceName) {
        return !classloaderService.isDeniedImportResource(bizIdentity, resourceName);
    }

    private void loadBizClassLoaderHook() {
        if (bizClassLoaderHook == null) {
            synchronized (this) {
                if (bizClassLoaderHook == null && isHookLoaded.compareAndSet(false, true)) {
                    bizClassLoaderHook = ArkServiceLoader.loadExtension(ClassLoaderHook.class,
                        BIZ_CLASS_LOADER_HOOK);
                }
            }
        }
    }

    @Override
    protected Class<?> preLoadClass(String className) throws ClassNotFoundException {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.preFindClass(className,
            classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    @Override
    protected Class<?> postLoadClass(String className) throws ClassNotFoundException {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.postFindClass(className,
            classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    @Override
    protected URL preFindResource(String resourceName) {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.preFindResource(resourceName,
            classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    @Override
    protected URL postFindResource(String resourceName) {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.postFindResource(
            resourceName, classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    @Override
    protected Enumeration<URL> preFindResources(String resourceName) {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.preFindResources(
            resourceName, classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    @Override
    protected Enumeration<URL> postFindResources(String resourceName) {
        loadBizClassLoaderHook();
        return bizClassLoaderHook == null ? null : bizClassLoaderHook.postFindResources(
            resourceName, classloaderService, bizManagerService.getBizByIdentity(bizIdentity));
    }

    /**
     * Getter method for property <tt>bizIdentity</tt>.
     *
     * @return property value of bizIdentity
     */
    public String getBizIdentity() {
        return bizIdentity;
    }
}