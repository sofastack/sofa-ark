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
import org.springframework.boot.loader.launch.Archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.Manifest;

import static org.junit.Assert.*;
import static com.alipay.sofa.ark.springboot.loader.JarLauncher.main;
public class CachedLaunchedURLClassLoaderTest {
    private static final Object NO_MANIFEST = new Object();

    private static final Set<String> SKIPPED_NAMES = Set.of(".", "..");

    private static final Comparator<File> entryComparator = Comparator.comparing(File::getAbsolutePath);
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
        List<URL> urls = new ArrayList<>();
        assertNotNull(new JarLauncher().createClassLoader(urls));
    }

    class ExplodedArchive implements Archive {

        private final File rootDirectory;

        private final String rootUriPath;

        private volatile Object manifest;

        /**
         * Create a new {@link org.springframework.boot.loader.launch.ExplodedArchive} instance.
         * @param rootDirectory the root directory
         */
        ExplodedArchive(File rootDirectory) {
            if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
                throw new IllegalArgumentException("Invalid source directory " + rootDirectory);
            }
            this.rootDirectory = rootDirectory;
            this.rootUriPath = this.rootDirectory.toURI().getPath();
        }

        @Override
        public Manifest getManifest() throws IOException {
            Object manifest = this.manifest;
            if (manifest == null) {
                manifest = loadManifest();
                this.manifest = manifest;
            }
            return (manifest != NO_MANIFEST) ? (Manifest) manifest : null;
        }

        private Object loadManifest() throws IOException {
            File file = new File(this.rootDirectory, "META-INF/MANIFEST.MF");
            if (!file.exists()) {
                return NO_MANIFEST;
            }
            try (FileInputStream inputStream = new FileInputStream(file)) {
                return new Manifest(inputStream);
            }
        }
        @Override
        public Set<URL> getClassPathUrls(Predicate<Entry> includeFilter, Predicate<Entry> directorySearchFilter)
                throws IOException {
            Set<URL> urls = new LinkedHashSet<>();
            LinkedList<File> files = new LinkedList<>(listFiles(this.rootDirectory));
            while (!files.isEmpty()) {
                File file = files.poll();
                if (SKIPPED_NAMES.contains(file.getName())) {
                    continue;
                }
                String entryName = file.toURI().getPath().substring(this.rootUriPath.length());
                files.addAll(0, listFiles(file));
            }
            return urls;
        }

        private List<File> listFiles(File file) {
            File[] files = file.listFiles();
            if (files == null) {
                return Collections.emptyList();
            }
            Arrays.sort(files, entryComparator);
            return Arrays.asList(files);
        }

        @Override
        public File getRootDirectory() {
            return this.rootDirectory;
        }

        @Override
        public String toString() {
            return this.rootDirectory.toString();
        }

    }
}
