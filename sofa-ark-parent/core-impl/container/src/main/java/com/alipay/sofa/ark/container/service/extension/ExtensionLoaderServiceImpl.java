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
package com.alipay.sofa.ark.container.service.extension;

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.extension.Extensible;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.ark.spi.service.extension.ExtensionClass;
import com.alipay.sofa.ark.spi.service.extension.ExtensionLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_FILE_DIR;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Singleton
public class ExtensionLoaderServiceImpl implements ExtensionLoaderService {

    private static final ConcurrentHashMap<Class, ConcurrentHashMap<String, ExtensionClass>> EXTENSION_MAP = new ConcurrentHashMap<>();

    private static final Logger                                                              LOGGER        = ArkLoggerFactory
                                                                                                               .getDefaultLogger();

    @Inject
    private PluginManagerService                                                             pluginManagerService;

    @Inject
    private BizManagerService                                                                bizManagerService;

    @Override
    public <T> T getExtensionContributor(Class<T> interfaceType, String extensionName) {
        // delay to load spi & compatible pre version
        return null;
    }

    @Override
    public <T> List<T> getExtensionContributor(Class<T> interfaceType) {
        // delay to load spi & delay to load spi & compatible pre version
        return null;
    }

    @Override
    public <T> T getExtensionContributor(String isolateSpace, Class<T> interfaceType,
                                         String extensionName) {
        ConcurrentHashMap<String, ExtensionClass> extensionClassMap = EXTENSION_MAP
            .get(interfaceType);
        if (extensionClassMap == null) {
            extensionClassMap = loadExtensionStartup(isolateSpace, interfaceType);
        }
        ExtensionClass extensionClass = extensionClassMap.get(isolateSpace + Constants.STRING_COLON
                                                              + extensionName);
        return extensionClass == null ? null : (T) extensionClass.getObject();
    }

    @Override
    public <T> List<T> getExtensionContributor(String isolateSpace, Class<T> interfaceType) {
        ConcurrentHashMap<String, ExtensionClass> extensionClassMap = EXTENSION_MAP
            .get(interfaceType);
        if (extensionClassMap == null) {
            extensionClassMap = loadExtensionStartup(isolateSpace, interfaceType);
        }
        List<ExtensionClass> extensionClassList = new ArrayList<>(extensionClassMap.values());
        Collections.sort(extensionClassList, new OrderComparator());
        List<T> ret = new ArrayList<>();
        for (ExtensionClass extensionClass : extensionClassList) {
            if (extensionClass != null && extensionClass.getDefinedLocation() != null) {
                ret.add((T) extensionClass.getObject());
            }
        }
        return ret;
    }

    /**
     * initialize to loading extension of specified interfaceType
     * @param interfaceType
     */
    private ConcurrentHashMap<String, ExtensionClass> loadExtensionStartup(String isolateSpace,
                                                                           Class<?> interfaceType) {
        ConcurrentHashMap<String, ExtensionClass> extensionClassMap = EXTENSION_MAP
            .get(interfaceType);
        if (extensionClassMap == null) {
            synchronized (ExtensionLoaderServiceImpl.class) {
                extensionClassMap = EXTENSION_MAP.get(interfaceType);
                if (extensionClassMap == null) {
                    try {
                        extensionClassMap = new ConcurrentHashMap<>();
                        // load plugin
                        Set<? extends ExtensionClass<?, Plugin>> extensionPluginClassSet = loadExtensionFromArkPlugins(interfaceType);
                        for (ExtensionClass extensionClass : extensionPluginClassSet) {
                            extensionClassMap.put(isolateSpace + Constants.STRING_COLON
                                                  + extensionClass.getExtension().value(),
                                extensionClass);
                        }
                        // load biz
                        Set<? extends ExtensionClass<?, Biz>> extensionBizClassSet = loadExtensionFromArkBizs(interfaceType);
                        for (ExtensionClass extensionClass : extensionBizClassSet) {
                            extensionClassMap.put(isolateSpace + Constants.STRING_COLON
                                                  + extensionClass.getExtension().value(),
                                extensionClass);
                        }
                        EXTENSION_MAP.put(interfaceType, extensionClassMap);
                    } catch (Throwable throwable) {
                        LOGGER.error("Loading extension of interfaceType: {} occurs error {}.",
                            interfaceType, throwable);
                        throw new ArkRuntimeException(throwable);
                    }
                }
            }
        }
        return extensionClassMap;
    }

