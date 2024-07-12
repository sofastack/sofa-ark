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
package com.alipay.sofa.ark.loader;

import com.alipay.sofa.ark.common.util.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 *
 * @author bingjie.lbj
 */
public class ExplodedBizArchiveTest {

    @Test
    public void testCreate() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL arkBizJar = cl.getResource("sample-biz-withjar.jar");
        File unpack = FileUtils.unzip(FileUtils.file(arkBizJar.getFile()), arkBizJar.getFile()
                                                                     + "-unpack");
        ExplodedBizArchive archive = new ExplodedBizArchive(unpack);
        Assert.assertNotNull(archive.getManifest());
        Assert.assertNotNull(archive.getUrl());

        try {
            archive.getInputStream(null);
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e){
            Assert.assertTrue(true);
        }
        try {
            archive.isEntryExist(entry -> true);
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e){
            Assert.assertTrue(true);
        }
        try {
            archive.getNestedArchive(null);
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e){
            Assert.assertTrue(true);
        }
        try {
            archive.getNestedArchives(null);
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e){
            Assert.assertTrue(true);
        }
        try {
            archive.iterator();
            Assert.assertTrue(false);
        } catch (UnsupportedOperationException e){
            Assert.assertTrue(true);
        }
        Assert.assertEquals(archive.getManifest().getMainAttributes().getValue("Ark-Biz-Name"),
            "sofa-ark-sample-springboot-ark");
        Assert.assertEquals(archive.getUrls().length, 3);

        ClassLoader bizClassLoader = new URLClassLoader(archive.getUrls());
        Class mainClass = null;
        Class logger = null;
        try {
            mainClass = bizClassLoader
                .loadClass("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication");
            logger = bizClassLoader.loadClass("org.slf4j.Logger");
        } catch (ClassNotFoundException exception) {

        }
        Assert.assertNotNull(logger);
        Assert.assertNotNull(mainClass);
    }

    @Test
    public void testCloseManifestFileStream() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL arkBizJar = cl.getResource("sample-biz-withjar.jar");
        File unpack = FileUtils.unzip(FileUtils.file(arkBizJar.getFile()), arkBizJar.getFile()
                                                                           + "-testdelete-unpack");
        ExplodedBizArchive archive = new ExplodedBizArchive(unpack);
        Assert.assertNotNull(archive.getManifest());
        File file = new File(unpack, "META-INF/MANIFEST.MF");
        Assert.assertTrue(file.delete());
        deleteQuietly(unpack);
    }
}