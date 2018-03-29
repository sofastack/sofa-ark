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
package com.alipay.sofa.ark.container.tester;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.loader.jar.Handler;

import java.net.URL;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class TestClassLoader extends BizClassLoader {

    private final ClassLoader delegateClassLoader;

    String[]                  packageForTest = { "org.junit", "junit", "org.hamcrest" };

    public TestClassLoader(String bizName, URL[] urls, ClassLoader delegate) {
        super(bizName, urls);
        delegateClassLoader = delegate;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        Handler.setUseFastConnectionExceptions(true);
        try {
            if (isTestClass(name)) {
                return delegateClassLoader.loadClass(name);
            } else {
                return loadClassInternal(name, resolve);
            }
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    private boolean isTestClass(String name) {
        for (String pkg : packageForTest) {
            if (name.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}