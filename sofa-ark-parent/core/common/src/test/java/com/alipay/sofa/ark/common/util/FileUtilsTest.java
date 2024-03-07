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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.alipay.sofa.ark.common.util.FileUtils.*;
import static java.lang.System.getProperty;
import static java.lang.System.setProperty;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.touch;
import static org.junit.Assert.*;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/7/28 11:24 PM
 * @since
 **/
public class FileUtilsTest {

    private static final String ORIGIN = getProperty("os.name");

    @Before
    public void before() {
        setProperty("os.name", "windows");
    }

    @After
    public void after() {
        setProperty("os.name", ORIGIN);
    }

    @Test
    public void testGetCompatiblePath() {
        String winPath = getCompatiblePath("C:\\a\\b\\c");
        assertTrue(winPath.contains("/"));
        String macPath = getCompatiblePath("/a/b/c");
        assertTrue(winPath.contains(macPath));
    }

    @Test
    public void testSHA1Hash() throws IOException {
        URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
        assertNotNull(sha1Hash(file(url.getFile())));
    }

    @Test
    public void testUnzip() throws IOException {
        URL sampleBiz = this.getClass().getClassLoader().getResource("sample-biz.jar");
        File file = file(sampleBiz.getFile());
        assertNotNull(unzip(file, file.getAbsolutePath() + "-unpack"));
    }

    @Test
    public void testMkdir() {
        assertNull(mkdir(""));
        // test recursive creation
        File newDir = mkdir("C:\\a\\b\\c");
        assertNotNull(newDir);
        // test for exist path
        assertNotNull(mkdir("C:\\a\\b\\c"));
        // del the dir
        deleteQuietly(newDir);
    }

    @Test
    public void testDecodePath() {
        String path = "C:\\temp dir\\b\\c";
        String encodedPath = "C:\\temp%20dir\\b\\c";
        assertEquals(path, decodePath(path));
        assertEquals(path, decodePath(encodedPath));
    }

    @Test
    public void testNewFile() throws IOException {

        String dir = "C:\\temp dir\\b\\c";
        String encodedPath = "C:\\temp%20dir\\b\\c";
        mkdir(dir);
        touch(new File(dir, "test.txt"));
        File file = file(encodedPath, "test.txt");
        assertNotNull(file);
        assertTrue(file.exists());

        file = new File(encodedPath, "test.txt");
        assertFalse(file.exists());
    }
}
