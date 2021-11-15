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
package com.alipay.sofa.ark.loader;

import junit.framework.TestCase;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bingjie.lbj
 * @since 0.1.0
 */
public class EmbedClassPathArchiveTest extends TestCase {

    public void testGetContainerArchive() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL springbootFatJar = cl.getResource("sample-springboot-fat-biz.jar");
        JarFileArchive jarFileArchive = new JarFileArchive(new File(springbootFatJar.getFile()));
        List<Archive> archives = jarFileArchive.getNestedArchives(this::isNestedArchive);
        List<URL> urls = new ArrayList<>(archives.size());
        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }

        EmbedClassPathArchive archive = new EmbedClassPathArchive(
                "com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication", "main",
                urls.toArray(new URL[] {}));
        assertTrue(archive.getContainerArchive().getUrls().length != 0);
        assertTrue(archive.getConfClasspath().size() != 0);
        assertTrue(archive.getBizArchives().size() == 0);
        assertTrue(archive.getPluginArchives().size() == 1);

        URLClassLoader classLoader = new URLClassLoader(archive.getContainerArchive().getUrls());
        try {
            Class clazz = classLoader.loadClass("com.alipay.sofa.ark.container.ArkContainer");
            assertTrue(clazz != null);
        } catch (Exception e) {
            assertTrue("loadClass class failed ", false);
        }
    }

    protected boolean isNestedArchive(Archive.Entry entry) {
        return entry.isDirectory() ? entry.getName().equals("BOOT-INF/classes/") : entry.getName()
            .startsWith("BOOT-INF/lib/");
    }

}