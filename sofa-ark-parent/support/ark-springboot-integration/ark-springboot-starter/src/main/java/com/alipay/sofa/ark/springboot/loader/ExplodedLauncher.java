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

import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;

public class ExplodedLauncher extends JarLauncher {
    private Archive archive;

    protected ExplodedLauncher() throws Exception {
        this.archive = createArchive0();
    }

    public static void main(String[] args) throws Exception {
        new ExplodedLauncher().launch(args);
    }

    protected void launch(String[] args) throws Exception {
        ClassLoader classLoader = createClassLoader(getClassPathArchives());
        launch(args, getMainClass(), classLoader);
    }

    @Override
    protected String getMainClass() throws Exception {
        Manifest manifest = this.archive.getManifest();
        String mainClass = null;
        if (manifest != null) {
            mainClass = manifest.getMainAttributes().getValue("Start-Class");
        }
        if (mainClass == null) {
            throw new IllegalStateException("No 'Start-Class' manifest entry specified in " + this);
        }
        return mainClass;
    }

    @Override
    protected List<Archive> getClassPathArchives() throws Exception {
        List<Archive> archives = new ArrayList<>(this.archive.getNestedArchives(this::isNestedArchive));
        postProcessClassPathArchives(archives);
        return archives;
    }

    protected static final Archive createArchive0() throws Exception {
        ProtectionDomain protectionDomain = ExplodedLauncher.class.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
        String path = (location != null) ? location.getSchemeSpecificPart() : null;
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        return (root.isDirectory() ? new FastExplodedArchive(root) : new JarFileArchive(root));
    }

    public static class FastExplodedArchive extends ExplodedArchive {
        private File root;

        public FastExplodedArchive(File root) {
            super(root);
            this.root = root;
        }

        @Override
        protected Archive getNestedArchive(Entry entry) throws IOException {
            Archive archive = super.getNestedArchive(entry);
            if (archive instanceof JarFileArchive) {
                return new UrlArchive(new File(root, entry.getName()));
            }
            return archive;
        }
    }

    public static class UrlArchive implements Archive {
        private URL url;

        public UrlArchive(URL url) {
            this.url = url;
        }

        public UrlArchive(File file) throws MalformedURLException {
            this.url = file.toURI().toURL();
        }

        @Override
        public URL getUrl() throws MalformedURLException {
            return this.url;
        }

        @Override
        public Manifest getManifest() throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Iterator<Entry> iterator() {
            List<Entry> entries = new ArrayList<>();
            entries.add(new UrlEntry(this.url));
            return entries.iterator();
        }
    }

    public static class UrlEntry implements Archive.Entry {
        private URL url;

        public UrlEntry(URL url) {
            this.url = url;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public String getName() {
            return url.getFile();
        }
    }

}