    private <I> Set<ExtensionClass<I, Plugin>> loadExtensionFromArkPlugins(Class<I> interfaceType)
                                                                                                  throws Throwable {
        Set<ExtensionClass<I, Plugin>> extensionClassSet = new HashSet<>();
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            extensionClassSet.addAll(loadExtensionFromArkPlugin(interfaceType, plugin));
        }
        return extensionClassSet;
    }

    private <I, L> Set<ExtensionClass<I, Plugin>> loadExtensionFromArkPlugin(Class<I> interfaceType,
                                                                             Plugin plugin)
                                                                                           throws Throwable {
        return loadExtension(interfaceType, plugin, plugin.getPluginClassLoader());
    }

    private <I> Set<ExtensionClass<I, Biz>> loadExtensionFromArkBizs(Class<I> interfaceType)
                                                                                            throws Throwable {
        Set<ExtensionClass<I, Biz>> extensionClassSet = new HashSet<>();
        for (Biz biz : bizManagerService.getBizInOrder()) {
            extensionClassSet.addAll(loadExtensionFromArkBiz(interfaceType, biz));
        }
        return extensionClassSet;
    }

    private <I, L> Set<ExtensionClass<I, Biz>> loadExtensionFromArkBiz(Class<I> interfaceType,
                                                                       Biz biz) throws Throwable {
        return loadExtension(interfaceType, biz, biz.getBizClassLoader());
    }

    private <I, L> Set<ExtensionClass<I, L>> loadExtension(Class<I> interfaceType, L location,
                                                           ClassLoader resourceLoader)
                                                                                      throws Throwable {
        BufferedReader reader = null;
        try {
            Set<ExtensionClass<I, L>> extensionClassSet = new HashSet<>();
            Extensible extensible = interfaceType.getAnnotation(Extensible.class);
            if (extensible == null) {
                throw new ArkRuntimeException(String.format(
                    "Extensible class %s is not annotated by %s.", interfaceType, Extensible.class));
            }
            String fileName = interfaceType.getCanonicalName();
            if (!StringUtils.isEmpty(extensible.file())) {
                fileName = extensible.file();
            }
            Enumeration<URL> enumeration = resourceLoader.getResources(EXTENSION_FILE_DIR
                                                                       + fileName);
            while (enumeration.hasMoreElements()) {
                URL url = enumeration.nextElement();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(
                        "Loading extension of extensible: {} from location: {} and file: {}",
                        interfaceType, location, url);
                }
                reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    ExtensionClass<I, L> extensionClass = new ExtensionClass<>();
                    extensionClass.setDefinedLocation(location);
                    extensionClass.setExtensible(extensible);
                    extensionClass.setInterfaceClass(interfaceType);
                    Class<?> implementClass = resourceLoader.loadClass(line.trim());
                    if (!interfaceType.isAssignableFrom(implementClass)) {
                        throw new ArkRuntimeException(String.format(
                            "Extension implementation class %s is not type of %s.",
                            implementClass.getCanonicalName(), interfaceType.getCanonicalName()));
                    }
                    Extension extension = implementClass.getAnnotation(Extension.class);
                    if (extension == null) {
                        throw new ArkRuntimeException(String.format(
                            "Extension implementation class %s is not annotated by %s.",
                            implementClass, Extension.class));
                    }
                    extensionClass.setExtension(extension);
                    extensionClass.setImplementClass((Class<I>) implementClass);
                    extensionClassSet.add(extensionClass);
                }
            }
            return extensionClassSet;
        } catch (Throwable throwable) {
            LOGGER
                .error("Loading extension files from {} occurs an error {}.", location, throwable);
            throw throwable;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}