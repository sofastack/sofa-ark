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
package com.alipay.sofa.ark.support.listener;

import com.alipay.sofa.ark.common.util.ClassloaderUtils;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

import java.lang.annotation.Annotation;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ArkTestNGInvokedMethodListener implements IInvokedMethodListener {
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        Class testClass = method.getTestMethod().getRealClass();
        if (isTestOnArk(testClass)) {
            ClassloaderUtils.pushContextClassloader(DelegateArkContainer.getTestClassLoader());
        }
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        ClassloaderUtils.pushContextClassloader(ClassLoader.getSystemClassLoader());
    }

    protected boolean isTestOnArk(Class testClass) {
        for (Annotation annotation : testClass.getAnnotations()) {
            String annotationType = annotation.annotationType().getCanonicalName();
            if (annotationType.equals(TestNGOnArk.class.getCanonicalName())) {
                return true;
            }
        }
        return false;
    }
}