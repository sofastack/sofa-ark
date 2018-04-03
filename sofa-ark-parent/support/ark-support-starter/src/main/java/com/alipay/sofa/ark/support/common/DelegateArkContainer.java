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
package com.alipay.sofa.ark.support.common;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;

import java.lang.reflect.Method;

/**
 * wrap the {@see com.alipay.sofa.ark.container.ArkContainer}
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class DelegateArkContainer {

    private static final String TEST_HELPER             = "com.alipay.sofa.ark.container.test.TestHelper";
    private static final String CREATE_TEST_CLASSLOADER = "createTestClassLoader";

    private static Method       CREATE_TEST_CLASSLOADER_METHOD;

    private static Object       arkContainer;
    private static Object       testHelper;
    private static ClassLoader  testClassLoader;
    private static final Object LOCK                    = new Object();

    /**
     * Launch Ark Container when run tests
     */
    public static void launch() {
        if (arkContainer == null) {
            synchronized (LOCK) {
                if (arkContainer == null) {
                    arkContainer = SofaArkBootstrap.prepareContainerForTest();

                    wrapping();

                    Thread.currentThread().setContextClassLoader(
                        DelegateArkContainer.getTestClassLoader());
                }
            }
        }
    }

    /**
     * wrap {@see com.alipay.sofa.ark.container.ArkContainer}
     */
    protected static void wrapping() {
        AssertUtils.assertNotNull(arkContainer, "Ark Container must be not null.");

        try {
            Class<?> testHelperClass = arkContainer.getClass().getClassLoader()
                .loadClass(TEST_HELPER);
            testHelper = testHelperClass.getConstructor(Object.class).newInstance(arkContainer);
            CREATE_TEST_CLASSLOADER_METHOD = testHelperClass.getMethod(CREATE_TEST_CLASSLOADER);
        } catch (Exception ex) {
            // impossible situation
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get {@see com.alipay.sofa.ark.container.tester.TestClassLoader}, used by
     * loading test class
     *
     * @return
     */
    public static ClassLoader getTestClassLoader() {
        if (testClassLoader == null) {
            synchronized (LOCK) {
                if (testClassLoader == null) {
                    try {
                        testClassLoader = (ClassLoader) CREATE_TEST_CLASSLOADER_METHOD
                            .invoke(testHelper);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        return testClassLoader;
    }

    /**
     * Check whether {@see com.alipay.sofa.ark.container.ArkContainer} startup or not.
     * @return
     */
    public static boolean isStarted() {
        try {
            return arkContainer != null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Load class using {@see com.alipay.sofa.ark.container.tester.TestClassLoader}
     * @param name
     * @return
     */
    public static Class loadClass(String name) {
        try {
            return getTestClassLoader().loadClass(name);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }

}