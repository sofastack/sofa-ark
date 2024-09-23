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
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
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

    public ArkPluginCopyAction(File jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public WorkResult execute(CopyActionProcessingStream stream) {
        try (ZipArchiveOutputStream zipStream = new ZipArchiveOutputStream(jarFile)) {
            zipStream.setEncoding(String.valueOf(StandardCharsets.UTF_8));
            stream.process(new StreamAction(zipStream));
            return WorkResults.didWork(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JAR file", e);
        }
    }

    private static class StreamAction implements CopyActionProcessingStreamAction {
        private final ZipArchiveOutputStream zipStream;
        StreamAction(ZipArchiveOutputStream zipStream) {
            this.zipStream = zipStream;
        }

        @Override
        public void processFile(FileCopyDetailsInternal details) {
            try {
                ZipArchiveEntry entry = createEntry(details);

                if (details.isDirectory()) {
                    zipStream.putArchiveEntry(entry);
                    zipStream.closeArchiveEntry();
                } else {
                    String path = details.getRelativePath().getPathString();

                    if (path.startsWith("lib")) {
                        setupStoredEntry(entry, details);
                    }

                    zipStream.putArchiveEntry(entry);
                    details.copyTo(zipStream);
                    zipStream.closeArchiveEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to add file to JAR: " + details.getPath(), e);
            }
        }
    }

    private static ZipArchiveEntry createEntry(FileCopyDetailsInternal details) {
        String path = details.getRelativePath().getPathString();
        ZipArchiveEntry entry = new ZipArchiveEntry(details.isDirectory() ? path + '/' : path);
        entry.setTime(details.getLastModified());
        int unixMode = details.getMode() | (details.isDirectory() ? UnixStat.DIR_FLAG : UnixStat.FILE_FLAG);
        entry.setUnixMode(unixMode);
        return entry;
    }

    private static void setupStoredEntry(ZipArchiveEntry entry, FileCopyDetailsInternal details) throws IOException {
        try (InputStream inputStream = details.open()) {
            CrcAndSize crcAndSize = new CrcAndSize(inputStream);
            crcAndSize.setUpStoredEntry(entry);
        } catch (Exception e) {
            System.out.println("Warning: Unable to process JAR file in lib directory: " + details.getPath());
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
