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
import com.alipay.sofa.ark.spi.configurator.ArkConfigHook;
import com.alipay.sofa.ark.spi.configurator.ArkConfigListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final static ConcurrentMap<String, Object>                  CFG          = new ConcurrentHashMap<String, Object>();

    /**
     * Configuration Listener
     */
    private final static ConcurrentMap<String, List<ArkConfigListener>> CFG_LISTENER = new ConcurrentHashMap<String, List<ArkConfigListener>>();

    /**
     * Configuration Hook
     */
    private final static ConcurrentMap<String, List<ArkConfigHook>>     CFG_HOOK     = new ConcurrentHashMap<String, List<ArkConfigHook>>();

    static {
        init(); // 加载配置文件
    }

    private static void init() {
        try {
            // load file configs
            loadConfigFile("config/ark/bootstrap.json");

            // load system properties
            CFG.putAll(new HashMap(System.getProperties())); // 注意部分属性可能被覆盖为字符串
        } catch (Exception e) {
            throw new ArkRuntimeException("Catch Exception when load ArkConfigs", e);
        }
    }

    /**
     * 加载自定义配置文件
     *
     * @param fileName 文件名
     * @throws IOException 加载异常
     */
    private static void loadConfigFile(String fileName) throws IOException {
    }

}