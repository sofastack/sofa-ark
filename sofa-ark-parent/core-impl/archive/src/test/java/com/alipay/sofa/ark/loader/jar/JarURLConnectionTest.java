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
package com.alipay.sofa.ark.loader.jar;

import com.alipay.sofa.ark.loader.jar.JarURLConnection.JarEntryName;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;

import static com.alipay.sofa.ark.loader.jar.JarURLConnection.JarEntryName.get;
import static com.alipay.sofa.ark.loader.jar.JarURLConnection.get;
import static org.junit.Assert.*;

public class JarURLConnectionTest {

    private JarURLConnection jarURLConnection;

    private URL              url = this.getClass().getClassLoader()
                                     .getResource("sample-biz-withjar.jar");

    @Before
    public void setUp() throws Exception {
        jarURLConnection = get(url, new JarFile(new File(url.getPath())));
    }

    @Test
    public void testGetJarFileURL() throws IOException {

        assertTrue(jarURLConnection.getJarFileURL().getFile().endsWith("/sample-biz-withjar.jar"));
        assertNull(jarURLConnection.getJarEntry());

        jarURLConnection = get(new URL(
            "file://a/b/sample-biz-withjar.jar!/lib/slf4j-api-1.7.30.jar!/"), new JarFile(new File(
            url.getPath())));

        assertEquals("com.alipay.sofa.ark.loader.data.RandomAccessDataFile$DataInputStream",
            jarURLConnection.getInputStream().getClass().getName());
        assertNull(jarURLConnection.getJarEntry());
        assertEquals("", jarURLConnection.getEntryName());
    }

    @Test
    public void testGetContentLength() throws Exception {
        assertEquals(52949, jarURLConnection.getContentLength());
        Field field = JarURLConnection.class.getDeclaredField("jarEntryName");
        field.setAccessible(true);
        field.set(jarURLConnection, new JarEntryName("!/lib/slf4j-api-1.7.30.jar!/"));
        assertEquals(-1, jarURLConnection.getContentLength());
    }

    @Test
    public void testGetContent() throws IOException {
        assertEquals(JarFile.class, jarURLConnection.getContent().getClass());
        assertEquals("x-java/jar", jarURLConnection.getContentType());
    }

    @Test
    public void testGetLastModified() throws Exception {
        assertEquals(0, jarURLConnection.getLastModified());
        Field field = JarURLConnection.class.getDeclaredField("jarEntryName");
        field.setAccessible(true);
        field.set(jarURLConnection, new JarEntryName("!/lib/slf4j-api-1.7.30.jar!/"));
        assertEquals(0, jarURLConnection.getLastModified());
    }

    @Test
    public void testJarEntryName() {
        JarEntryName jarEntryName = get(url.toString());

        String javaVersion = System.getProperty("java.version");
        if (javaVersion.startsWith("1.8")) {
            assertEquals("content/unknown", jarEntryName.getContentType());
        } else {
            assertEquals("application/java-archive", jarEntryName.getContentType());
        }

        if (javaVersion.startsWith("1.8")) {
            assertEquals("content/unknown", jarEntryName.getContentType());
        } else {
            assertEquals("application/java-archive", jarEntryName.getContentType());
        }
    }
}
