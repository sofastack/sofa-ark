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

package com.alipay.sofa.ark.plugin;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.util.GradleVersion;

public class ArkCopyAction implements CopyAction {

    static final long CONSTANT_TIME_FOR_ZIP_ENTRIES = OffsetDateTime.of(1980, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli();

    private final File output;

    private final Manifest manifest;

    private final boolean preserveFileTimestamps;

    private final Integer dirMode;

    private final Integer fileMode;

    private final boolean includeDefaultLoader;

    private final Spec<FileTreeElement> requiresUnpack;

    private final Spec<FileTreeElement> exclusions;

    private final Spec<FileCopyDetails> librarySpec;

    private final Function<FileCopyDetails, ZipCompression> compressionResolver;

    private final String encoding;

    private final JarOutputStream jarOutput;

    private final File arkOutput;



    ArkCopyAction(File output,File arkOutput, Manifest manifest, boolean preserveFileTimestamps, Integer dirMode, Integer fileMode,
        boolean includeDefaultLoader,  Spec<FileTreeElement> requiresUnpack,
        Spec<FileTreeElement> exclusions, Spec<FileCopyDetails> librarySpec,
        Function<FileCopyDetails, ZipCompression> compressionResolver, String encoding
    ) throws IOException {
        this.output = output;
        this.arkOutput = arkOutput;
        this.manifest = manifest;
        this.preserveFileTimestamps = preserveFileTimestamps;
        this.dirMode = dirMode;
        this.fileMode = fileMode;
        this.includeDefaultLoader = includeDefaultLoader;
        this.requiresUnpack = requiresUnpack;
        this.exclusions = exclusions;
        this.librarySpec = librarySpec;
        this.compressionResolver = compressionResolver;
        this.encoding = encoding;
        FileOutputStream fileOutputStream = new FileOutputStream(this.output);
        this.jarOutput = new JarOutputStream(fileOutputStream);
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream copyActions) {
        try {
            writeArchive(copyActions);
            return WorkResults.didWork(true);
        }
        catch (IOException ex) {
            throw new GradleException("Failed to create " + this.output, ex);
        }
    }

    private void writeArchive(CopyActionProcessingStream copyActions) throws IOException {

        OutputStream output = new FileOutputStream(this.output);
        try {
            writeArchive(copyActions, output);
        }
        finally {
            closeQuietly(output);
        }
    }

    private void writeArchive(CopyActionProcessingStream copyActions, OutputStream output) throws IOException {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output);
        JarArchiveOutputStream jarOOO = new JarArchiveOutputStream(output);
        try {
            setEncodingIfNecessary(zipOutput);
            Processor processor = new Processor(zipOutput, jarOOO);
            copyActions.process(processor::process);
            processor.finish();
        }
        finally {
            closeQuietly(zipOutput);
        }
    }



    private void setEncodingIfNecessary(ZipArchiveOutputStream zipOutputStream) {
        if (this.encoding != null) {
            zipOutputStream.setEncoding(this.encoding);
        }
    }

    private void closeQuietly(OutputStream outputStream) {
        try {
            outputStream.close();
        }
        catch (IOException ex) {
        }
    }



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
            byte[] buffer = new byte[32 * 1024];
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
     * Internal process used to copy {@link FileCopyDetails file details} to the zip file.
     */
    private class Processor {

        private final ZipArchiveOutputStream out;

        private final JarArchiveOutputStream jarOutput;

        private File arkFile;


        private LoaderZipEntries.WrittenEntries writtenLoaderEntries;

        private final Set<String> writtenDirectories = new LinkedHashSet<>();

        private final Set<String> writtenLibraries = new LinkedHashSet<>();


        Processor(ZipArchiveOutputStream out, JarArchiveOutputStream outputStream) throws IOException {
            this.out = out;
            jarOutput =  outputStream;
        }

        void process(FileCopyDetails details) {
            if(details.getName().contains("sofa-ark-all")){
                this.arkFile = details.getFile();
                return;
            }
            if (skipProcessing(details)) {
                return;
            }
            try {
                //writeLoaderEntriesIfNecessary(details);
                if (details.isDirectory()) {
                    processDirectory(details);
                }
                else {
                    processFile(details);
                }
            }
            catch (IOException ex) {
                throw new GradleException("Failed to add " + details + " to " + ArkCopyAction.this.output, ex);
            }
        }

