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
import java.io.IOException;
import java.net.URL;

/**
 * @author xingcun
 * @version FileUtilsLinuxTest.java, v 0.1 2023年11月01日 13:39 xingcun
 */
public class FileUtilsLinuxTest {

    private static final String ORIGIN = System.getProperty("os.name");

    @Before
    public void before() {
        System.setProperty("os.name", "Linux");
    }

    @After
    public void after() {
        System.setProperty("os.name", ORIGIN);
    }

    @Test
    public void testGetCompatiblePathLinux() {
        String path = "/home/user/Documents";
        String expected = "/home/user/Documents";
        String actual = FileUtils.getCompatiblePath(path);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testValidateZipEntry_ValidEntry() throws IOException {
        URL sampleBiz = this.getClass().getClassLoader().getResource("sample-biz.jar");
        File entryFile = new File(sampleBiz.getFile());
        String targetPath = sampleBiz.getFile();

        FileUtils.validateZipEntry(targetPath, entryFile);
    }

    @Test(expected = ArkRuntimeException.class)
    public void testValidateZipEntry_InvalidEntry() throws IOException {
        String targetPath = "/folder/file.txt";
        URL sampleBiz = this.getClass().getClassLoader().getResource("sample-biz.jar");
        File entryFile = new File(sampleBiz.getFile());

        FileUtils.validateZipEntry(targetPath, entryFile);
    }
}