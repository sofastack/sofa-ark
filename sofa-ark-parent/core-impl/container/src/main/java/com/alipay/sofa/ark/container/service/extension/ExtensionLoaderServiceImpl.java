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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
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

import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_FILE_DIR;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Singleton
public class ExtensionLoaderServiceImpl implements ExtensionLoaderService {
    private static final Logger  LOGGER               = ArkLoggerFactory.getDefaultLogger();

    private PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
                                                          .getService(PluginManagerService.class);

    private BizManagerService    bizManagerService    = ArkServiceContainerHolder.getContainer()
                                                          .getService(BizManagerService.class);

    @Override
    public <T> T getExtensionContributorFromArkPlugin(Class<T> interfaceType, String extensionName,
                                                      String pluginName) {
        AssertUtils.assertNotNull(interfaceType, "interfaceType can't be null.");
        AssertUtils.assertNotNull(extensionName, "extensionName can't be null.");
        AssertUtils.assertNotNull(pluginName, "pluginName can't be null.");
        Plugin plugin = pluginManagerService.getPluginByName(pluginName);
        AssertUtils.assertNotNull(plugin, "plugin: " + pluginName + " is null");
        return getExtensionContributor(interfaceType, extensionName, plugin,
            plugin.getPluginClassLoader());
    }

    @Override
    public <T> T getExtensionContributorFromArkBiz(Class<T> interfaceType, String extensionName,
                                                   String bizIdentity) {
        AssertUtils.assertNotNull(interfaceType, "interfaceType can't be null.");
        AssertUtils.assertNotNull(extensionName, "extensionName can't be null.");
        AssertUtils.assertNotNull(bizIdentity, "bizIdentity can't be null.");
        Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
        AssertUtils.assertNotNull(biz, "biz: " + bizIdentity + " is null");
        return getExtensionContributor(interfaceType, extensionName, biz, biz.getBizClassLoader());
    }

    public <T, L> T getExtensionContributor(Class<T> interfaceType, String extensionName,
                                            L location, ClassLoader resourceLoader) {
        ExtensionClass<T, L> extensionClass = null;
        try {
            Set<ExtensionClass<T, L>> extensionClassSet = loadExtension(interfaceType,
                extensionName, location, resourceLoader);
            for (ExtensionClass extensionClazz : extensionClassSet) {
                if (extensionClass == null
                    || extensionClass.getPriority() > extensionClazz.getPriority()) {
                    extensionClass = extensionClazz;
                }
            }
        } catch (Throwable throwable) {
            LOGGER.error("Loading extension of interfaceType: {} occurs error {}.", interfaceType,
                throwable);
            throw new ArkRuntimeException(throwable);
        }
        return extensionClass == null ? null : extensionClass.getObject();
    }

    private <I, L> Set<ExtensionClass<I, L>> loadExtension(Class<I> interfaceType,
                                                           String extensionName, L location,
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
                    Class<?> implementClass = null;
                    String clazzName = line.trim();
                    try {
                        implementClass = resourceLoader.loadClass(clazzName);
                    } catch (Exception e) {
                        if (ArkConfigs.isEmbedEnable()
                            && resourceLoader != ArkClient.getMasterBiz().getBizClassLoader()) {
                            implementClass = ArkClient.getMasterBiz().getBizClassLoader()
                                .loadClass(clazzName);
                        } else {
                            throw e;
                        }
                    }
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
                    if (!extensionName.equals(extension.value())) {
                        continue;
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