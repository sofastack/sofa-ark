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
package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.spi.constant.Constants;

import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ClassUtils {

    /**
     * Get package name from a specified class name.
     *
     * @param className
     * @return
     */
    public static String getPackageName(String className) {
        AssertUtils.isFalse(StringUtils.isEmpty(className), "ClassName should not be empty!");
        int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(0, index);
        }
        return Constants.DEFAULT_PACKAGE;
    }

    public static String getCodeBase(Class<?> cls) {

        if (cls == null) {
            return null;
        }
        ProtectionDomain domain = cls.getProtectionDomain();
        if (domain == null) {
            return null;
        }
        CodeSource source = domain.getCodeSource();
        if (source == null) {
            return null;
        }
        URL location = source.getLocation();
        if (location == null) {
            return null;
        }
        return location.getFile();
    }
}