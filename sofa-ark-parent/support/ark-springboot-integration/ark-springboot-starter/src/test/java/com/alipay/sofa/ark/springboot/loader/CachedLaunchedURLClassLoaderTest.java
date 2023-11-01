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
package com.alipay.sofa.ark.springboot.loader;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.loader.archive.ExplodedArchive;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import static com.alipay.sofa.ark.springboot.loader.JarLauncher.main;
import static org.junit.Assert.*;

public class CachedLaunchedURLClassLoaderTest {

    private CachedLaunchedURLClassLoader cachedLaunchedURLClassLoader;

    private File                         resourcesDir = new File("src/test/resources/");

    @Before
    public void setUp() throws Exception {
        cachedLaunchedURLClassLoader = new CachedLaunchedURLClassLoader(true, new ExplodedArchive(
            resourcesDir), new URL[] { new URL("file:///" + resourcesDir.getAbsolutePath()) }, this
            .getClass().getClassLoader());
    }

    @Test
    public void testLoadClass() throws Exception {

        Field field = CachedLaunchedURLClassLoader.class.getDeclaredField("classCache");
        field.setAccessible(true);
        assertEquals(0, ((Map) field.get(cachedLaunchedURLClassLoader)).size());

        try {
            cachedLaunchedURLClassLoader.loadClass("a", true);
            assertTrue(false);
        } catch (ClassNotFoundException cnfe) {
        }

        try {
            cachedLaunchedURLClassLoader.loadClass("a", true);
            assertTrue(false);
        } catch (ClassNotFoundException cnfe) {
        }

        assertEquals(CachedLaunchedURLClassLoaderTest.class,
            cachedLaunchedURLClassLoader.loadClass(
                "com.alipay.sofa.ark.springboot.loader.CachedLaunchedURLClassLoaderTest", true));
        assertEquals(CachedLaunchedURLClassLoaderTest.class,
            cachedLaunchedURLClassLoader.loadClass(
                "com.alipay.sofa.ark.springboot.loader.CachedLaunchedURLClassLoaderTest", true));
        assertEquals(1, ((Map) field.get(cachedLaunchedURLClassLoader)).size());
    }

    @Test
    public void testFindResource() throws Exception {

        Field field = CachedLaunchedURLClassLoader.class.getDeclaredField("resourceUrlCache");
        field.setAccessible(true);
        assertEquals(0, ((Map) field.get(cachedLaunchedURLClassLoader)).size());

        assertEquals(null, cachedLaunchedURLClassLoader.findResource("c"));
        assertEquals(null, cachedLaunchedURLClassLoader.findResource("c"));
        assertEquals(null, cachedLaunchedURLClassLoader.findResource("d"));
        assertEquals(2, ((Map) field.get(cachedLaunchedURLClassLoader)).size());
    }

    @Test
    public void testFindResources() throws Exception {

        Field field = CachedLaunchedURLClassLoader.class.getDeclaredField("resourcesUrlCache");
        field.setAccessible(true);
        assertEquals(0, ((Map) field.get(cachedLaunchedURLClassLoader)).size());

        assertEquals(false, cachedLaunchedURLClassLoader.findResources("b").hasMoreElements());
        assertEquals(null, cachedLaunchedURLClassLoader.findResources("b"));
        assertEquals(1, ((Map) field.get(cachedLaunchedURLClassLoader)).size());
    }

    @Test
    public void testJarLauncher() throws Exception {
        try {
            main(new String[] {});
        } catch (Exception e) {
        }
        assertNotNull(new JarLauncher().createClassLoader(new URL[] {}));
    }
}
