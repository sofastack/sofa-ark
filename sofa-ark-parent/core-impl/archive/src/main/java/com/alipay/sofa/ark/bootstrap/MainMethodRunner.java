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
package com.alipay.sofa.ark.bootstrap;

import java.lang.reflect.Method;

/**
 * Utility class that is used  to call a main method. The class
 * containing the main method is loaded using the thread context class loader.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class MainMethodRunner {

    private final String   mainClassName;

    private final String[] args;

    /**
     * Create a new {@link MainMethodRunner} instance.
     * @param mainClass the main class
     * @param args incoming arguments
     */
    public MainMethodRunner(String mainClass, String[] args) {
        this.mainClassName = mainClass;
        this.args = (args == null ? null : args.clone());
    }

    public Object run() throws Exception {
        Class<?> mainClass = Thread.currentThread().getContextClassLoader()
            .loadClass(this.mainClassName);
        Method mainMethod = mainClass.getDeclaredMethod("main", String[].class);
        return mainMethod.invoke(null, new Object[] { this.args });
    }

}