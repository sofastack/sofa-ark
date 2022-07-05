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
package com.alipay.sofa.ark.tools;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.*;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * Writes JAR content, ensuring valid directory entries are always create and duplicate
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
public class JarWriter implements LoaderClassesWriter {

    private static final String   NESTED_ARCHIVE_LOADER_JAR               = "sofa-ark-archive";
    private static final String   NESTED_SPI_LOADER_JAR                   = "sofa-ark-spi";
    private static final String   NESTED_COMMON_LOADER_JAR                = "sofa-ark-common";
    private static final String   NESTED_ARCHIVE_LOADER_CLASS_PREFIX      = "com/alipay/sofa/ark/bootstrap";
    private static final String   NESTED_ARCHIVE_BOOTSTRAP_CLASS_PREFIX   = "com/alipay/sofa/ark/loader";
    private static final String   NESTED_SPI_ARCHIVE_LOADER_CLASS_PREFIX  = "com/alipay/sofa/ark/spi/archive";
    private static final String   NESTED_ARCHIVE_STRING_UTIL_CLASS_PREFIX = "com/alipay/sofa/ark/common/util/StringUtils";
    private static final String   NESTED_ARCHIVE_ASSERT_UTIL_CLASS_PREFIX = "com/alipay/sofa/ark/common/util/AssertUtils";
    private static final String   NESTED_SPI_CONSTANT_CLASS_PREFIX        = "com/alipay/sofa/ark/spi/constant";

    private static final int      BUFFER_SIZE                             = 32 * 1024;

    private final JarOutputStream jarOutput;

    private final Set<String>     writtenEntries                          = new HashSet<>();

    /**
     * Create a new {@link JarWriter} instance.
     *
     * @param file         the file to write
     * @throws IOException           if the file cannot be opened
     * @throws FileNotFoundException if the file cannot be found
     */
    public JarWriter(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        this.jarOutput = new JarOutputStream(fileOutputStream);
    }

    /**
     * Write the specified manifest.
     *
     * @param manifest the manifest to write
     * @throws IOException of the manifest cannot be written
     */
    public void writeManifest(final Manifest manifest) throws IOException {
        JarEntry entry = new JarEntry("META-INF/MANIFEST.MF");
        writeEntry(entry, new EntryWriter() {
            @Override
            public void write(OutputStream outputStream) throws IOException {
                manifest.write(outputStream);
            }
        });
    }

    public void writeMarkEntry() throws IOException {
        String str = "a mark file included in sofa-ark module.";
        writeEntry(Constants.ARK_BIZ_MARK_ENTRY,
            new ByteArrayInputStream(str.toString().getBytes()));
    }

    /**
     * Write all entries from the specified jar file.
     *
     * @param jarFile the source jar file
     * @throws IOException if the entries cannot be written
     */
    public void writeEntries(JarFile jarFile) throws IOException {
        this.writeEntries(jarFile, new IdentityEntryTransformer());
    }

