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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
    public void testGetCompatiblePathLinux() {
        System.setProperty("os.name", "Linux");
        String path = "/home/user/Documents";
        String expected = "/home/user/Documents";
        String actual = FileUtils.getCompatiblePath(path);
        Assert.assertEquals(expected, actual);
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
    public void testZipSlipBug() throws IOException {
        String zipFileName = "zipSlipExample.zip";
        FileOutputStream fos = new FileOutputStream(zipFileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        String goodFileContent = "This is a good file.";
        String evilFileContent = "This is an evil file.";
        ZipEntry goodEntry1 = new ZipEntry("goodfile.txt");
        zos.putNextEntry(goodEntry1);
        zos.write(goodFileContent.getBytes());
        zos.closeEntry();
        ZipEntry goodEntry2 = new ZipEntry("newDir/goodfile.txt");
        zos.putNextEntry(goodEntry2);
        zos.write(goodFileContent.getBytes());
        zos.closeEntry();
        ZipEntry evilEntry = new ZipEntry("../evilfile.txt");
        zos.putNextEntry(evilEntry);
        zos.write(evilFileContent.getBytes());
        zos.closeEntry();
        zos.close();
        fos.close();

        File zipFile = new File(zipFileName);
        try {
            File unzipFile = FileUtils.unzip(zipFile, zipFile.getAbsolutePath() + "-unpack");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), ArkRuntimeException.class);
        } finally {
            if (null != zipFile && zipFile.exists()) {
                zipFile.delete();
            }
            File unzipFileFolder = new File(zipFileName + "-unpack");
            if (unzipFileFolder.exists()) {
                deleteFolder(unzipFileFolder);
            }
        }
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file); // 递归删除子文件夹
                } else {
                    file.delete(); // 删除文件
                }
            }
        }
        folder.delete(); // 删除文件夹本身
    }
}
