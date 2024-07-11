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
package com.alipay.sofa.ark.spi.service.extension;

import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ArkServiceLoader {
    private static ExtensionLoaderService extensionLoaderService;

    public static <T> T loadExtensionFromArkPlugin(Class<T> interfaceType, String extensionName,
                                                   String pluginName) {
        return extensionLoaderService.getExtensionContributorFromArkPlugin(interfaceType,
            extensionName, pluginName);
    }

    public static <T> T loadExtensionFromArkBiz(Class<T> interfaceType, String extensionName,
                                                String bizIdentity) {
        return extensionLoaderService.getExtensionContributorFromArkBiz(interfaceType,
            extensionName, bizIdentity);
    }

    public static <T> List<T> loadExtensionsFromArkBiz(Class<T> interfaceType, String bizIdentity) {
        return extensionLoaderService
            .getExtensionContributorsFromArkBiz(interfaceType, bizIdentity);
    }

    public static ExtensionLoaderService getExtensionLoaderService() {
        return extensionLoaderService;
    }

    public static void setExtensionLoaderService(ExtensionLoaderService extensionLoaderService) {
        ArkServiceLoader.extensionLoaderService = extensionLoaderService;
    }
}