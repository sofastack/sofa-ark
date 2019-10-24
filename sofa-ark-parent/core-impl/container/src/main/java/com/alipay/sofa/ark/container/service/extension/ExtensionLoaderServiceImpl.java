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
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.service.extension.Extensible;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.ark.spi.service.extension.ExtensionClass;
import com.alipay.sofa.ark.spi.service.extension.ExtensionLoaderService;
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
    private static final Logger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Override
    public <T> T getExtensionContributorInClassloader(Class<T> interfaceType, String extensionName,
                                                      ClassLoader classLoader) {
        Set<ExtensionClass<T>> extensionClassSet = loadExtension(interfaceType, extensionName,
            classLoader);
        ExtensionClass<T> extensionClass = null;
        for (ExtensionClass extensionClazz : extensionClassSet) {
            if (extensionClass == null
                || extensionClass.getPriority() > extensionClazz.getPriority()) {
                extensionClass = extensionClazz;
            }
        }
        return extensionClass == null ? null : extensionClass.getObject();
    }

    private <T> Set<ExtensionClass<T>> loadExtension(Class<T> interfaceType, String extensionName,
                                                     ClassLoader resourceLoader) {
        BufferedReader reader = null;
        try {
            Set<ExtensionClass<T>> extensionClassSet = new HashSet<>();
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
                    LOGGER
                        .debug("Loading extension of extensible: {} file: {}", interfaceType, url);
                }
                reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    ExtensionClass<T> extensionClass = new ExtensionClass<>();
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
                    if (!extensionName.equals(extension.value())) {
                        continue;
                    }
                    extensionClass.setExtension(extension);
                    extensionClass.setImplementClass((Class<T>) implementClass);
                    extensionClassSet.add(extensionClass);
                }
            }
            return extensionClassSet;
        } catch (Throwable throwable) {
            LOGGER.error("Loading extension {} occurs an error.", interfaceType, throwable);
            throw new ArkRuntimeException(throwable);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Throwable t) {
                    throw new ArkRuntimeException(t);
                }
            }
        }
    }
}