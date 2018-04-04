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
package com.alipay.sofa.ark.support.thread;

/**
 * Runner used to launch the application.
 *
 * @author qilong.zql
 * @since 0.1.0
 */

import java.lang.reflect.Method;

public class LaunchRunner implements Runnable {

    private final String   startClassName;

    private final String   startMethodName;

    private final String[] args;

    public LaunchRunner(String startClassName, String... args) {
        this(startClassName, "main", args);
    }

    public LaunchRunner(String startClassName, String startMethodName, String... args) {
        this.startClassName = startClassName;
        this.startMethodName = startMethodName;
        this.args = (args != null ? args : new String[] {});
    }

    @Override
    public void run() {
        Thread thread = Thread.currentThread();
        ClassLoader classLoader = thread.getContextClassLoader();
        try {
            Class<?> startClass = classLoader.loadClass(this.startClassName);
            Method entryMethod;
            try {
                entryMethod = startClass.getMethod(startMethodName, String[].class);
            } catch (NoSuchMethodException ex) {
                entryMethod = startClass.getDeclaredMethod(startMethodName, String[].class);
            }
            if (!entryMethod.isAccessible()) {
                entryMethod.setAccessible(true);
            }
            entryMethod.invoke(null, new Object[] { this.args });
        } catch (NoSuchMethodException ex) {

            Exception wrappedEx = new Exception(
                String.format(
                    "The specified entry class:%s doesn't contain an entry method:%s with appropriate signature.",
                    this.startClassName, this.startMethodName), ex);
            thread.getThreadGroup().uncaughtException(thread, wrappedEx);
        } catch (Throwable ex) {
            thread.getThreadGroup().uncaughtException(thread, ex);
        }
    }

    public static void join(ThreadGroup threadGroup) {
        boolean hasNonDaemonThreads;
        do {
            hasNonDaemonThreads = false;
            Thread[] threads = new Thread[threadGroup.activeCount()];
            threadGroup.enumerate(threads);
            for (Thread thread : threads) {
                if (thread != null && !thread.isDaemon()) {
                    try {
                        hasNonDaemonThreads = true;
                        thread.join();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } while (hasNonDaemonThreads);
    }

}