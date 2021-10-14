package com.alipay.sofa.ark.container.service.classloader;

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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.common.utils.StringUtil;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author bingjie.lbj
 *
 * @version : DelegateMasterBizClassLoaderHook.java, v 0.1
 */
@Extension("biz-classloader-hook")
public class MasterBizClassLoaderHookAll implements ClassLoaderHook<Biz> {

    public static String[] _UNPROXY_PACKAGE_ROOT = new String[] { "Class.class", "Object.class",
            "com.class", "java/lang/com.class", "com/alipay.class", "Throwable.class",
            "String.class", "Boolean.class", "config/application.properties",
            "config/application.xml", "application.xml", "application.yml", "application.yaml",
            "config/application-default.properties", "config/application-default.xml",
            "config/application-default.yml", "config/application-default.yaml",
            "application-default.properties", "application-default.xml", "application-default.yml",
            "log4j2"                            };

    @Override
    public Class<?> preFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                             throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                              throws ClassNotFoundException {
        if (inUnProxyPackage(name)) {
            return null;
        }
        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        return bizClassLoader.loadClass(name);
    }

    @Override
    public URL preFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {

        return null;
    }

    @Override
    public URL postFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {
        if (inUnProxyResource(name)) {
            return null;
        }

        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        try {
            return bizClassLoader.getResource(name);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Enumeration<URL> preFindResources(String name, ClassLoaderService classLoaderService,
                                             Biz biz) throws IOException {

        return null;

    }

    @Override
    public Enumeration<URL> postFindResources(String name, ClassLoaderService classLoaderService,
                                              Biz biz) throws IOException {
        if (inUnProxyResource(name)) {
            return null;
        }
        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        try {
            return bizClassLoader.getResources(name);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean inUnProxyPackage(String packageName) {
        if (StringUtil.isNotBlank(packageName)) {
            for (String packageRoot : _UNPROXY_PACKAGE_ROOT) {
                if (packageName.startsWith(packageRoot)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean inUnProxyResource(String resourceName) {
        if (!resourceName.endsWith(".class")) {
            return true;
        }
        if (StringUtil.isNotBlank(resourceName)) {
            for (String packageRoot : _UNPROXY_PACKAGE_ROOT) {
                if (resourceName.startsWith(packageRoot)) {
                    return true;
                }
            }
            for (String packageRoot : _UNPROXY_PACKAGE_ROOT) {
                String packageRootUri = packageRoot.replaceAll("\\.", "\\/");
                if (resourceName.startsWith(packageRootUri)) {
                    return true;
                }
            }
        }
        return false;
    }
}
