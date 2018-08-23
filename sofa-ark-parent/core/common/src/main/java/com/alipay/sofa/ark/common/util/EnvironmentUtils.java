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

import java.util.Properties;

/**
 * a utils class to get environment properties
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class EnvironmentUtils {

    private static Properties properties = new Properties();

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return System.getProperty(key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return System.getProperty(key, defaultValue);
        }
        return value;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public static void clearProperty(String key) {
        System.clearProperty(key);
    }

}