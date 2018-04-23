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
package com.alipay.sofa.ark.spi.tools;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * runtime utils
 *
 * @author joe
 * @since 0.3.0
 */
public class RuntimeUtil {
    /**
     * get classpath from ClassLoader
     *
     * @param classLoader ClassLoader
     * @return classpath
     */
    public static URL[] getClasspath(ClassLoader classLoader) {
        if (classLoader == null) {
            return new URL[0];
        }
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        } else {
            return getClasspath(classLoader.getParent());
        }
    }
}
