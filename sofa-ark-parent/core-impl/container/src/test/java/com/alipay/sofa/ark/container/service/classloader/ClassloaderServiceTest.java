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
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClassloaderServiceTest extends BaseTest {

    private ClassloaderService classloaderService;

    @Before
    public void before() {
        classloaderService = new ClassloaderServiceImpl();
        classloaderService.init();
    }

    @Test
    public void testIsSunReflect() {
        Assert.assertTrue(classloaderService
            .isSunReflectClass("sun.reflect.GeneratedMethodAccessor100"));
    }

    @Test
    public void testIsNotSunReflect() {
        Assert.assertFalse(classloaderService.isSunReflectClass("test"));
    }

    @Test
    public void testIsArkSpiClass() {
        Assert.assertTrue(classloaderService
            .isArkSpiClass("com.alipay.sofa.ark.spi.service.ArkService"));
    }

    @Test
    public void testIsNotArkSpiClass() {
        Assert.assertFalse(classloaderService.isArkSpiClass("test"));
    }

    @Test
    public void testJDKClassloader() throws Exception {
        String sunToolClass = "sun.tools.attach.BsdVirtualMachine";
        ClassLoader jdkClassloader = classloaderService.getJDKClassloader();
        Assert.assertNotNull(jdkClassloader);
        try {
            // only when this class can be loaded from system classloader,
            // then it should be loaded successfully from jdkClassloader
            classloaderService.getSystemClassloader().loadClass(sunToolClass);
            Assert.assertNotNull(jdkClassloader.loadClass(sunToolClass));
        } catch (ClassNotFoundException e) {
            // ignore
        }

    }

    @Test
    public void testArkClassloader() {
        ClassLoader arkClassloader = classloaderService.getArkClassloader();
        Assert.assertNotNull(arkClassloader);
    }

    @Test
    public void testSystemClassloader() {
        ClassLoader systemClassloader = classloaderService.getSystemClassloader();
        Assert.assertNotNull(systemClassloader);
    }

    @Test
    public void testAgentClassloader() throws ClassNotFoundException {
        ClassLoader agentClassLoader = classloaderService.getAgentClassloader();
        Assert.assertNotNull(agentClassLoader);
        Assert.assertTrue(((URLClassLoader) agentClassLoader).getURLs().length == 2);
        Assert.assertNotNull(agentClassLoader.loadClass("SampleClass"));
    }

}