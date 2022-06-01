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
package com.alipay.sofa.ark.dynamic.classloader;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.dynamic.loader.TestClassloader;
import com.alipay.sofa.ark.dynamic.loader.TestJarLanucher;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.testng.Assert;
import org.testng.IHookable;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;

/**
 * @author hanyue
 * @version : TestClassLoaderTest.java, v 0.1 2022年05月31日 1:51 PM hanyue Exp $
 */
public class TestClassLoader2Test {

    private static TestJarLanucher testJarLanucher;
    private static ClassLoader     appClassLoader;
    private static TestClassloader testClassloader;

    @BeforeTest
    public void setUp() throws Exception {
        File file = JarUtils.getMasterBizFatJar();
        File unpackFile = new File(file.getAbsolutePath() + "-unpack");
        if (!unpackFile.exists()) {
            unpackFile = FileUtils.unzip(file, file.getAbsolutePath() + "-unpack");
        }

        testJarLanucher = new TestJarLanucher(new ExplodedArchive(unpackFile), unpackFile);
        appClassLoader = TestClassLoader2Test.class.getClassLoader();

        ExplodedArchive explodedArchive = new ExplodedArchive(unpackFile);
        testClassloader = new TestClassloader(true, explodedArchive,
            new URL[] { unpackFile.toURL() }, null, appClassLoader, unpackFile);

        Assert.assertNotNull(testJarLanucher);
        Assert.assertNotNull(testClassloader);

        EnvironmentUtils.setProperty(Constants.FORCE_DELEGATE_TO_TEST_CLASSLOADER, "org.testng");
        EnvironmentUtils.setProperty(Constants.FORCE_DELEGATE_TO_APP_CLASSLOADER,
            "org.springframework");
    }

    @Test(expectedExceptions = ClassNotFoundException.class)
    public void testDelegateClassLoader() throws ClassNotFoundException {
        Class<?> iHookableClass1 = testClassloader.loadClass(IHookable.class.getName());
        Assert.assertNotNull(iHookableClass1);
        Assert.assertEquals(iHookableClass1.getClassLoader(), appClassLoader);
    }

    @Test
    public void testTestClassLoader() throws ClassNotFoundException {
        Class<?> jarLauncherClass = testClassloader.loadClass(JarLauncher.class.getName());
        Assert.assertNotNull(jarLauncherClass);
        Assert.assertEquals(jarLauncherClass.getClassLoader(), appClassLoader);
    }
}