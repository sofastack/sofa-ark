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
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * A default hook for biz classloader. Trying to post load class by master biz if not found
 *
 * @author bingjie.lbj
 */
@Extension("biz-classloader-hook")
public class DelegateToMasterBizClassLoaderHook implements ClassLoaderHook<Biz> {

    @Override
    public Class<?> preFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                             throws ClassNotFoundException {
        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ClassLoaderService classLoaderService, Biz biz)
                                                                                              throws ClassNotFoundException {
        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz == null || (biz.getBizClassLoader() == bizClassLoader)) {
            return null;
        }
        // if Master Biz contains same class in multi jar, need to check each whether is provided
        Class clazz = bizClassLoader.loadClass(name);
        if (clazz != null) {
            String location = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
            if (biz.isProvided(location)) {
                return clazz;
            }

            try {
                Enumeration<URL> urls = bizClassLoader.getResources(ClassUtils
                    .convertClassNameToResourcePath(name) + ".class");
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    if (biz.isProvided(resourceUrl)) {
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
        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz == null || (biz.getBizClassLoader() == bizClassLoader)) {
            return null;
        }
        try {
            URL resourceUrl = bizClassLoader.getResource(name);
            if (biz.isProvided(resourceUrl)) {
                return resourceUrl;
            }
            return null;
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
        ClassLoader bizClassLoader = ArkClient.getMasterBiz().getBizClassLoader();
        if (biz == null || (biz.getBizClassLoader() == bizClassLoader)) {
            return null;
        }
        try {
            Enumeration<URL> resourceUrls = bizClassLoader.getResources(name);
            List<URL> matchedResourceUrls = new ArrayList<>();
            while (resourceUrls.hasMoreElements()) {
                URL resourceUrl = resourceUrls.nextElement();

                if (biz.isProvided(resourceUrl)) {
                    matchedResourceUrls.add(resourceUrl);
                }
            }
            return Collections.enumeration(matchedResourceUrls);
        } catch (Exception e) {
            return null;
        }
    }
}
