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
package com.alipay.sofa.ark.support.runner;

import com.alipay.sofa.ark.common.util.ClassloaderUtils;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class JUnitExecutionListener extends RunListener {

    private final static String         ARK_JUNIT4_RUNNER = "com.alipay.sofa.ark.support.runner.ArkJUnit4Runner";
    private final static String         ARK_BOOT_RUNNER   = "com.alipay.sofa.ark.springboot.runner.ArkBootRunner";
    private final static Object         LOCK              = new Object();
    private static volatile RunListener singleton;

    private JUnitExecutionListener() {
    }

    @Override
    public void testStarted(Description description) throws Exception {
        if (isTestOnArkContainer(description)) {
            ClassloaderUtils.pushContextClassloader(DelegateArkContainer.getTestClassLoader());
        } else {
            ClassloaderUtils.pushContextClassloader(ClassLoader.getSystemClassLoader());
        }
        super.testStarted(description);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testStarted(description);
        ClassloaderUtils.pushContextClassloader(ClassLoader.getSystemClassLoader());
    }

    protected boolean isTestOnArkContainer(Description description) {
        RunWith runWith = description.getTestClass().getAnnotation(RunWith.class);

        if (runWith == null) {
            return false;
        }

        Class<?> runnerClass = runWith.value();
        String className = runnerClass.getName();

        return ARK_JUNIT4_RUNNER.equals(className) || ARK_BOOT_RUNNER.equals(className);

    }

    public static RunListener getRunListener() {
        if (singleton == null) {
            synchronized (LOCK) {
                if (singleton == null) {
                    singleton = new JUnitExecutionListener();
                }
            }
        }
        return singleton;
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        super.testRunFinished(result);
        DelegateArkContainer.shutdown();
    }
}