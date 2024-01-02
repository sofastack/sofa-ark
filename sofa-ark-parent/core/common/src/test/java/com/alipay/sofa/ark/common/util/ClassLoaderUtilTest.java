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

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.alipay.sofa.ark.common.util.EnvironmentUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
    public void testGetURLs() {
        ClassLoader urlClassLoader = new URLClassLoader(new URL[] {});
        Assert.assertArrayEquals(((URLClassLoader) urlClassLoader).getURLs(),
            ClassLoaderUtils.getURLs(urlClassLoader));

        ClassLoader appClassLoader = this.getClass().getClassLoader();
        URL[] urls = null;
        if (appClassLoader instanceof URLClassLoader) {
            urls = ((URLClassLoader) appClassLoader).getURLs();
            Assert.assertArrayEquals(urls, ClassLoaderUtils.getURLs(appClassLoader));
        } else {
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(System.getProperty("path.separator"));
            List<URL> classpathURLs = new ArrayList<>();
            for (String classpathEntry : classpathEntries) {
                URL url = null;
                try {
                    url = FileUtils.file(classpathEntry).toURI().toURL();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    throw new ArkRuntimeException("Failed to get urls from " + appClassLoader, e);
                }
                classpathURLs.add(url);
            }
            urls = classpathURLs.toArray(new URL[0]);
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

    @Test
    public void testParseSkyWalkingAgentPath() {
        List<String> mockArguments = new ArrayList<>();
        String workingPath = this.getClass().getClassLoader()
                .getResource("sample-skywalking-agent.jar").getPath();
        mockArguments.add(String.format("-javaagent:%s", workingPath));
        RuntimeMXBean runtimeMXBean = Mockito.mock(RuntimeMXBean.class);
        when(runtimeMXBean.getInputArguments()).thenReturn(mockArguments);

        MockedStatic<ManagementFactory> managementFactoryMockedStatic = Mockito.mockStatic(ManagementFactory.class);
        managementFactoryMockedStatic.when(ManagementFactory::getRuntimeMXBean).thenReturn(runtimeMXBean);

        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        Assert.assertEquals(2, agentUrl.length);

        managementFactoryMockedStatic.close();
    }

    @Test
    public void testEnvironmentUtils() {
        assertNull(getProperty("not_exists_prop"));
        setSystemProperty("not_exists_prop", "aaa");
        assertEquals("aaa", getProperty("not_exists_prop"));
        clearSystemProperty("not_exists_prop");
        assertNull(getProperty("not_exists_prop"));
    }
}
