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

import com.alipay.sofa.ark.loader.data.RandomAccessData;
import com.alipay.sofa.ark.loader.data.RandomAccessData.ResourceAccess;
import com.alipay.sofa.ark.loader.data.RandomAccessDataFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Extended variant of {@link java.util.jar.JarFile} that behaves in the same way but
 * offers the following additional functionality.
 * <ul>
 * <li>A nested {@link JarFile} can be {@link #getNestedJarFile(ZipEntry) obtained} based
 * on any directory entry.</li>
 * <li>A nested {@link JarFile} can be {@link #getNestedJarFile(ZipEntry) obtained} for
 * embedded JAR files (as long as their entry is not compressed).</li>
 * </ul>
 *
 * @author Phillip Webb
 */
public class JarFile extends java.util.jar.JarFile {

    private static final String        MANIFEST_NAME            = "META-INF/MANIFEST.MF";

    private static final String        POM_PROPERTIES           = "pom.properties";

    private static final String        PROTOCOL_HANDLER         = "java.protocol.handler.pkgs";

    private static final String        HANDLERS_PACKAGE         = "com.alipay.sofa.ark.loader";

    private static final AsciiBytes    META_INF                 = new AsciiBytes("META-INF/");

    private static final AsciiBytes    SIGNATURE_FILE_EXTENSION = new AsciiBytes(".SF");

    private final RandomAccessDataFile rootFile;

    private final String               pathFromRoot;

    private final RandomAccessData     data;

    private final JarFileType          type;

    private URL                        url;

    private JarFileEntries             entries;

    private SoftReference<Manifest>    manifest;

    private boolean                    signed;

    /**
     * Create a new {@link JarFile} backed by the specified file.
     * @param file the root jar file
     * @throws IOException if the file cannot be read
     */
    public JarFile(File file) throws IOException {
        this(new RandomAccessDataFile(file));
    }

    /**
     * Create a new {@link JarFile} backed by the specified file.
     * @param file the root jar file
     * @throws IOException if the file cannot be read
     */
    JarFile(RandomAccessDataFile file) throws IOException {
        this(file, "", file, JarFileType.DIRECT);
    }

    /**
     * Private constructor used to create a new {@link JarFile} either directly or from a
     * nested entry.
     * @param rootFile the root jar file
     * @param pathFromRoot the name of this file
     * @param data the underlying data
     * @param type the type of the jar file
     * @throws IOException if the file cannot be read
     */
    private JarFile(RandomAccessDataFile rootFile, String pathFromRoot, RandomAccessData data,
                    JarFileType type) throws IOException {
        this(rootFile, pathFromRoot, data, null, type);
    }

    private JarFile(RandomAccessDataFile rootFile, String pathFromRoot, RandomAccessData data,
                    JarEntryFilter filter, JarFileType type) throws IOException {
        super(rootFile.getFile());
        this.rootFile = rootFile;
        this.pathFromRoot = pathFromRoot;
        CentralDirectoryParser parser = new CentralDirectoryParser();
        this.entries = parser.addVisitor(new JarFileEntries(this, filter));
        parser.addVisitor(centralDirectoryVisitor());
        this.data = parser.parse(data, filter == null);
        this.type = type;
    }

    private CentralDirectoryVisitor centralDirectoryVisitor() {
        return new CentralDirectoryVisitor() {

            @Override
            public void visitStart(CentralDirectoryEndRecord endRecord,
                                   RandomAccessData centralDirectoryData) {
            }

            @Override
            public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
                AsciiBytes name = fileHeader.getName();
                if (name.startsWith(META_INF) && name.endsWith(SIGNATURE_FILE_EXTENSION)) {
                    JarFile.this.signed = true;
                }
            }

            @Override
            public void visitEnd() {
            }

        };
    }

    protected final RandomAccessDataFile getRootJarFile() {
        return this.rootFile;
    }

    RandomAccessData getData() {
        return this.data;
    }

    @Override
    public Manifest getManifest() throws IOException {
        Manifest manifest = (this.manifest == null ? null : this.manifest.get());
        if (manifest == null) {
            if (this.type == JarFileType.NESTED_DIRECTORY) {
                try (JarFile jarFile = new JarFile(this.getRootJarFile())) {
                    manifest = jarFile.getManifest();
                }
            } else {
                try (InputStream inputStream = getInputStream(MANIFEST_NAME, ResourceAccess.ONCE)) {
                    if (inputStream == null) {
                        return null;
                    }
                    manifest = new Manifest(inputStream);
                }
            }
            this.manifest = new SoftReference<>(manifest);
        }
        return manifest;
    }

    public Properties getPomProperties() throws IOException {
        Enumeration<java.util.jar.JarEntry> entries = this.entries();

        Properties p = new Properties();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry jarEntry = entries.nextElement();
            if (jarEntry.getName().endsWith(POM_PROPERTIES)) {
                try (InputStream is = this.getInputStream(jarEntry)) {
                    p.load(is);
                    return p;
                }
            }
        }
        return p;
    }

    @Override
    public Enumeration<java.util.jar.JarEntry> entries() {
        final Iterator<JarEntry> iterator = this.entries.iterator();
        return new Enumeration<java.util.jar.JarEntry>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public java.util.jar.JarEntry nextElement() {
                return iterator.next();
            }

        };
    }

    @Override
    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    public boolean containsEntry(String name) {
        return this.entries.containsEntry(name);
    }

    @Override
    public ZipEntry getEntry(String name) {
        return this.entries.getEntry(name);
    }

    @Override
    public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
        return getInputStream(ze, ResourceAccess.PER_READ);
    }

    public InputStream getInputStream(ZipEntry ze, ResourceAccess access) throws IOException {
        if (ze instanceof JarEntry) {
            return this.entries.getInputStream((JarEntry) ze, access);
        }
        return getInputStream(ze == null ? null : ze.getName(), access);
    }

    InputStream getInputStream(String name, ResourceAccess access) throws IOException {
        return this.entries.getInputStream(name, access);
    }

    /**
     * Return a nested {@link JarFile} loaded from the specified entry.
     * @param entry the zip entry
     * @return a {@link JarFile} for the entry
     * @throws IOException if the nested jar file cannot be read
     */
    public synchronized JarFile getNestedJarFile(final ZipEntry entry) throws IOException {
        return getNestedJarFile((JarEntry) entry);
    }

    /**
     * Return a nested {@link JarFile} loaded from the specified entry.
     * @param entry the zip entry
     * @return a {@link JarFile} for the entry
     * @throws IOException if the nested jar file cannot be read
     */
    public synchronized JarFile getNestedJarFile(JarEntry entry) throws IOException {
        try {
            return createJarFileFromEntry(entry);
        } catch (Exception ex) {
            throw new IOException("Unable to open nested jar file '" + entry.getName() + "'", ex);
        }
    }

    private JarFile createJarFileFromEntry(JarEntry entry) throws IOException {
        if (entry.isDirectory()) {
            return createJarFileFromDirectoryEntry(entry);
        }
        return createJarFileFromFileEntry(entry);
    }

    private JarFile createJarFileFromDirectoryEntry(JarEntry entry) throws IOException {
        final AsciiBytes sourceName = new AsciiBytes(entry.getName());
        JarEntryFilter filter = new JarEntryFilter() {

            @Override
            public AsciiBytes apply(AsciiBytes name) {
                if (name.startsWith(sourceName) && !name.equals(sourceName)) {
                    return name.substring(sourceName.length());
                }
                return null;
            }

        };
        return new JarFile(this.rootFile, this.pathFromRoot + "!/"
                                          + entry.getName().substring(0, sourceName.length() - 1),
            this.data, filter, JarFileType.NESTED_DIRECTORY);
    }

    private JarFile createJarFileFromFileEntry(JarEntry entry) throws IOException {
        if (entry.getMethod() != ZipEntry.STORED) {
            throw new IllegalStateException(
                "Unable to open nested entry '" + entry.getName()
                        + "'. It has been compressed and nested "
                        + "jar files must be stored without compression. Please check the "
                        + "mechanism used to create your executable jar file");
        }
        RandomAccessData entryData = this.entries.getEntryData(entry.getName());
        return new JarFile(this.rootFile, this.pathFromRoot + "!/" + entry.getName(), entryData,
            JarFileType.NESTED_JAR);
    }

    @Override
    public int size() {
        return (int) this.data.getSize();
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.rootFile.close();
    }

    /**
     * Return a URL that can be used to access this JAR file. NOTE: the specified URL
     * cannot be serialized and or cloned.
     * @return the URL
     * @throws MalformedURLException if the URL is malformed
     */
    public URL getUrl() throws MalformedURLException {
        if (this.url == null) {
            Handler handler = new Handler(this);
            String file = this.rootFile.getFile().toURI() + this.pathFromRoot + "!/";
            file = file.replace("file:////", "file://"); // Fix UNC paths
            this.url = new URL("jar", "", -1, file, handler);

        }
        return this.url;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public String getName() {
        String str = this.rootFile.getFile().toString();
        StringBuilder sb = new StringBuilder(str.length() + this.pathFromRoot.length());
        return sb.append(str).append(this.pathFromRoot).toString();
    }

    boolean isSigned() {
        return this.signed;
    }

    void setupEntryCertificates(JarEntry entry) {
        // Fallback to JarInputStream to obtain certificates, not fast but hopefully not
        // happening that often.
        try {
            JarInputStream inputStream = new JarInputStream(getData().getInputStream(
                ResourceAccess.ONCE));
            try {
                java.util.jar.JarEntry certEntry = inputStream.getNextJarEntry();
                while (certEntry != null) {
                    inputStream.closeEntry();
                    if (entry.getName().equals(certEntry.getName())) {
                        setCertificates(entry, certEntry);
                    }
                    setCertificates(getJarEntry(certEntry.getName()), certEntry);
                    certEntry = inputStream.getNextJarEntry();
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void setCertificates(JarEntry entry, java.util.jar.JarEntry certEntry) {
        if (entry != null) {
            entry.setCertificates(certEntry);
        }
    }

    public void clearCache() {
        this.entries.clearCache();
    }

    protected String getPathFromRoot() {
        return this.pathFromRoot;
    }

    JarFileType getType() {
        return this.type;
    }

    /**
     * Register a {@literal 'java.protocol.handler.pkgs'} property so that a
     * {@link URLStreamHandler} will be located to deal with jar URLs.
     */
    public static void registerUrlProtocolHandler() {
        String handlers = System.getProperty(PROTOCOL_HANDLER, "");
        System.setProperty(PROTOCOL_HANDLER, ("".equals(handlers) ? HANDLERS_PACKAGE
            : handlers + "|" + HANDLERS_PACKAGE));
        resetCachedUrlHandlers();
    }

    /**
     * Reset any cached handlers just in case a jar protocol has already been used. We
     * reset the handler by trying to set a null {@link URLStreamHandlerFactory} which
     * should have no effect other than clearing the handlers cache.
     */
    private static void resetCachedUrlHandlers() {
        try {
            URL.setURLStreamHandlerFactory(null);
        } catch (Error ex) {
            // Ignore
        }
    }

    /**
     * The type of a {@link JarFile}.
     */
    enum JarFileType {
        DIRECT, NESTED_DIRECTORY, NESTED_JAR
    }

}