        private boolean skipProcessing(FileCopyDetails details) {
            return ArkCopyAction.this.exclusions.isSatisfiedBy(details)
                || (this.writtenLoaderEntries != null && this.writtenLoaderEntries.isWrittenDirectory(details));
        }

        private void processDirectory(FileCopyDetails details) throws IOException {
            String name = details.getRelativePath().getPathString();
            System.out.println("processDirectory->" + name);
            //JarEntry entr = new JarEntry(name + '/');
            ZipArchiveEntry entry = new ZipArchiveEntry(name + '/');
            prepareEntry(entry, name, getTime(details), getFileMode(details));
            this.out.putArchiveEntry(entry);
            this.out.closeArchiveEntry();
            this.writtenDirectories.add(name);
        }

        private void processFile(FileCopyDetails details) throws IOException {
            String name = details.getRelativePath().getPathString();
            System.out.println("processFile->"+name);
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            prepareEntry(entry, name, getTime(details), getFileMode(details));
            ZipCompression compression = ArkCopyAction.this.compressionResolver.apply(details);
            if (compression == ZipCompression.STORED) {
                prepareStoredEntry(details, entry);
            }
            this.out.putArchiveEntry(entry);
            details.copyTo(this.out);
            this.out.closeArchiveEntry();
            if (ArkCopyAction.this.librarySpec.isSatisfiedBy(details)) {
                this.writtenLibraries.add(name);
            }

        }

        private void writeParentDirectoriesIfNecessary(String name, Long time) throws IOException {
            String parentDirectory = getParentDirectory(name);
            System.out.println("writeParentDirectoriesIfNecessary-> "+parentDirectory);
            if (parentDirectory != null && this.writtenDirectories.add(parentDirectory)) {
                ZipArchiveEntry entry = new ZipArchiveEntry(parentDirectory + '/');
                prepareEntry(entry, parentDirectory, time, getDirMode());
                this.out.putArchiveEntry(entry);
                this.out.closeArchiveEntry();
            }
        }

        private String getParentDirectory(String name) {
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash == -1) {
                return null;
            }
            return name.substring(0, lastSlash);
        }

        void finish() throws IOException {
            //writeLoaderEntriesIfNecessary(null);
            writeClassPathIndexIfNecessary();
            writeArkBizMark();
            writeBootstrapEntry();
            // We must write the layer index last
            //writeLayersIndexIfNecessary();
        }


        private void writeBootstrapEntry() throws IOException {

            System.out.println("arkFile:->>>>>>"+arkFile);
            try (JarFile jarFileSource = new JarFile(this.arkFile)){
                Enumeration<JarEntry> entries = jarFileSource.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    System.out.println("||"+entry.getName());
                    if (entry.getName().contains("sofa-ark-archive")
                        || entry.getName().contains("sofa-ark-spi")
                        || entry.getName().contains("sofa-ark-common")) {

                        JarInputStream inputStream = new JarInputStream(new BufferedInputStream(
                            jarFileSource.getInputStream(entry)));
                        writeLoaderClasses(inputStream, jarFileSource);
                    }
                }
            } catch (NullPointerException exception){
                throw new RuntimeException("No sofa-ark-all file find, please configure it");
            }

        }

        private void writeLoaderClasses(JarInputStream jarInputStream, JarFile jarFileSource) throws IOException {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".class")
                    && (entry.getName().contains("com/alipay/sofa/ark/spi/archive")
                    || entry.getName().contains("com/alipay/sofa/ark/loader")
                    || entry.getName().contains("com/alipay/sofa/ark/bootstrap")
                    || entry.getName().contains("com/alipay/sofa/ark/common/util/StringUtils")
                    || entry.getName().contains("com/alipay/sofa/ark/common/util/AssertUtils") || entry
                    .getName().contains("com/alipay/sofa/ark/spi/constant"))) {


//					byte[] buffer = new byte[BUFFER_SIZE];
//					int bytesRead;
//					while ((bytesRead = this.inputStream.read(buffer)) != -1) {
//						outputStream.write(buffer, 0, bytesRead);
//					}

                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entry.getName());
                    prepareEntry(zipArchiveEntry, entry.getName(), getTime(), getFileMode());
                    this.out.putArchiveEntry(zipArchiveEntry);

                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = jarInputStream.read(bytes)) >= 0) {
                        this.out.write(bytes, 0, length);
                    }
