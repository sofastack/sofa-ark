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
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.loader.jar.Handler;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * Classloader to load Ark Container
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class ContainerClassLoader extends URLClassLoader {

    public ContainerClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            return super.loadClass(name, resolve);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    @Override
    public URL getResource(String name) {
        Handler.setUseFastConnectionExceptions(true);
        try {
            return super.getResource(name);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            return new UseFastConnectionExceptionsEnumeration(super.getResources(name));
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }
}