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
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

/**
 * ClassLoader to load Ark Container
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class ContainerClassLoader extends URLClassLoader {
    private static final String ARK_SPI_PACKAGES       = "com.alipay.sofa.ark.spi";
    private static final String ARK_API_PACKAGES       = "com.alipay.sofa.ark.api";
    private static final String ARK_LOG_PACKAGES       = "com.alipay.sofa.ark.common.log";
    private static final String ARK_EXCEPTION_PACKAGES = "com.alipay.sofa.ark.exception";
    private ClassLoader         arkExportClassLoader;

    public ContainerClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ContainerClassLoader(URL[] urls, ClassLoader parent, ClassLoader arkExportClassLoader) {
        super(urls, parent);
        this.arkExportClassLoader = arkExportClassLoader;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Handler.setUseFastConnectionExceptions(true);
        try {
            if ("true".equals(System.getProperty(Constants.EMBED_ENABLE))) {
                Class<?> clazz = resolveArkExportClass(name);
                if (clazz != null) {
                    return clazz;
                }
            }
            return super.loadClass(name, resolve);
        } finally {
            Handler.setUseFastConnectionExceptions(false);
        }
    }

    /**
     * Load ark spi class
     *
     * @param name
     * @return
     */
    protected Class<?> resolveArkExportClass(String name) {
        if (isArkExportClass(name) && arkExportClassLoader != null) {
            try {
                return arkExportClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return null;
    }

    public boolean isArkExportClass(String className) {
        return className.startsWith(ARK_SPI_PACKAGES) || className.startsWith(ARK_API_PACKAGES)
        //               || className.startsWith(ARK_LOG_PACKAGES)
               || className.startsWith(ARK_EXCEPTION_PACKAGES);
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