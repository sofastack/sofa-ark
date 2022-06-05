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
package com.alipay.sofa.ark.dynamic.support.testng;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContextManager;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.springboot.util.RootApplicationContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author hanyue
 * @version : AbstractTestNGSpringContextTests.java, v 0.1 2022年05月02日 上午10:21 hanyue Exp $
 */
@TestExecutionListeners({ DirtiesContextBeforeModesTestExecutionListener.class,
                         DependencyInjectionTestExecutionListener.class,
                         DirtiesContextTestExecutionListener.class })
public class AbstractTestNGSofaArkContextTests implements IHookable, ApplicationContextAware {

    /**
     * Logger available to subclasses.
     */
    protected final Log                     logger = LogFactory.getLog(getClass());

    /**
     * The {@link ApplicationContext} that was injected into this test instance
     * via {@link #setApplicationContext(ApplicationContext)}.
     */
    @Nullable
    protected ApplicationContext            applicationContext;

    private TestContextManager              testContextManager;

    /**
     * Manager SofaArk test framework all Listener and testContext
     */
    private final SofaArkTestContextManager sofaArkTestContextManager;

    @Nullable
    private Throwable                       testException;

    /**
     * Construct a new {@code AbstractTestNGSpringContextTests} instance and initialize
     * the internal {@link TestContextManager} for the current test class.
     */
    public AbstractTestNGSofaArkContextTests() {
        this.testContextManager = new TestContextManager(getClass());

        this.sofaArkTestContextManager = new SofaArkTestContextManager(getClass());
        reloadBuildTestContextManager();
    }

    private void reloadBuildTestContextManager() {
        if (ArkClient.getBizManagerService() != null) {
            Biz biz = ArkClient.getBizManagerService().getBizByClassLoader(
                getClass().getClassLoader());
            if (biz != null) {
                ApplicationContext applicationContext = RootApplicationContextUtils
                    .getApplicationContext(biz.getBizClassLoader());
                SofaArkTestContext serverlessTestContext = getSofaArkTestContextManager()
                    .getSofaArkTestContext();
                serverlessTestContext.setApplicationContext(applicationContext);
                this.testContextManager = serverlessTestContext
                    .testContextManager(this.testContextManager.getTestContext());
            }
        }
    }

    /**
     * Getter method for property <tt>serverlessTestContextManager</tt>.
     *
     * @return property value of serverlessTestContextManager
     */
    public SofaArkTestContextManager getSofaArkTestContextManager() {
        return sofaArkTestContextManager;
    }

    /**
     * Set the {@link ApplicationContext} to be used by this test instance,
     * provided via {@link ApplicationContextAware} semantics.
     *
     * @param applicationContext the ApplicationContext that this test runs in
     */
    @Override
    public final void setApplicationContext(@Nullable ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Delegates to the configured {@link TestContextManager} to call
     * {@linkplain TestContextManager#beforeTestClass() 'before test class'} callbacks.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @BeforeClass(alwaysRun = true)
    protected void springTestContextBeforeTestClass() throws Exception {
        this.testContextManager.beforeTestClass();
    }

    /**
     * Delegates to the configured {@link TestContextManager} to
     * {@linkplain TestContextManager#prepareTestInstance(Object) prepare} this test
     * instance prior to execution of any individual tests, for example for
     * injecting dependencies, etc.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "springTestContextBeforeTestClass")
    protected void springTestContextPrepareTestInstance() throws Exception {
        this.testContextManager.prepareTestInstance(this);
    }

    /**
     * Delegates to the configured {@link TestContextManager} to
     * {@linkplain TestContextManager#beforeTestMethod(Object, Method) pre-process}
     * the test method before the actual test is executed.
     *
     * @param testMethod the test method which is about to be executed
     * @throws Exception allows all exceptions to propagate
     */
    @BeforeMethod(alwaysRun = true)
    protected void springTestContextBeforeTestMethod(Method testMethod) throws Exception {
        this.testContextManager.beforeTestMethod(this, testMethod);
    }

    /**
     * Delegates to the {@linkplain IHookCallBack#runTestMethod(ITestResult) test
     * method} in the supplied {@code callback} to execute the actual test
     * and then tracks the exception thrown during test execution, if any.
     *
     * @see IHookable#run(IHookCallBack, ITestResult)
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (callBack.getParameters().length > 0) {
            callBack = IHookCallBackFactory.create(getClass().getClassLoader(), callBack,
                getTestContextManager().getTestContext());
        }

        Method testMethod = testResult.getMethod().getConstructorOrMethod().getMethod();
        boolean beforeCallbacksExecuted = false;

        try {
            this.testContextManager.beforeTestExecution(this, testMethod);
            beforeCallbacksExecuted = true;
        } catch (Throwable ex) {
            this.testException = ex;
        }

        if (beforeCallbacksExecuted) {
            callBack.runTestMethod(testResult);
            this.testException = getTestResultException(testResult);
        }

        try {
            this.testContextManager.afterTestExecution(this, testMethod, this.testException);
        } catch (Throwable ex) {
            if (this.testException == null) {
                this.testException = ex;
            }
        }

        if (this.testException != null) {
            throwAsUncheckedException(this.testException);
        }
    }

    /**
     * Delegates to the configured {@link TestContextManager} to
     * {@linkplain TestContextManager#afterTestMethod(Object, Method, Throwable)
     * post-process} the test method after the actual test has executed.
     *
     * @param testMethod the test method which has just been executed on the
     *                   test instance
     * @throws Exception allows all exceptions to propagate
     */
    @AfterMethod(alwaysRun = true)
    protected void springTestContextAfterTestMethod(Method testMethod) throws Exception {
        try {
            this.testContextManager.afterTestMethod(this, testMethod, this.testException);
        } finally {
            this.testException = null;
        }
    }

    /**
     * Delegates to the configured {@link TestContextManager} to call
     * {@linkplain TestContextManager#afterTestClass() 'after test class'} callbacks.
     *
     * @throws Exception if a registered TestExecutionListener throws an exception
     */
    @AfterClass(alwaysRun = true)
    protected void springTestContextAfterTestClass() throws Exception {
        this.testContextManager.afterTestClass();
    }

    private Throwable getTestResultException(ITestResult testResult) {
        Throwable testResultException = testResult.getThrowable();
        if (testResultException instanceof InvocationTargetException) {
            testResultException = ((InvocationTargetException) testResultException).getCause();
        }
        return testResultException;
    }

    private RuntimeException throwAsUncheckedException(Throwable t) {
        throwAs(t);
        // Appeasing the compiler: the following line will never be executed.
        throw new IllegalStateException(t);
    }

    @SuppressWarnings("unchecked")
    private <T extends Throwable> void throwAs(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Getter method for property <tt>testContextManager</tt>.
     *
     * @return property value of testContextManager
     */
    public TestContextManager getTestContextManager() {
        return testContextManager;
    }
}