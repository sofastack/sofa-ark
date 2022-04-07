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
package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.exception.ArkRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Utilities for manipulating files and directories in ark tooling.
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @author qilong.zql
 * @author GengZhang
 */
public class FileUtils {

    /**
     * Generate a SHA.1 Hash for a given file.
     * @param file the file to hash
     * @return the hash value as a String
     * @throws IOException if the file cannot be read
     */
    public static String sha1Hash(File file) throws IOException {
        try {
            DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file),
                MessageDigest.getInstance("SHA-1"));
            try {
                byte[] buffer = new byte[4098];
                while (inputStream.read(buffer) != -1) { //NOPMD
                    // Read the entire stream
                }
                return bytesToHex(inputStream.getMessageDigest().digest());
            } finally {
                inputStream.close();
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Atomically creates a new directory somewhere beneath the system's
     * temporary directory (as defined by the {@code java.io.tmpdir} system
     */
    public static File createTempDir(String subPath) {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        File tempDir = new File(baseDir, subPath);
        if (tempDir.exists()) {
            return tempDir;
        } else if (tempDir.mkdir()) {
            return tempDir;
        }
        throw new ArkRuntimeException("Failed to create temp file");
    }

    /**
     * {@link org.apache.commons.io.FileUtils#copyInputStreamToFile(InputStream, File)}
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copyInputStreamToFile(final InputStream source, final File destination)
                                                                                              throws IOException {
        org.apache.commons.io.FileUtils.copyInputStreamToFile(source, destination);
    }

    /**
     *
     * @param path
     * @return
     */
    public static String getCompatiblePath(String path) {
        if (System.getProperty("os.name").toLowerCase().indexOf("window") > -1) {
            return path.replace("\\", "/");
        }
        return path;
    }

    public static File unzip(File root, String targetPath) throws IOException {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(root);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    String dirPath = targetPath + File.separator + entry.getName();
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    InputStream inputStream = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        inputStream = zipFile.getInputStream(entry);
                        File file = new File(targetPath + File.separator + entry.getName());
                        if (!file.exists()) {
                            File fileParent = file.getParentFile();
                            if (!fileParent.exists()) {
                                fileParent.mkdirs();
                            }
                        }
                        file.createNewFile();
                        fileOutputStream = new FileOutputStream(file);
                        int count;
                        byte[] buf = new byte[8192];
                        while ((count = inputStream.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, count);
                        }

                    } finally {
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    }
                }
            }
            return new File(targetPath);
        } finally {
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }
}