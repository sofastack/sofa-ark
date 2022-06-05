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
package com.alipay.sofa.ark.dynamic.loader;

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.spi.constant.Constants;
import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

/**
 * Load Fat Jar
 *
 * @author hanyue
 * @version : TestClassloader.java, v 0.1 2022年05月20日 上午9:59 hanyue Exp $
 */
public class TestClassloader extends LaunchedURLClassLoader {

    private final static String[] packageForTest = {
                                                 // Junit
            "org.junit", "junit", "org.hamcrest",
            // TestNG
            "org.testng", "com.beust.jcommander", "bsh",
            // mockito
            "org.mockito", "org.jmockit"        };

    private final ClassLoader     delegateClassLoader;

    private List<String>          delegateClassToAppClassLoader;

    private List<String>          delegateClassToTestClassLoader;

    private final URLClassLoader  rootClassLoader;

    /**
     * Instantiates a new Test classloader.
     *
     * @param exploded            the exploded
     * @param rootArchive         the root archive
     * @param urls                the urls
     * @param parent              the parent
     * @param delegateClassLoader the delegate class loader
     * @param root                the root
     * @throws MalformedURLException the malformed url exception
     */
    public TestClassloader(boolean exploded, Archive rootArchive, URL[] urls, ClassLoader parent,
                           ClassLoader delegateClassLoader, File root) throws MalformedURLException {
        super(exploded, rootArchive, urls, parent);
        this.delegateClassLoader = delegateClassLoader;
        rootClassLoader = new URLClassLoader(new URL[] { root.toURL() }, null);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isDelegateToAppClassLoader(ClassUtils.getPackageName(name))) {
            try {
                return delegateClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new ArkLoaderException(String.format(
                    "[TestClass Loader]: can not load class: %s", name));
            }
        } else {
            return super.loadClass(name, resolve);
        }
    }

    @Override
    public URL getResource(String name) {
        URL resource = super.getResource(name);
        if (resource != null) {
            return resource;
        }

        // Make sure all resource be load
        return rootClassLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        Enumeration<URL> resources = super.getResources(name);
        if (resources.hasMoreElements()) {
            return resources;
        }

        // Make sure all resource be load
        return rootClassLoader.getResources(name);
    }

    @Override
    public URL findResource(String name) {
        URL resource = super.findResource(name);
        if (resource != null) {
            return resource;
        }

        // Make sure all resource be load
        return rootClassLoader.findResource(name);
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Enumeration<URL> resources = super.findResources(name);
        if (resources.hasMoreElements()) {
            return resources;
        }

        // Make sure all resource be load
        return rootClassLoader.findResources(name);
    }

    private boolean isDelegateToAppClassLoader(String name) {
        if (delegateClassToAppClassLoader == null) {
            String classes = EnvironmentUtils.getProperty(
                Constants.FORCE_DELEGATE_TO_APP_CLASSLOADER, Constants.EMPTY_STR);
            delegateClassToAppClassLoader = Arrays.asList(classes.split(Constants.COMMA_SPLIT));
        }
        if (delegateClassToTestClassLoader == null) {
            String classes = EnvironmentUtils.getProperty(
                Constants.FORCE_DELEGATE_TO_TEST_CLASSLOADER, Constants.EMPTY_STR);
            delegateClassToTestClassLoader = Arrays.asList(classes.split(Constants.COMMA_SPLIT));
        }

        for (String pkg : delegateClassToAppClassLoader) {
            if (!StringUtils.isEmpty(pkg) && name.startsWith(pkg)) {
                return true;
            }
        }

        for (String pkg : delegateClassToTestClassLoader) {
            if (!StringUtils.isEmpty(pkg) && name.startsWith(pkg)) {
                return false;
            }
        }

        for (String pkg : packageForTest) {
            if (name.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}