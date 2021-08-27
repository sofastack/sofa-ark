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

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.extension.Extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author jinle.xjl
 * @since 1.1.7
 */
@Extension(value = Constants.BIZ_CLASS_LOADER_HOOK, order = 99)
public class TestDefaultBizClassLoaderHook implements ClassLoaderHook<Biz> {
    @Override
    public Class<?> preFindClass(String name, ClassLoaderService classLoaderService, Biz biz) throws ClassNotFoundException {
        if ("defaultA".equals(name)) {
            return ClassDefaultA.class;
        }
        return null;
    }

    @Override
    public Class<?> postFindClass(String name, ClassLoaderService classLoaderService, Biz biz) throws ClassNotFoundException {
        if ("defaultB".equals(name)) {
            return ClassDefaultB.class;
        }
        return null;
    }

    @Override
    public URL preFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {
        return null;
    }

    @Override
    public URL postFindResource(String name, ClassLoaderService classLoaderService, Biz biz) {
        return null;
    }

    @Override
    public Enumeration<URL> preFindResources(String name, ClassLoaderService classLoaderService, Biz biz) throws IOException {
        return null;
    }

    @Override
    public Enumeration<URL> postFindResources(String name, ClassLoaderService classLoaderService, Biz biz) throws IOException {
        return null;
    }

    public class ClassDefaultA {
    }

    public class ClassDefaultB {
    }
}