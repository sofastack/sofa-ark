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
package com.alipay.sofa.ark.boot.mojo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.UnixStat;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.gradle.api.internal.file.CopyActionProcessingStreamAction;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.WorkResults;

public class ArkPluginCopyAction implements CopyAction {
    private final File jarFile;

    private final Set<File> shadeFiles;

    public ArkPluginCopyAction(File jarFile, Set<File> shadeFiles) {
        this.jarFile = jarFile;
        this.shadeFiles = shadeFiles;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        try (ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(jarFile)) {
            zipStream.setEncoding(String.valueOf(StandardCharsets.UTF_8));
            StreamAction action = new StreamAction(zipStream, shadeFiles);
            stream.process(action);
            return WorkResults.didWork(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JAR file", e);
        }
    }

    private static class StreamAction implements CopyActionProcessingStreamAction {
        private final ZipArchiveOutputStream zipStream;

        private final Set<File> shadeFiles;
        StreamAction(ZipArchiveOutputStream zipStream, Set<File> shadeFiles) {
            this.zipStream = zipStream;
            this.shadeFiles = shadeFiles;
        }

        @Override
        public void processFile(FileCopyDetailsInternal details) {
            try {
                if (details.isDirectory()) {
                    addDirectory(details);
                } else if (shadeFiles.contains(details.getFile())) {
                    addShadeFileContents(details.getFile());
                } else {
                    addFile(details);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to add file to JAR: " + details.getPath(), e);
            }
        }

        private void addDirectory(FileCopyDetailsInternal details) throws IOException {
            ZipArchiveEntry entry = createEntry(details);
            zipStream.putArchiveEntry(entry);
            zipStream.closeArchiveEntry();
        }

        private void addShadeFileContents(File shadeFile) throws IOException {
            try (ZipFile zipFile = new ZipFile(shadeFile)) {
                zipFile.stream()
                    .filter(this::shouldProcessEntry)
                    .forEach(entry -> processShadeEntry(zipFile, entry));
            }
        }

        private void addFile(FileCopyDetailsInternal details) throws IOException {

            ZipArchiveEntry entry = createEntry(details);

            String path = details.getRelativePath().getPathString();
            if (path.startsWith("lib")) {
                try (InputStream inputStream = details.open()) {
                    CrcAndSize crcAndSize = new CrcAndSize(inputStream);
                    crcAndSize.setUpStoredEntry(entry);
                } catch (Exception e) {
                    throw new IOException("please check this jar file");
                }
            }

            zipStream.putArchiveEntry(entry);
            details.copyTo(zipStream);
            zipStream.closeArchiveEntry();
        }

        private ZipArchiveEntry createEntry(FileCopyDetailsInternal details){
            String path = details.isDirectory() ? details.getRelativePath().getPathString() + '/' : details.getRelativePath().getPathString();
            ZipArchiveEntry entry = new ZipArchiveEntry(path);
            entry.setTime(details.getLastModified());
            int unixMode = details.getMode() | (details.isDirectory()  ? UnixStat.DIR_FLAG : UnixStat.FILE_FLAG);
            entry.setUnixMode(unixMode);
            return entry;
        }

        private boolean shouldProcessEntry(ZipEntry entry) {
            return !"META-INF/MANIFEST.MF".equals(entry.getName());
        }

        private void processShadeEntry(ZipFile zipFile, ZipEntry entry) {
            try {
                ZipArchiveEntry newEntry = createNewEntry(entry);

                if (entry.isDirectory()) {
                    addDirectoryEntry(newEntry);
                } else {
                    addFileEntry(zipFile, entry, newEntry);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to process shade entry: " + entry.getName(), e);
            }
        }

        private ZipArchiveEntry createNewEntry(ZipEntry entry) {
            ZipArchiveEntry newEntry = new ZipArchiveEntry(entry.getName());
            newEntry.setTime(entry.getTime());
            newEntry.setUnixMode(UnixStat.FILE_FLAG | UnixStat.DEFAULT_FILE_PERM);
            return newEntry;
        }

        private void addDirectoryEntry(ZipArchiveEntry entry) throws IOException {
            zipStream.putArchiveEntry(entry);
            zipStream.closeArchiveEntry();
        }

        private void addFileEntry(ZipFile zipFile, ZipEntry entry, ZipArchiveEntry newEntry) throws IOException {
            zipStream.putArchiveEntry(newEntry);
            try (InputStream inputStream = zipFile.getInputStream(entry)) {
                copy(inputStream, zipStream);
            }
            zipStream.closeArchiveEntry();
        }

        private void copy(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
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
