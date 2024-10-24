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

import com.alipay.sofa.ark.common.util.FileUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;
import org.gradle.util.GradleVersion;

public class ArkBizCopyAction implements CopyAction {

    static final long CONSTANT_TIME_FOR_ZIP_ENTRIES = OffsetDateTime.of(1980, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli();

    private final File bizOutput;

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

    private final File arkOutput;

    private String arkBootFile;

    private final java.util.jar.Manifest arkManifest;

    private List<File> pluginFiles;
    private List<File> bizFiles;
    private List<File> conFile;



    ArkBizCopyAction(File bizOutput,File arkOutput, Manifest manifest, boolean preserveFileTimestamps, Integer dirMode, Integer fileMode,
        boolean includeDefaultLoader,  Spec<FileTreeElement> requiresUnpack,
        Spec<FileTreeElement> exclusions, Spec<FileCopyDetails> librarySpec,
        Function<FileCopyDetails, ZipCompression> compressionResolver, String encoding, java.util.jar.Manifest arkManifest, List<File> pluginFiles, List<File> bizFiles, List<File> conFile
    ) throws IOException {
        this.bizOutput = bizOutput;
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
        this.arkManifest = arkManifest;
        this.pluginFiles = pluginFiles;
        this.bizFiles = bizFiles;
        this.conFile = conFile;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream copyActions) {
        try {
            writeBizArchive(copyActions);
            writeArkArchive();
            return WorkResults.didWork(true);
        }
        catch (IOException ex) {
            throw new GradleException("Failed to create " + this.bizOutput, ex);
        }
    }

    private void writeBizArchive(CopyActionProcessingStream copyActions) throws IOException {
        OutputStream output = new FileOutputStream(this.bizOutput);
        try {
            writeArchive(copyActions, output);
        }
        finally {
            closeQuietly(output);
        }
    }

    private void writeArkArchive() throws IOException {
        OutputStream output = new FileOutputStream(this.arkOutput);
        try {
            writeArkArchive(output);
        }
        finally {
            closeQuietly(output);
        }
    }

    private void writeArkArchive(OutputStream output) throws IOException {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output);
        try {
            setEncodingIfNecessary(zipOutput);
            Processor1 processor = new Processor1(zipOutput);
            processor.process();
        }
        finally {
            closeQuietly(zipOutput);
        }
    }

    private void writeArchive(CopyActionProcessingStream copyActions, OutputStream output) throws IOException {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(output);
        try {
            setEncodingIfNecessary(zipOutput);
            Processor processor = new Processor(zipOutput);
            copyActions.process(processor::process);
            processor.finish();
        }
        finally {
            closeQuietly(zipOutput);
        }
    }

    private void closeQuietly(OutputStream outputStream) {
        try {
            outputStream.close();
        }
        catch (IOException ex) {
        }
    }

    private void setEncodingIfNecessary(ZipArchiveOutputStream zipOutputStream) {
        if (this.encoding != null) {
            zipOutputStream.setEncoding(this.encoding);
        }
    }

private class Processor1{
    private final ZipArchiveOutputStream out;

    private LoaderZipEntries.WrittenEntries writtenLoaderEntries;

    private final Set<String> writtenDirectories = new LinkedHashSet<>();

    private final Set<String> writtenLibraries = new LinkedHashSet<>();

    private String arkFile;


    Processor1(ZipArchiveOutputStream out) throws IOException {
        this.out = out;
        this.arkFile = conFile.get(0).getAbsolutePath();
    }

    void process() throws IOException {
        writePluginJar();
        writeBootstrapEntry();
        writeArkManifest();
        writeContainer();
        writeBizJar();
    }

    void writePluginJar() throws IOException {
        writeFiles(pluginFiles, "SOFA-ARK/plugin/");
    }

    void writeArkManifest() throws IOException {
        ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry("META-INF/MANIFEST.MF");
        this.out.putArchiveEntry(zipArchiveEntry);
        ArkBizCopyAction.this.arkManifest.write(this.out);
        this.out.closeArchiveEntry();
    }