    public void writeBootstrapEntry(JarFile arkContainerJar) throws IOException {
        Enumeration<JarEntry> entries = arkContainerJar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().contains(NESTED_ARCHIVE_LOADER_JAR)
                || entry.getName().contains(NESTED_SPI_LOADER_JAR)
                || entry.getName().contains(NESTED_COMMON_LOADER_JAR)) {
                JarInputStream inputStream = new JarInputStream(new BufferedInputStream(
                    arkContainerJar.getInputStream(entry)));
                writeLoaderClasses(inputStream);
            }
        }
    }

    public void writeEntries(JarFile jarFile, EntryTransformer entryTransformer) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            ZipHeaderPeekInputStream inputStream = new ZipHeaderPeekInputStream(
                jarFile.getInputStream(entry));
            try {
                if (inputStream.hasZipHeader() && entry.getMethod() != ZipEntry.STORED) {
                    new CrcAndSize(inputStream).setupStoredEntry(entry);
                    inputStream.close();
                    inputStream = new ZipHeaderPeekInputStream(jarFile.getInputStream(entry));
                }
                EntryWriter entryWriter = new InputStreamEntryWriter(inputStream, true);
                JarEntry transformedEntry = entryTransformer.transform(entry);
                if (transformedEntry != null) {
                    writeEntry(transformedEntry, entryWriter);
                }
            } finally {
                inputStream.close();
            }
        }
    }

    /**
         * Writes an entry. The {@code inputStream} is closed once the entry has been written
         *
         * @param entryName   The name of the entry
         * @param inputStream The stream from which the entry's data can be read
         * @throws IOException if the write fails
         */
    @Override
    public void writeEntry(String entryName, InputStream inputStream) throws IOException {
        JarEntry entry = new JarEntry(entryName);
        writeEntry(entry, new InputStreamEntryWriter(inputStream, true));
    }

    /**
     * Write a nested library.
     *
     * @param destination the destination of the library
     * @param library     the library
     * @throws IOException if the write fails
     */
    public void writeNestedLibrary(String destination, Library library) throws IOException {
        File file = library.getFile();
        JarEntry entry = new JarEntry(destination + library.getName());
        entry.setTime(getNestedLibraryTime(file));
        if (library.isUnpackRequired()) {
            entry.setComment("UNPACK:" + FileUtils.sha1Hash(file));
        }
        new CrcAndSize(file).setupStoredEntry(entry);
        writeEntry(entry, new InputStreamEntryWriter(new FileInputStream(file), true));
    }

    private long getNestedLibraryTime(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    return entry.getTime();
                }
            }
        } catch (Exception ex) {
            // Ignore and just use the source file timestamp
        }
        return file.lastModified();
    }

    /**
     *
     * @param jarInputStream the inputStream of the resource containing the loader classes
     * to be written
     * @throws IOException
     */
    @Override
    public void writeLoaderClasses(JarInputStream jarInputStream) throws IOException {
        JarEntry entry;
        while ((entry = jarInputStream.getNextJarEntry()) != null) {
            if (entry.getName().endsWith(".class")
                && (entry.getName().contains(NESTED_SPI_ARCHIVE_LOADER_CLASS_PREFIX)
                    || entry.getName().contains(NESTED_ARCHIVE_BOOTSTRAP_CLASS_PREFIX)
                    || entry.getName().contains(NESTED_ARCHIVE_LOADER_CLASS_PREFIX)
                    || entry.getName().contains(NESTED_ARCHIVE_STRING_UTIL_CLASS_PREFIX)
                    || entry.getName().contains(NESTED_ARCHIVE_ASSERT_UTIL_CLASS_PREFIX) || entry
                    .getName().contains(NESTED_SPI_CONSTANT_CLASS_PREFIX))) {
                writeEntry(entry, new InputStreamEntryWriter(jarInputStream, false));
            }
        }
        jarInputStream.close();
    }

    /**
     * Close the writer.
     *
     * @throws IOException if the file cannot be closed
     */
    public void close() throws IOException {
        this.jarOutput.close();
    }

    /**
     * Perform the actual write of a {@link JarEntry}. All other {@code write} method
     * delegate to this one.
     *
     * @param entry       the entry to write
     * @param entryWriter the entry writer or {@code null} if there is no content
     * @throws IOException in case of I/O errors
     */
    private void writeEntry(JarEntry entry, EntryWriter entryWriter) throws IOException {
        String parent = entry.getName();
        if (parent.endsWith("/")) {
            parent = parent.substring(0, parent.length() - 1);
        }
        if (parent.lastIndexOf("/") != -1) {
            parent = parent.substring(0, parent.lastIndexOf("/") + 1);
            if (parent.length() > 0) {
                writeEntry(new JarEntry(parent), null);
            }
        }

        if (this.writtenEntries.add(entry.getName())) {
            this.jarOutput.putNextEntry(entry);
            if (entryWriter != null) {
                entryWriter.write(this.jarOutput);
            }
            this.jarOutput.closeEntry();
        }
    }

    /**
     * Interface used to write jar entry date.
     */
    private interface EntryWriter {

        /**
         * Write entry data to the specified output stream.
         *
         * @param outputStream the destination for the data
         * @throws IOException in case of I/O errors
         */
        void write(OutputStream outputStream) throws IOException;

    }

    /**
     * {@link EntryWriter} that writes content from an {@link InputStream}.
     */
    private static class InputStreamEntryWriter implements EntryWriter {

        private final InputStream inputStream;

        private final boolean     close;

        InputStreamEntryWriter(InputStream inputStream, boolean close) {
            this.inputStream = inputStream;
            this.close = close;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = this.inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            if (this.close) {
                this.inputStream.close();
            }
        }

    }

    /**
     * {@link InputStream} that can peek ahead at zip header bytes.
     */
    private static class ZipHeaderPeekInputStream extends FilterInputStream {

        private static final byte[]  ZIP_HEADER = new byte[] { 0x50, 0x4b, 0x03, 0x04 };

        private final byte[]         header;

        private ByteArrayInputStream headerStream;

        protected ZipHeaderPeekInputStream(InputStream in) throws IOException {
            super(in);
            this.header = new byte[4];
            int len = in.read(this.header);
            this.headerStream = new ByteArrayInputStream(this.header, 0, len);
        }

        @Override
        public int read() throws IOException {
            int read = (this.headerStream == null ? -1 : this.headerStream.read());
            if (read != -1) {
                this.headerStream = null;
                return read;
            }
            return super.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = (this.headerStream == null ? -1 : this.headerStream.read(b, off, len));
            if (read != -1) {
                this.headerStream = null;
                return read;
            }
            return super.read(b, off, len);
        }

        public boolean hasZipHeader() {
            return Arrays.equals(this.header, ZIP_HEADER);
        }

    }

    /**
     * Data holder for CRC and Size.
     */
    private static class CrcAndSize {

        private final CRC32 crc = new CRC32();

        private long        size;

        CrcAndSize(File file) throws IOException {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                load(inputStream);
            } finally {
                inputStream.close();
            }
        }

        CrcAndSize(InputStream inputStream) throws IOException {
            load(inputStream);
        }

        private void load(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                this.crc.update(buffer, 0, bytesRead);
                this.size += bytesRead;
            }
        }

        public void setupStoredEntry(JarEntry entry) {
            entry.setSize(this.size);
            entry.setCompressedSize(this.size);
            entry.setCrc(this.crc.getValue());
            entry.setMethod(ZipEntry.STORED);
        }

    }

    /**
     * An {@code EntryTransformer} enables the transformation of {@link JarEntry jar
     * entries} during the writing process.
     */
    public interface EntryTransformer {
        /**
         * Transform {@link JarEntry}
         *
         * @param jarEntry
         * @return
         */
        JarEntry transform(JarEntry jarEntry);
    }

    /**
     * An {@code EntryTransformer} that returns the entry unchanged.
     */
    public static final class IdentityEntryTransformer implements EntryTransformer {

        @Override
        public JarEntry transform(JarEntry jarEntry) {
            return jarEntry;
        }

    }
}