//					this.out.write(entry.);
                    this.out.closeArchiveEntry();


                }
            }
            jarInputStream.close();
        }

        private void writeEntry(JarEntry entry, EntryWriter entryWriter,  JarFile jarFileSource) throws IOException {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+entry.getName());

            JarArchiveEntry newEntry = new JarArchiveEntry(entry.getName());
            newEntry.setSize(entry.getSize());
            newEntry.setTime(entry.getTime());

            jarOutput.putArchiveEntry(newEntry);

            //
            try (InputStream is = jarFileSource.getInputStream(entry)) {
                IOUtils.copy(is, jarOutput);
            }

            jarOutput.closeArchiveEntry();


//			String parent = entry.getName();
//			if (parent.endsWith("/")) {
//				parent = parent.substring(0, parent.length() - 1);
//			}
//			if (parent.lastIndexOf("/") != -1) {
//				parent = parent.substring(0, parent.lastIndexOf("/") + 1);
//				if (parent.length() > 0) {
//					writeEntry(new JarEntry(parent), null);
//				}
//			}
//
//
//			this.jarOutput.putNextEntry(entry);
//			if (entryWriter != null) {
//				entryWriter.write(this.jarOutput);
//			}
//			this.jarOutput.closeEntry();

        }



        private void writeArkBizMark() throws IOException {
            String str = "a mark file included in sofa-ark module.";
            String name = "com/alipay/sofa/ark/biz/mark";
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            prepareEntry(entry, name, getTime(), getFileMode());
            this.out.putArchiveEntry(entry);
            this.out.write(str.getBytes());
            this.out.closeArchiveEntry();
        }

        private void writeLoaderEntriesIfNecessary(FileCopyDetails details) throws IOException {
            if (!ArkCopyAction.this.includeDefaultLoader || this.writtenLoaderEntries != null) {
                return;
            }
            if (isInMetaInf(details)) {
                // Always write loader entries after META-INF directory (see gh-16698)
                return;
            }
            LoaderZipEntries loaderEntries = new LoaderZipEntries(getTime(), getDirMode(), getFileMode());
            this.writtenLoaderEntries = loaderEntries.writeTo(this.out);

        }

        private boolean isInMetaInf(FileCopyDetails details) {
            if (details == null) {
                return false;
            }
            String[] segments = details.getRelativePath().getSegments();
            return segments.length > 0 && "META-INF".equals(segments[0]);
        }


        private void writeClassPathIndexIfNecessary() throws IOException {
            Attributes manifestAttributes = ArkCopyAction.this.manifest.getAttributes();
            String classPathIndex = (String) manifestAttributes.get("Spring-Boot-Classpath-Index");
            if (classPathIndex != null) {
                List<String> lines = this.writtenLibraries.stream()
                    .map((line) -> "- \"" + line + "\"")
                    .collect(Collectors.toList());
                writeEntry(classPathIndex, ZipEntryContentWriter.fromLines(ArkCopyAction.this.encoding, lines),
                    true);
            }
        }



        private void writeEntry(String name, ZipEntryContentWriter entryWriter, boolean addToLayerIndex)
            throws IOException {
            writeEntry(name, entryWriter, addToLayerIndex, ZipEntryCustomizer.NONE);
        }

        private void writeEntry(String name, ZipEntryContentWriter entryWriter, boolean addToLayerIndex,
            ZipEntryCustomizer entryCustomizer) throws IOException {
            System.out.println("write zip name ->>>" + name);
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            prepareEntry(entry, name, getTime(), getFileMode());
            entryCustomizer.customize(entry);
            this.out.putArchiveEntry(entry);
            entryWriter.writeTo(this.out);
            this.out.closeArchiveEntry();

        }


        private void prepareEntry(ZipArchiveEntry entry, String name, Long time, int mode) throws IOException {
            writeParentDirectoriesIfNecessary(name, time);
            entry.setUnixMode(mode);
            if (time != null) {
                entry.setTime(DefaultTimeZoneOffset.INSTANCE.removeFrom(time));
            }
        }

        private void prepareStoredEntry(FileCopyDetails details, ZipArchiveEntry archiveEntry) throws IOException {
            prepareStoredEntry(details.open(), archiveEntry);
            if (ArkCopyAction.this.requiresUnpack.isSatisfiedBy(details)) {
                archiveEntry.setComment("UNPACK:" + FileUtils.sha1Hash(details.getFile()));
            }
        }

        private void prepareStoredEntry(InputStream input, ZipArchiveEntry archiveEntry) throws IOException {
            new CrcAndSize(input).setUpStoredEntry(archiveEntry);
        }

        private Long getTime() {
            return getTime(null);
        }

        private Long getTime(FileCopyDetails details) {
            if (!ArkCopyAction.this.preserveFileTimestamps) {
                return CONSTANT_TIME_FOR_ZIP_ENTRIES;
            }
            if (details != null) {
                return details.getLastModified();
            }
            return null;
        }

        private int getDirMode() {
            return (ArkCopyAction.this.dirMode != null) ? ArkCopyAction.this.dirMode
                : UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        }

        private int getFileMode() {
            return (ArkCopyAction.this.fileMode != null) ? ArkCopyAction.this.fileMode
                : UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        }

        private int getFileMode(FileCopyDetails details) {
            return (ArkCopyAction.this.fileMode != null) ? ArkCopyAction.this.fileMode
                : UnixStat.FILE_FLAG | getPermissions(details);
        }

        private int getPermissions(FileCopyDetails details) {
            if (GradleVersion.current().compareTo(GradleVersion.version("8.3")) >= 0) {
                try {
                    Object permissions = details.getClass().getMethod("getPermissions").invoke(details);
                    return (int) permissions.getClass().getMethod("toUnixNumeric").invoke(permissions);
                }
                catch (Exception ex) {
                    throw new GradleException("Failed to get permissions", ex);
                }
            }
            return details.getMode();
        }

    }

    /**
     * Callback interface used to customize a {@link ZipArchiveEntry}.
     */
    @FunctionalInterface
    private interface ZipEntryCustomizer {

        ZipEntryCustomizer NONE = (entry) -> {
        };

        /**
         * Customize the entry.
         * @param entry the entry to customize
         * @throws IOException on IO error
         */
        void customize(ZipArchiveEntry entry) throws IOException;

    }

    /**
     * Callback used to write a zip entry data.
     */
    @FunctionalInterface
    private interface ZipEntryContentWriter {

        /**
         * Write the entry data.
         * @param out the output stream used to write the data
         * @throws IOException on IO error
         */
        void writeTo(ZipArchiveOutputStream out) throws IOException;

        /**
         * Create a new {@link ZipEntryContentWriter} that will copy content from the
         * given {@link InputStream}.
         * @param in the source input stream
         * @return a new {@link ZipEntryContentWriter} instance
         */
        static ZipEntryContentWriter fromInputStream(InputStream in) {
            System.out.println("44444444444444");
            return (out) -> {
                StreamUtils.copy(in, out);
                in.close();
            };
        }

        /**
         * Create a new {@link ZipEntryContentWriter} that will copy content from the
         * given lines.
         * @param encoding the required character encoding
         * @param lines the lines to write
         * @return a new {@link ZipEntryContentWriter} instance
         */
        static ZipEntryContentWriter fromLines(String encoding, Collection<String> lines) {
            return (out) -> {
                OutputStreamWriter writer = new OutputStreamWriter(out, encoding);
                for (String line : lines) {
                    writer.append(line).append("\n");
                }
                writer.flush();
            };
        }

    }

    /**
     * Data holder for CRC and Size.
     */
    private static class CrcAndSize {

        private static final int BUFFER_SIZE = 32 * 1024;

        private final CRC32 crc = new CRC32();

        private long size;

        CrcAndSize(InputStream inputStream) throws IOException {
            try {
                load(inputStream);
            }
            finally {
                inputStream.close();
            }
        }

        private void load(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                this.crc.update(buffer, 0, bytesRead);
                this.size += bytesRead;
            }
        }

        void setUpStoredEntry(ZipArchiveEntry entry) {
            entry.setSize(this.size);
            entry.setCompressedSize(this.size);
            entry.setCrc(this.crc.getValue());
            entry.setMethod(ZipEntry.STORED);
        }

    }

}
