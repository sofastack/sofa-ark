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
package com.alipay.sofa.ark.api;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author qilong.zql
 * @author GengZhang
 * @since 0.6.0
 */
public class ArkConfigs {

    /**
     * Global Configuration
     */
    private final static ConcurrentMap<String, Object> CFG = new ConcurrentHashMap<String, Object>();

    /**
     * executed only once
     */
    public static void init(List<URL> confFiles) {
        try {
            // load file configs
            for (URL url : confFiles) {
                loadConfigFile(url.openStream());
            }
        } catch (Exception e) {
            throw new ArkRuntimeException("Catch Exception when load ArkConfigs", e);
        }
    }

    /**
     * load conf file
     *
     * @param inputStream conf file
     * @throws IOException loading exception
     */
    private static void loadConfigFile(InputStream inputStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inputStream);
        for (Object key : properties.keySet()) {
            CFG.put((String) key, properties.get(key));
        }
    }

    /**
     * configure system property
     *
     * @param key
     * @param value
     */
    public static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    /**
     * clear system property
     *
     * @param key
     */
    public static String getSystemProperty(String key) {
        return System.getProperty(key);
    }

    /**
     * Get string value.
     *
     * @param primaryKey the primary key
     * @return the string value
     */
    public static String getStringValue(String primaryKey) {
        String val = getSystemProperty(primaryKey);
        if (val == null) {
            val = (String) CFG.get(primaryKey);
        }
        return val;
    }

    /**
     * Get string value.
     *
     * @param primaryKey the primary key
     * @param defaultValue
     * @return the string value
     */
    public static String getStringValue(String primaryKey, String defaultValue) {
        String val = getStringValue(primaryKey);
        return val == null ? defaultValue : val;
    }

    /**
     * Get int value.
     *
     * @param primaryKey the primary key
     * @param defaultValue
     * @return the int value
     */
    public static int getIntValue(String primaryKey, int defaultValue) {
        String val = getStringValue(primaryKey);
        return val == null ? defaultValue : Integer.valueOf(val);
    }

    /**
     * Get ArkConfigs key set
     *
     * @return
     */
    public static Set<String> keySet() {
        Set<String> keySet = new HashSet<>(CFG.keySet());
        keySet.addAll(new HashMap(System.getProperties()).keySet());
        return keySet;
    }

    /**
     * put string config
     * @param key
     * @param value
     */
    public static void putStringValue(String key, String value) {
        CFG.put(key, value);
    }

    public static boolean isEmbedEnable() {
        return Boolean.valueOf(System.getProperty(Constants.EMBED_ENABLE));
    }

    public static void setEmbedEnable(boolean enable) {
        System.setProperty(Constants.EMBED_ENABLE, enable ? "true" : "false");
    }
}