    private void writeBootstrapEntry() throws IOException {
        try (JarFile jarFileSource = new JarFile(this.arkFile)){
            Enumeration<JarEntry> entries = jarFileSource.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
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

    void writeContainer() throws IOException {
        File file = new File(arkFile);
        try(  FileInputStream fileInputStream = new FileInputStream(file)) {
            ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry("SOFA-ARK/container/"+ file.getName());
            writeEntry(fileInputStream, zipArchiveEntry);
        }
    }

    void writeBizJar() throws IOException {
        bizFiles.add(bizOutput);
        writeFiles(bizFiles, "SOFA-ARK/biz/");
    }

    void writeFiles(List<File> files, String path){
        for(File file : files){
            try(  FileInputStream fileInputStream = new FileInputStream(file)) {
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(path + file.getName());
                writeEntry(fileInputStream, zipArchiveEntry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeEntry(FileInputStream fileInputStream, ZipArchiveEntry zipArchiveEntry) throws IOException {
        this.out.putArchiveEntry(zipArchiveEntry);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            this.out.write(buffer, 0, len);
        }
        this.out.closeArchiveEntry();
    }



    private void writeDirectory(ZipArchiveEntry entry, ZipArchiveOutputStream out) throws IOException {
        out.putArchiveEntry(entry);
        out.closeArchiveEntry();
    }

    private void writeClass(ZipArchiveEntry entry, ZipInputStream in, ZipArchiveOutputStream out) throws IOException {
        out.putArchiveEntry(entry);
        StringUtils.copyTo(in, out);
        out.closeArchiveEntry();
    }

    private int getDirMode() {
        return (ArkBizCopyAction.this.dirMode != null) ? ArkBizCopyAction.this.dirMode
            : UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
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

                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(entry.getName());
                this.out.putArchiveEntry(zipArchiveEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = jarInputStream.read(bytes)) >= 0) {
                    this.out.write(bytes, 0, length);
                }
                this.out.closeArchiveEntry();


            }
        }
        jarInputStream.close();
    }


}

    private class Processor {

        private final ZipArchiveOutputStream out;

        private LoaderZipEntries.WrittenEntries writtenLoaderEntries;

        private final Set<String> writtenDirectories = new LinkedHashSet<>();

        private final Set<String> writtenLibraries = new LinkedHashSet<>();

        Processor(ZipArchiveOutputStream out) throws IOException {
            this.out = out;
        }

        void process(FileCopyDetails details) {
            if (skipProcessing(details)) {
                return;
            }
            try {
                if (details.isDirectory()) {
                    processDirectory(details);
                } else {
                    processFile(details);
                }
            } catch (IOException ex) {
                throw new GradleException("Failed to add " + details + " to " + ArkBizCopyAction.this.bizOutput, ex);
            }
        }


        private boolean skipProcessing(FileCopyDetails details) {
            return ArkBizCopyAction.this.exclusions.isSatisfiedBy(details)
                || (this.writtenLoaderEntries != null && this.writtenLoaderEntries.isWrittenDirectory(details));
        }

        private void processDirectory(FileCopyDetails details) throws IOException {
            String name = details.getRelativePath().getPathString();
            ZipArchiveEntry entry = new ZipArchiveEntry(name + '/');
            prepareEntry(entry, name, getTime(details), getFileMode(details));
            this.out.putArchiveEntry(entry);
            this.out.closeArchiveEntry();
            this.writtenDirectories.add(name);
        }

        private void processFile(FileCopyDetails details) throws IOException {
            String name = details.getRelativePath().getPathString();
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            prepareEntry(entry, name, getTime(details), getFileMode(details));
            ZipCompression compression = ArkBizCopyAction.this.compressionResolver.apply(details);
            if (compression == ZipCompression.STORED) {
                prepareStoredEntry(details, entry);
            }
            this.out.putArchiveEntry(entry);
            details.copyTo(this.out);
            this.out.closeArchiveEntry();
            if (ArkBizCopyAction.this.librarySpec.isSatisfiedBy(details)) {
                this.writtenLibraries.add(name);
            }

        }

        private String getParentDirectory(String name) {
            int lastSlash = name.lastIndexOf('/');
            if (lastSlash == -1) {
                return null;
            }
            return name.substring(0, lastSlash);
        }

        private void prepareEntry(ZipArchiveEntry entry, String name, Long time, int mode) throws IOException {
            writeParentDirectoriesIfNecessary(name, time);
            entry.setUnixMode(mode);
            if (time != null) {
                entry.setTime(DefaultTimeZoneOffset.INSTANCE.removeFrom(time));
            }
        }

        private void writeParentDirectoriesIfNecessary(String name, Long time) throws IOException {
            String parentDirectory = getParentDirectory(name);
            if (parentDirectory != null && this.writtenDirectories.add(parentDirectory)) {
                ZipArchiveEntry entry = new ZipArchiveEntry(parentDirectory + '/');
                prepareEntry(entry, parentDirectory, time, getDirMode());
                this.out.putArchiveEntry(entry);
                this.out.closeArchiveEntry();
            }
        }



        void finish() throws IOException {
            writeArkBizMark();
        }

        private void writeArkBizMark() throws IOException {
            String info = "a mark file included in sofa-ark module.";
            String name = "com/alipay/sofa/ark/biz/mark";
            ZipArchiveEntry entry = new ZipArchiveEntry(name);
            prepareEntry(entry, name, getTime(), getFileMode());
            this.out.putArchiveEntry(entry);
            byte[] data = info.getBytes(StandardCharsets.UTF_8);
            this.out.write(data, 0, data.length);
            this.out.closeArchiveEntry();
        }

        private void prepareStoredEntry(FileCopyDetails details, ZipArchiveEntry archiveEntry) throws IOException {
            prepareStoredEntry(details.open(), archiveEntry);
            if (ArkBizCopyAction.this.requiresUnpack.isSatisfiedBy(details)) {
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
            if (!ArkBizCopyAction.this.preserveFileTimestamps) {
                return CONSTANT_TIME_FOR_ZIP_ENTRIES;
            }
            if (details != null) {
                return details.getLastModified();
            }
            return null;
        }

        private int getDirMode() {
            return (ArkBizCopyAction.this.dirMode != null) ? ArkBizCopyAction.this.dirMode
                : UnixStat.DIR_FLAG | UnixStat.DEFAULT_DIR_PERM;
        }

        private int getFileMode() {
            return (ArkBizCopyAction.this.fileMode != null) ? ArkBizCopyAction.this.fileMode
                : UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM;
        }

        private int getFileMode(FileCopyDetails details) {
            return (ArkBizCopyAction.this.fileMode != null) ? ArkBizCopyAction.this.fileMode
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
