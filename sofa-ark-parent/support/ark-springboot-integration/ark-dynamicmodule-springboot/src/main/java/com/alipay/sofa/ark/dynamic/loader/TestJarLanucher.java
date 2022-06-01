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
package com.alipay.sofa.ark.dynamic.loader;

import com.alipay.sofa.ark.dynamic.common.MasterBizClassloaderHolder;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.Archive;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author hanyue
 * @version : TestJarLanucher.java, v 0.1 2022年05月19日 下午1:01 hanyue Exp $
 */
public class TestJarLanucher extends JarLauncher {

    private final File root;

    public TestJarLanucher(Archive archive, File root) {
        super(archive);
        this.root = root;
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws MalformedURLException {
        TestClassloader testClassloader = new TestClassloader(isExploded(), getArchive(), urls,
            null, getClass().getClassLoader(), root);
        MasterBizClassloaderHolder.setClassLoader(testClassloader);
        return testClassloader;
    }

    public void run(String[] args) throws Exception {
        super.launch(args);
    }
}