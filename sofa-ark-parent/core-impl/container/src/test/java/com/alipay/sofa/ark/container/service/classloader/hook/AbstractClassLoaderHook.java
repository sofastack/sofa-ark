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
package com.alipay.sofa.ark.container.service.classloader.hook;

import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.container.service.classloader.CompoundEnumeration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class AbstractClassLoaderHook<T> implements ClassLoaderHook<T> {
    @Override
    public Class<?> preFindClass(String name, ClassLoaderService classLoaderService, T t) {
        if ("A.A".equals(name)) {
            return TestBizClassLoaderHook.ClassA.class;
        }
        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ClassLoaderService classLoaderService, T t) {
        return TestBizClassLoaderHook.ClassB.class;
    }

    @Override
    public URL preFindResource(String name, ClassLoaderService classLoaderService, T t) {
        if ("R1".equals(name)) {
            return classLoaderService.getArkClassLoader().getResource(
                "pluginA_export_resource1.xml");
        }
        return null;
    }

    @Override
    public URL postFindResource(String name, ClassLoaderService classLoaderService, T t) {
        if ("any".equals(name)) {
            return classLoaderService.getArkClassLoader().getResource(
                "pluginA_export_resource2.xml");
        }
        return null;
    }

    @Override
    public Enumeration<URL> preFindResources(String name, ClassLoaderService classLoaderService, T t)
                                                                                                     throws IOException {
        if ("R2".equals(name)) {
            return classLoaderService.getArkClassLoader().getResources("sample-biz.jar");
        }
        List<Enumeration<URL>> enumerationList = new ArrayList<>();
        return new CompoundEnumeration<>(enumerationList.toArray(new Enumeration[] {}));
    }

    @Override
    public Enumeration<URL> postFindResources(String name, ClassLoaderService classLoaderService,
                                              T t) throws IOException {
        if ("any".equals(name)) {
            return classLoaderService.getArkClassLoader().getResources("sample-plugin.jar");
        }
        List<Enumeration<URL>> enumerationList = new ArrayList<>();
        return new CompoundEnumeration<>(enumerationList.toArray(new Enumeration[] {}));
    }

    public static class ClassA {
    }

    public static class ClassB {
    }
}