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
package com.alipay.sofa.ark.web.embed.tomcat;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.loader.ResourceEntry;
import org.apache.catalina.loader.WebappClassLoaderBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.catalina.LifecycleState.STARTED;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ArkTomcatEmbeddedWebappClassLoaderTest {

    private ArkTomcatEmbeddedWebappClassLoader arkTomcatEmbeddedWebappClassLoader = new ArkTomcatEmbeddedWebappClassLoader();

    public ArkTomcatEmbeddedWebappClassLoaderTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLoadClass() throws ClassNotFoundException, LifecycleException {

        // 1) Test load class from current class loader's parent.
        assertEquals(String.class,
            arkTomcatEmbeddedWebappClassLoader.loadClass(String.class.getName(), true));

        // 2) Test load class from current class loader's parent, which is null.
        ArkTomcatEmbeddedWebappClassLoader arkTomcatEmbeddedWebappClassLoader2 = new ArkTomcatEmbeddedWebappClassLoader();
        try {
            Field field = WebappClassLoaderBase.class.getDeclaredField("parent");
            field.setAccessible(true);
            field.set(arkTomcatEmbeddedWebappClassLoader2, null);
            arkTomcatEmbeddedWebappClassLoader2.loadClass(String.class.getName(), true);
            assertFalse(true);
        } catch (Exception e) {
            if (e instanceof ClassNotFoundException) {
                // we expected ClassNotFoundException is thrown here, so we do nothing.
            } else {
                throw new RuntimeException(e);
            }
        }

        // 3) Test load class without web class loader class cache.
        try {
            Field field = WebappClassLoaderBase.class.getDeclaredField("state");
            field.setAccessible(true);
            field.set(arkTomcatEmbeddedWebappClassLoader, STARTED);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        WebResourceRoot webResourceRoot = mock(WebResourceRoot.class);
        WebResource webResource = mock(WebResource.class);
        String path = "/" + this.getClass().getName().replace('.', '/') + ".class";
        when(webResourceRoot.getClassLoaderResource(path)).thenReturn(webResource);
        arkTomcatEmbeddedWebappClassLoader.setResources(webResourceRoot);

        assertEquals(this.getClass(),
            arkTomcatEmbeddedWebappClassLoader.loadClass(this.getClass().getName(), true));
        verify(webResourceRoot, times(1)).getClassLoaderResource(path);
        // note: exists always return false to mock resource not found and fallback to parent class loader.
        verify(webResource, times(1)).exists();

        // 4) Test load class from web class loader class cache.
        ConcurrentHashMap<String, ResourceEntry> resourceEntries = new ConcurrentHashMap<>();
        path = "/a/b.class";
        try {
            Field field = WebappClassLoaderBase.class.getDeclaredField("resourceEntries");
            field.setAccessible(true);
            ResourceEntry resourceEntry = new ResourceEntry();
            resourceEntry.loadedClass = this.getClass();
            resourceEntries.put(path, resourceEntry);
            field.set(arkTomcatEmbeddedWebappClassLoader, resourceEntries);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertEquals(this.getClass(), arkTomcatEmbeddedWebappClassLoader.loadClass("a.b", true));

        // 5) Test load class with delegate TRUE.
        arkTomcatEmbeddedWebappClassLoader.setDelegate(true);
        assertEquals(String.class,
            arkTomcatEmbeddedWebappClassLoader.loadClass(String.class.getName(), true));
        assertEquals(this.getClass(), arkTomcatEmbeddedWebappClassLoader.loadClass("a.b", false)); // cover resolve false

        // 6) Test load apache class.
        arkTomcatEmbeddedWebappClassLoader.setDelegate(false);
        assertEquals(WebappClassLoaderBase.class, arkTomcatEmbeddedWebappClassLoader.loadClass(
            WebappClassLoaderBase.class.getName(), true));
        ResourceEntry resourceEntry = new ResourceEntry();
        resourceEntry.loadedClass = String.class;
        resourceEntries.put("/org/apache.class", resourceEntry);
        assertEquals(String.class,
            arkTomcatEmbeddedWebappClassLoader.loadClass("org.apache", false)); // cover resolve false
    }

    @Test(expected = ClassNotFoundException.class)
    public void testLoadClassWithNotFound() throws ClassNotFoundException {
        assertEquals(this.getClass(), arkTomcatEmbeddedWebappClassLoader.loadClass("a.b", true));
    }

    @Test
    public void testOtherMethods() throws IOException, ClassNotFoundException {

        new ArkTomcatEmbeddedWebappClassLoader(this.getClass().getClassLoader());

        assertNull(arkTomcatEmbeddedWebappClassLoader.findResource("aaa"));

        assertEquals(false, arkTomcatEmbeddedWebappClassLoader.findResources("aaa")
            .hasMoreElements());

        arkTomcatEmbeddedWebappClassLoader.addURL(null);

        try {
            Field field = WebappClassLoaderBase.class.getDeclaredField("securityManager");
            field.setAccessible(true);
            field.set(arkTomcatEmbeddedWebappClassLoader, new SecurityManager());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        arkTomcatEmbeddedWebappClassLoader.checkPackageAccess(String.class.getName());
        arkTomcatEmbeddedWebappClassLoader.checkPackageAccess("java"); // cover wrong class name
    }

    @Test(expected = ClassNotFoundException.class)
    public void testCheckPackageAccessFailed() throws IOException, ClassNotFoundException {

        SecurityManager securityManager = mock(SecurityManager.class);
        doThrow(new SecurityException()).when(securityManager).checkPackageAccess("a");

        try {
            Field field = WebappClassLoaderBase.class.getDeclaredField("securityManager");
            field.setAccessible(true);
            field.set(arkTomcatEmbeddedWebappClassLoader, securityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        arkTomcatEmbeddedWebappClassLoader.checkPackageAccess("a.b");
    }
}
