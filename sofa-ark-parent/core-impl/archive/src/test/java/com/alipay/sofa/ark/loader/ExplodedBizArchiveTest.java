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
import junit.framework.TestCase;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author bingjie.lbj
 */
public class ExplodedBizArchiveTest extends TestCase {

    public void testCreate() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL arkBizJar = cl.getResource("sample-biz-withjar.jar");
        File unpack = FileUtils.unzip(new File(arkBizJar.getFile()), arkBizJar.getFile()
                                                                     + "-unpack");
        ExplodedBizArchive archive = new ExplodedBizArchive(unpack);
        Assert.assertNotNull(archive.getManifest());
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
}