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

import com.alipay.sofa.ark.transloader.ClassWrapper;
import com.alipay.sofa.ark.transloader.Transloader;
import org.springframework.test.context.TestContext;
import org.testng.IHookCallBack;
import org.testng.ITestResult;
import org.testng.TestNGException;
import org.testng.internal.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * The type Hook call back factory.
 *
 * @author hanyue
 * @version : IHookCallBackFactory.java, v 0.1 2022年06月02日 11:12 AM hanyue Exp $
 */
public class IHookCallBackFactory {

    /**
     * Create hook call back.
     *
     * @param classLoader the class loader
     * @param callBack    the call back
     * @param testContext the test context
     * @return the hook call back
     */
    public static IHookCallBack create(ClassLoader classLoader, IHookCallBack callBack,
                                       TestContext testContext) {
        Object[] parameters = callBack.getParameters();
        Object[] newParameters = (Object[]) Transloader.DEFAULT.wrap(parameters).cloneWith(
                classLoader);
        Class<?>[] parameterTypes = ClassWrapper.getClasses(testContext.getTestMethod()
                .getParameterTypes(), classLoader);
        return create(testContext.getTestMethod(), testContext.getTestInstance(), newParameters,
                parameterTypes);
    }

    /**
     * Create hook call back.
     *
     * @param testMethod     the test method
     * @param instance       the instance
     * @param parameters     the parameters
     * @param parameterTypes the parameter types
     * @return the hook call back
     */
    public static IHookCallBack create(Method testMethod, Object instance, Object[] parameters,
                                       Class<?>[] parameterTypes) {
        return new IHookCallBack() {
            @Override
            public void runTestMethod(ITestResult tr) {
                try {
                    invokeMethod(testMethod, instance, parameters, parameterTypes);
                } catch (Throwable t) {
                    tr.setThrowable(t); // make Throwable available to IHookable
                    throw new RuntimeException(t);
                }
            }

            @Override
            public Object[] getParameters() {
                return parameters;
            }
        };
    }

    /**
     * Invoke method object.
     *
     * @param testMethod     the test method
     * @param instance       the instance
     * @param parameters     the parameters
     * @param parameterTypes the parameter types
     * @return the object
     * @throws InvocationTargetException the invocation target exception
     * @throws IllegalAccessException the illegal access exception
     */
    protected static Object invokeMethod(Method testMethod, Object instance, Object[] parameters,
                                         Class<?>[] parameterTypes)
            throws InvocationTargetException,
            IllegalAccessException {
        Utils.checkInstanceOrStatic(instance, testMethod);

        // TESTNG-326, allow IObjectFactory to load from non-standard classloader
        // If the instance has a different classloader, its class won't match the
        // method's class
        if (instance != null
                && !testMethod.getDeclaringClass().isAssignableFrom(instance.getClass())) {
            // for some reason, we can't call this method on this class
            // is it static?
            boolean isStatic = Modifier.isStatic(testMethod.getModifiers());
            if (!isStatic) {
                // not static, so grab a method with the same name and signature in this case
                Class<?> clazz = instance.getClass();
                try {
                    testMethod = clazz.getMethod(testMethod.getName(), parameterTypes);
                } catch (Exception e) {
                    // ignore, the method may be private
                    boolean found = false;
                    for (; clazz != null; clazz = clazz.getSuperclass()) {
                        try {
                            testMethod = clazz.getDeclaredMethod(testMethod.getName(),
                                    testMethod.getParameterTypes());
                            found = true;
                            break;
                        } catch (Exception e2) {
                        }
                    }
                    if (!found) {
                        // should we assert here? Or just allow it to fail on invocation?
                        if (testMethod.getDeclaringClass().equals(instance.getClass())) {
                            throw new RuntimeException("Can't invoke method " + testMethod
                                    + ", probably due to classloader mismatch");
                        }
                        throw new RuntimeException("Can't invoke method " + testMethod
                                + " on this instance of " + instance.getClass()
                                + " due to class mismatch");
                    }
                }
            }
        }

        if (!Modifier.isPublic(testMethod.getModifiers()) || !testMethod.isAccessible()) {
            try {
                testMethod.setAccessible(true);
            } catch (SecurityException e) {
                throw new TestNGException(testMethod.getName() + " must be public", e);
            }
        }
        return testMethod.invoke(instance, parameters);
    }
}