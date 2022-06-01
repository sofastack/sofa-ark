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
package com.alipay.sofa.ark.dynamic.common.context;

import org.springframework.context.ApplicationContext;
import org.springframework.core.AttributeAccessor;
import org.springframework.core.AttributeAccessorSupport;
import org.springframework.lang.Nullable;
import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author hanyue
 * @version : SofaArkTestContext.java, v 0.1 2022年05月08日 上午7:29 hanyue Exp $
 */
public class SofaArkTestContext extends AttributeAccessorSupport implements TestContext {

    private ApplicationContext applicationContext;

    private final Class<?>     testClass;

    @Nullable
    private volatile Object    testInstance;

    @Nullable
    private volatile Method    testMethod;

    @Nullable
    private volatile Throwable testException;

    public SofaArkTestContext(Class<?> testClass) {
        this.testClass = testClass;
    }

    public SofaArkTestContext(SofaArkTestContext defaultTestContext) {
        this.applicationContext = defaultTestContext.getApplicationContext();
        this.testClass = defaultTestContext.getTestClass();
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(@Nullable ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Class<?> getTestClass() {
        return testClass;
    }

    @Override
    public Object getTestInstance() {
        return testInstance;
    }

    @Override
    public Method getTestMethod() {
        return testMethod;
    }

    @Override
    public Throwable getTestException() {
        return testException;
    }

    @Override
    public void markApplicationContextDirty(HierarchyMode hierarchyMode) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyAttributesFrom(AttributeAccessor source) {
        super.copyAttributesFrom(source);
    }

    @Override
    public void updateState(Object testInstance, Method testMethod, Throwable testException) {
        this.testInstance = testInstance;
        this.testMethod = testMethod;
        this.testException = testException;
    }

    /**
     * Generate TestContextManager By testContext
     *
     * @param testContext
     * @return
     */
    public TestContextManager testContextManager(TestContext testContext) {
        try {
            TestContextManager testContextManager = new TestContextManager(getTestClass());
            copyAttributesFrom(testContext);

            Field testContextField = TestContextManager.class.getDeclaredField("testContext");
            ReflectionUtils.makeAccessible(testContextField);
            ReflectionUtils.setField(testContextField, testContextManager, this);

            return testContextManager;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}