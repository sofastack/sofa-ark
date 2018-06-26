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
package com.alipay.sofa.ark.loader.test.base;

import com.alipay.sofa.ark.loader.data.RandomAccessDataFile;
import com.alipay.sofa.ark.loader.jar.AsciiBytes;
import com.alipay.sofa.ark.loader.jar.Bytes;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.*;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class BaseTest {

    public static final String TEMP_DIR           = "temp-workspace";

    public static final String TEMP_FILE          = "temp-file.info";

    public static final String TEMP_ZIP           = "temp-fat-jar.jar";

    public static final String TEST_ENTRY         = "testEntry/";

    public static final String TEST_ENTRY_COMMENT = "testComment";

    public static final String TEST_ENTRY_EXTRA   = "testExtra";

    public static final byte[] CONSTANT_BYTE      = new byte[] { '1', '1', '2', '2', '3', '3', '4',
            '4', '5', '5', '6', '6', '7', '7', '8', '8' };

    @BeforeClass
    public static void startUp() throws IOException {
        generateFile();
        generateZip();
    }

    @AfterClass
    public static void shutDown() {
        cleanWorkspace();
    }

    /**
     * 构建普通的文本文件
     */
    public static void generateFile() throws IOException {
        OutputStream outputStream = new FileOutputStream(getTempDemoFile());

        try {
            outputStream.write(CONSTANT_BYTE, 0, CONSTANT_BYTE.length);
        } finally {
            outputStream.close();
        }

    }

    /**
     * 构建 zip 文件
     */
    public static void generateZip() throws IOException {
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(getTempDemoZip()));
        CRC32 crc = new CRC32();

        /* name end with '/' indicates a directory */
        jos.putNextEntry(new ZipEntry("META-INF/"));

        jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
        jos.write(generateManifest());

        jos.putNextEntry(new ZipEntry("lib/"));

        ZipEntry jarEntry = new ZipEntry("lib/junit-4.12.jar");
        byte[] jarContent = fetchResource("junit-4.12.jar");
        crc.update(jarContent);

        jarEntry.setMethod(ZipEntry.STORED);
        jarEntry.setSize(jarContent.length);
        jarEntry.setCrc(crc.getValue());

        jos.putNextEntry(jarEntry);
        jos.write(jarContent);

        ZipEntry entryForTest = new ZipEntry(TEST_ENTRY);
        entryForTest.setComment(TEST_ENTRY_COMMENT);
        entryForTest.setExtra(TEST_ENTRY_EXTRA.getBytes());
        jos.putNextEntry(entryForTest);

        jos.closeEntry();
        jos.close();
    }

    private static byte[] generateManifest() {
        AsciiBytes asciiBytes = new AsciiBytes("").append("k1: v1\n").append("k2: v2\n");
        return asciiBytes.toString().getBytes();
    }

    private static byte[] fetchResource(String resourceName) throws IOException {
        URL resource = BaseTest.class.getClassLoader().getResource(resourceName);
        RandomAccessDataFile dataFile = new RandomAccessDataFile(new File(resource.getFile()));
        return Bytes.get(dataFile);
    }

    public static File getTmpDir() {
        String tmpPath = System.getProperty("java.io.tmpdir");
        return new File(tmpPath);
    }

    public static File getWorkspace() {
        File workSpace = new File(getTmpDir(), TEMP_DIR);
        if (!workSpace.exists()) {
            workSpace.mkdirs();
        }
        return workSpace;
    }

    public static File getTempDemoFile() {
        return new File(getWorkspace(), TEMP_FILE);
    }

    public static File getTempDemoZip() {
        return new File(getWorkspace(), TEMP_ZIP);
    }

    public static boolean cleanWorkspace() {
        File workDir = new File(getTmpDir(), TEMP_DIR);
        return FileUtils.deleteQuietly(workDir);
    }

    public static boolean compareByteArray(byte[] a, byte[] b) {
        if (a == null && b == null) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; ++i) {
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;
    }
}
