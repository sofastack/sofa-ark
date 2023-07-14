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
package com.alipay.sofa.ark.common.util;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClassLoaderUtilTest {

    private class MockClassLoader extends ClassLoader {

        private final URLClassPath ucp;

        private MockClassLoader(URL[] urls) {
            ucp = new URLClassPath(urls);
        }

        private URL[] getURLs() {
            return ucp.getURLs();
        }

        private class URLClassPath {
            private ArrayList<URL> path = new ArrayList<>();

            private URLClassPath(URL[] urls) {
                Collections.addAll(path, urls);
            }

            private URL[] getURLs() {
                return this.path.toArray(new URL[0]);
            }
        }
    }

    @Test
    public void testPushContextClassLoader() {
        ClassLoader classLoader = new URLClassLoader(new URL[] {});
        ClassLoaderUtils.pushContextClassLoader(classLoader);
        Assert.assertEquals(classLoader, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void testPopContextClassLoader() {
        ClassLoader classLoader = new URLClassLoader(new URL[] {});
        ClassLoaderUtils.popContextClassLoader(classLoader);
        Assert.assertEquals(classLoader, Thread.currentThread().getContextClassLoader());
    }

    @Test
    @SuppressWarnings({ "restriction", "unchecked" })
    public void testGetURLs() throws NoSuchFieldException, IllegalAccessException {
        ClassLoader urlClassLoader = new URLClassLoader(new URL[] {});
        Assert.assertArrayEquals(((URLClassLoader) urlClassLoader).getURLs(),
            ClassLoaderUtils.getURLs(urlClassLoader));

        ClassLoader appClassLoader = this.getClass().getClassLoader();
        URL[] urls = null;
        if (appClassLoader instanceof URLClassLoader) {
            urls = ((URLClassLoader) appClassLoader).getURLs();
        } else {

            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(null);

            // jdk.internal.loader.ClassLoaders.AppClassLoader.ucp
            Field ucpField = appClassLoader.getClass().getDeclaredField("ucp");
            long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
            Object ucpObject = unsafe.getObject(appClassLoader, ucpFieldOffset);

            // jdk.internal.loader.URLClassPath.path
            Field pathField = ucpField.getType().getDeclaredField("path");
            long pathFieldOffset = unsafe.objectFieldOffset(pathField);
            ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);

            urls = path.toArray(new URL[path.size()]);
        }
        Assert.assertArrayEquals(urls, ClassLoaderUtils.getURLs(appClassLoader));

        URL[] urLs = ClassLoaderUtils.getURLs(null);
        Assert.assertNotNull(urLs);

    }

    @Test
    public void testGetAgentClassPath() {
        List<String> mockArguments = new ArrayList<>();
        String workingPath = this.getClass().getClassLoader()
                .getResource("").getPath();
        mockArguments.add(String.format("-javaagent:%s", workingPath));

        RuntimeMXBean runtimeMXBean = Mockito.mock(RuntimeMXBean.class);
        when(runtimeMXBean.getInputArguments()).thenReturn(mockArguments);

        MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class);
        managementFactoryMockedStatic.when(ManagementFactory::getRuntimeMXBean).thenReturn(runtimeMXBean);

        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        Assert.assertEquals(1, agentUrl.length);

        managementFactoryMockedStatic.close();
    }
}