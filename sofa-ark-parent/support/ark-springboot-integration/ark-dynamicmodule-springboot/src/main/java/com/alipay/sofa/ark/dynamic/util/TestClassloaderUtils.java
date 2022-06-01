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
package com.alipay.sofa.ark.dynamic.util;

import com.alipay.sofa.ark.common.util.ReflectionUtils;
import com.alipay.sofa.ark.dynamic.common.MasterBizClassloaderHolder;
import com.alipay.sofa.ark.dynamic.launcher.BizLauncher;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants.WORKSPACE;

/**
 * @author hanyue
 * @version : TestClassloaderUtils.java, v 0.1 2022年05月21日 上午10:49 hanyue Exp $
 */
public class TestClassloaderUtils {
    private static final String LIB_DIR = "/lib/";

    private static final List<String> sourceResources = new ArrayList<String>() {{
        add(LIB_DIR + "sofa-ark-dynamicmodule-springboot.jar");
        add(LIB_DIR + "spring-test-5.2.13.RELEASE.jar");
    }};
    private static final List<URL>    targetResources = new ArrayList<>();

    static {
        if (!new File(WORKSPACE, LIB_DIR).exists()) {
            new File(WORKSPACE, LIB_DIR).mkdirs();
        }

        for (String resource : sourceResources) {
            try {
                InputStream inputStream = TestClassloaderUtils.class.getResource(resource).openStream();
                String target = WORKSPACE + resource;

                targetResources.add(new File(target).toURL());
                OutputStream outputStream = new FileOutputStream(target);
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (URL url : targetResources) {
                new File(url.getFile()).deleteOnExit();
            }
        }));
    }

    /**
     * Build the environment for launching Biz
     *
     * @param testClass
     */
    public static void importResourceToTestClassloader(Class<?> testClass) {
        ClassLoader classLoader = MasterBizClassloaderHolder.getClassLoader();

        // Add resource to TestClassLoader
        for (URL resource : targetResources) {
            ReflectionUtils.addURL(classLoader, resource);
        }

        // Gets the test directory of the current class
        URL testTarget = testClass.getClass().getResource(File.separator);

        // BizClassLoader add testTarget
        ReflectionUtils.addURL(classLoader, testTarget);
    }

    /**
     * Start Test Biz in TestClassLoader
     *
     * @param testClass
     * @return
     * @throws Exception
     */
    public static Object startBizAndInjectTestClasss(Class<?> testClass) throws Exception {
        TestClassloaderUtils.importResourceToTestClassloader(testClass);

        ClassLoader masterBizClassloader = MasterBizClassloaderHolder.getClassLoader();
        Class<?> newTestClass = masterBizClassloader.loadClass(testClass.getName());

        Class<?> bizBootstrapStageClass = masterBizClassloader.loadClass(BizLauncher.class.getName());
        Object bizBootstrapStage = bizBootstrapStageClass.newInstance();
        Method method = bizBootstrapStageClass.getDeclaredMethod(BizLauncher.START_METHOD, Class.class);
        method.setAccessible(true);
        Object newInstance = method.invoke(bizBootstrapStage, newTestClass);

        return newInstance;
    }
}