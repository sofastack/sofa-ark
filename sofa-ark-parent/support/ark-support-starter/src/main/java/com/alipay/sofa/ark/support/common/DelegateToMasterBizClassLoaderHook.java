package com.alipay.sofa.ark.support.common;

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
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.extension.Extension;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * A default hook for biz classloader. Trying to post load class by master biz if not found
 *
 * @author bingjie.lbj
 */
@Extension("biz-classloader-hook")
public class DelegateToMasterBizClassLoaderHook implements ClassLoaderHook<Biz> {

    private static String CGLIB_FLAG = "CGLIB$$";

    @Override
    public Class<?> preFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                             throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                              throws ClassNotFoundException {
        ClassLoader masterClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz == null || (biz.getBizClassLoader() == masterClassLoader)) {
            return null;
        }
        // The cglib proxy class cannot be delegate to the master, it must be created by the biz's own defineClass
        // see: spring 6, org.springframework.cglib.core.AbstractClassGenerator.generate
        if (name.contains(CGLIB_FLAG)) {
            return null;
        }
        // if Master Biz contains same class in multi jar, need to check each whether is provided
        Class<?> clazz = masterClassLoader.loadClass(name);
        if (clazz != null) {
            if (biz.isDeclared(clazz.getProtectionDomain().getCodeSource().getLocation(), "")) {
                return clazz;
            }

            try {
                String classResourceName = name.replace('.', '/') + ".class";
                Enumeration<URL> urls = masterClassLoader.getResources(classResourceName);
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    if (resourceUrl != null && biz.isDeclared(resourceUrl, classResourceName)) {
                        ArkLoggerFactory.getDefaultLogger().warn(
                            String.format("find class %s from %s in multiple dependencies.", name,
                                resourceUrl.getFile()));
                        return clazz;
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public URL preFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {

        return null;
    }

    @Override
    public URL postFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {
        if (biz == null || (!biz.isDeclaredMode() && shouldSkip(name))) {
            return null;
        }

        ClassLoader masterClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz.getBizClassLoader() == masterClassLoader) {
            return null;
        }
        try {
            URL resourceUrl = masterClassLoader.getResource(name);
            if (resourceUrl != null && biz.isDeclared(resourceUrl, name)) {
                return resourceUrl;
            }

            Enumeration<URL> matchResourceUrls = postFindResources(name, classLoaderService, biz);

            // get the first resource url when match multiple resources
            if (matchResourceUrls != null && matchResourceUrls.hasMoreElements()) {
                return matchResourceUrls.nextElement();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public Enumeration<URL> preFindResources(String name, ClassLoaderService classLoaderService,
                                             Biz biz) throws IOException {
        return null;
    }

    @Override
    public Enumeration<URL> postFindResources(String name, ClassLoaderService classLoaderService,
                                              Biz biz) throws IOException {
        if (biz == null || (!biz.isDeclaredMode() && shouldSkip(name))) {
            return null;
        }
        ClassLoader masterClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz.getBizClassLoader() == masterClassLoader) {
            return null;
        }
        try {
            Enumeration<URL> resourceUrls = masterClassLoader.getResources(name);
            List<URL> matchedResourceUrls = new ArrayList<>();
            while (resourceUrls.hasMoreElements()) {
                URL resourceUrl = resourceUrls.nextElement();

                if (resourceUrl != null && biz.isDeclared(resourceUrl, name)) {
                    matchedResourceUrls.add(resourceUrl);
                }
            }
            return Collections.enumeration(matchedResourceUrls);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean shouldSkip(String resourceName) {
        return !resourceName.endsWith(".class");
    }
}
