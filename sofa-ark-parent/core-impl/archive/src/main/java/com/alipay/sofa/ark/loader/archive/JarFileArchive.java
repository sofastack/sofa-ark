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
package com.alipay.sofa.ark.loader.archive;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.loader.data.RandomAccessData.ResourceAccess;
import com.alipay.sofa.ark.spi.archive.Archive;

/**
 * {@link Archive} implementation backed by a {@link JarFile}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class JarFileArchive implements Archive {

    private static final String UNPACK_MARKER = "UNPACK:";

    private static final int    BUFFER_SIZE   = 32 * 1024;

    private final JarFile       jarFile;

    private URL                 url;

    private File                tempUnpackFolder;

    public JarFileArchive(File file) throws IOException {
        this(file, null);
    }

    public JarFileArchive(File file, URL url) throws IOException {
        this(new JarFile(file));
        this.url = url;
    }

    public JarFileArchive(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        if (this.url != null) {
            return this.url;
        }
        return this.jarFile.getUrl();
    }

    @Override
    public Manifest getManifest() throws IOException {
        return this.jarFile.getManifest();
    }

    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
        List<Archive> nestedArchives = new ArrayList<>();
        for (Entry entry : this) {
            if (filter.matches(entry)) {
                nestedArchives.add(getNestedArchive(entry));
            }
        }
        return Collections.unmodifiableList(nestedArchives);
    }

    @Override
    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        return this.jarFile.getInputStream(zipEntry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return new EntryIterator(this.jarFile.entries());
    }

    public Properties getPomProperties() throws IOException {
        return this.jarFile.getPomProperties();
    }

    public Archive getNestedArchive(Entry entry) throws IOException {
        JarEntry jarEntry = ((JarFileEntry) entry).getJarEntry();
        if (jarEntry.getComment() != null && jarEntry.getComment().startsWith(UNPACK_MARKER)) {
            return getUnpackedNestedArchive(jarEntry);
        }
        try {
            JarFile jarFile = this.jarFile.getNestedJarFile(jarEntry);
            return new JarFileArchive(jarFile);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to get nested archive for entry "
                                            + entry.getName(), ex);
        }
    }

    private Archive getUnpackedNestedArchive(JarEntry jarEntry) throws IOException {
        String name = jarEntry.getName();
        if (name.lastIndexOf("/") != -1) {
            name = name.substring(name.lastIndexOf("/") + 1);
        }
        File file = new File(getTempUnpackFolder(), name);
        if (!file.exists() || file.length() != jarEntry.getSize()) {
            unpack(jarEntry, file);
        }
        return new JarFileArchive(file, file.toURI().toURL());
    }

    private File getTempUnpackFolder() {
        if (this.tempUnpackFolder == null) {
            File tempFolder = FileUtils.file(System.getProperty("java.io.tmpdir"));
            this.tempUnpackFolder = createUnpackFolder(tempFolder);
        }
        return this.tempUnpackFolder;
    }

    private File createUnpackFolder(File parent) {
        int attempts = 0;
        while (attempts++ < 1000) {
            String fileName = FileUtils.file(this.jarFile.getName()).getName();
            File unpackFolder = new File(parent, fileName + "-spring-boot-libs-"
                                                 + UUID.randomUUID());
            if (unpackFolder.mkdirs()) {
                return unpackFolder;
            }
        }
        throw new IllegalStateException("Failed to create unpack folder in directory '" + parent
                                        + "'");
    }

    private void unpack(JarEntry entry, File file) throws IOException {
        InputStream inputStream = this.jarFile.getInputStream(entry, ResourceAccess.ONCE);
        try {
            OutputStream outputStream = new FileOutputStream(file);
            try {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } finally {
            inputStream.close();
        }
    }

    @Override
    public String toString() {
        try {
            return getUrl().toString();
        } catch (Exception ex) {
            return "jar archive";
        }
    }

    /**
     * {@link Archive.Entry} iterator implementation backed by {@link JarEntry}.
     */
    private static class EntryIterator implements Iterator<Entry> {

        private final Enumeration<JarEntry> enumeration;

        EntryIterator(Enumeration<JarEntry> enumeration) {
            this.enumeration = enumeration;
        }

        @Override
        public boolean hasNext() {
            return this.enumeration.hasMoreElements();
        }

        @Override
        public Entry next() {
            return new JarFileEntry(this.enumeration.nextElement());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

    }

    /**
     * {@link Archive.Entry} implementation backed by a {@link JarEntry}.
     */
    public static class JarFileEntry implements Entry {

        private final JarEntry jarEntry;

        public JarFileEntry(JarEntry jarEntry) {
            this.jarEntry = jarEntry;
        }

        public JarEntry getJarEntry() {
            return this.jarEntry;
        }

        @Override
        public boolean isDirectory() {
            return this.jarEntry.isDirectory();
        }

        @Override
        public String getName() {
            return this.jarEntry.getName();
        }

    }

}
