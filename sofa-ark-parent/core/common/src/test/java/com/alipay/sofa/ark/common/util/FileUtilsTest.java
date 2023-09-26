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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/7/28 11:24 PM
 * @since
 **/
public class FileUtilsTest {

    private static final String ORIGIN = System.getProperty("os.name");

    @Before
    public void before() {
        System.setProperty("os.name", "windows");
    }

    @After
    public void after() {
        System.setProperty("os.name", ORIGIN);
    }

    @Test
    public void testGetCompatiblePath() {
        String winPath = FileUtils.getCompatiblePath("C:\\a\\b\\c");
        Assert.assertTrue(winPath.contains("/"));
        String macPath = FileUtils.getCompatiblePath("/a/b/c");
        Assert.assertTrue(winPath.contains(macPath));
    }

    @Test
    public void testSHA1Hash() throws IOException {
        URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
        Assert.assertNotNull(FileUtils.sha1Hash(new File(url.getFile())));
    }

    @Test
    public void testUnzip() throws IOException {
        URL sampleBiz = this.getClass().getClassLoader().getResource("sample-biz.jar");
        File file = new File(sampleBiz.getFile());
        Assert.assertNotNull(FileUtils.unzip(file, file.getAbsolutePath() + "-unpack"));
    }

    @Test
    public void testMkdir() {
        Assert.assertNull(FileUtils.mkdir(""));
        // test recursive creation
        File newDir = FileUtils.mkdir("C:\\a\\b\\c");
        Assert.assertNotNull(newDir);
        // test for exist path
        Assert.assertNotNull(FileUtils.mkdir("C:\\a\\b\\c"));
        // del the dir
        org.apache.commons.io.FileUtils.deleteQuietly(newDir);
    }

    @Test
    public void testUnzipSlip() throws IOException {
        String outerFileStr = "../outer.test";
        String zipFileName = "compressed.zip";
        FileOutputStream fos = new FileOutputStream(zipFileName);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        //write to compressed.zip
        try {
            ZipEntry zipEntry = new ZipEntry(outerFileStr);
            zipOut.putNextEntry(zipEntry);
            String data = "this is test file";
            byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
            int length = bytes.length;
            zipOut.write(bytes, 0, length);
            zipOut.flush();
        } finally {
            zipOut.close();
            fos.close();
        }

        //unzip
        File zipFile = new File(zipFileName);
        //test zipFile exist
        Assert.assertTrue(zipFile.exists());
        File unzipFile = null;
        try {
            unzipFile = FileUtils.unzip(zipFile, "./");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().startsWith("unzip failed"));
        } finally {
            //clear zipFile
            if (null != zipFile && zipFile.exists()) {
                zipFile.delete();
            }
        }
        //assert failed null
        Assert.assertNull(unzipFile);
    }

}
