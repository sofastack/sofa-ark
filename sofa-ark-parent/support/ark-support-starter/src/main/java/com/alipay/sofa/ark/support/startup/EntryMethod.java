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
package com.alipay.sofa.ark.support.startup;

import com.alipay.sofa.ark.common.util.AssertUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * The "main" method located from a running thread.
 *
 * @author qilong.zql
 * @since 0.1.0
 * @author Phillip Webb
 */
public class EntryMethod {

    private final Method method;

    public EntryMethod() {
        this(Thread.currentThread());
    }

    public EntryMethod(Thread thread) {
        AssertUtils.assertNotNull(thread, "Thread must not be null");
        this.method = getMainMethod(thread);
    }

    private Method getMainMethod(Thread thread) {
        for (StackTraceElement element : thread.getStackTrace()) {
            if ("main".equals(element.getMethodName())) {
                Method method = getMainMethod(element);
                if (method != null) {
                    return method;
                }
            }
        }
        throw new IllegalStateException("Unable to find main method");
    }

    private Method getMainMethod(StackTraceElement element) {
        try {
            Class<?> elementClass = Class.forName(element.getClassName());
            Method method = elementClass.getDeclaredMethod("main", String[].class);
            if (Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (Throwable ex) {
            // ignore
        }
        return null;
    }

    /**
     * Returns the actual main method.
     * @return the main method
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Return the name of the declaring class.
     * @return the declaring class name
     */
    public String getDeclaringClassName() {
        return this.method.getDeclaringClass().getName();
    }

    public String getMethodName() {
        return this.method.getName();
    }

}