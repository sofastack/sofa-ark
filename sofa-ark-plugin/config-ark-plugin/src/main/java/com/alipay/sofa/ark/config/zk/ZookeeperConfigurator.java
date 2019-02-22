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
package com.alipay.sofa.ark.config.zk;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.config.RegistryConfig;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qilong.zql
 * @author LiWei
 * @since 0.6.0
 */
public class ZookeeperConfigurator {
    public static RegistryConfig buildConfig(String config) {
        String zkAddress = parseAddress(config);
        Map<String, String> parameters = parseParam(config);
        return new RegistryConfig().setAddress(zkAddress)
            .setProtocol(Constants.CONFIG_PROTOCOL_ZOOKEEPER).setParameters(parameters);
    }

    /**
     * parse address
     *
     * @param config config value
     */
    public static String parseAddress(String config) {
        String address = null;
        if (!StringUtils.isEmpty(config) && config.startsWith(Constants.CONFIG_PROTOCOL_ZOOKEEPER)) {
            final String zkProtocol = Constants.CONFIG_PROTOCOL_ZOOKEEPER + "://";
            String value = config.substring(zkProtocol.length());
            if (!value.contains("?")) {
                address = value;
            } else {
                int index = value.lastIndexOf('?');
                address = value.substring(0, index);
            }
        }
        return address;
    }

    /**
     * parse params
     *
     * @param config
     * @return
     */
    public static Map<String, String> parseParam(String config) {
        Map<String, String> map = new HashMap<String, String>();
        String host = parseAddress(config);
        AssertUtils.isFalse(StringUtils.isEmpty(host), "host must not be empty.");
        String paramString = config.substring(config.indexOf(host) + host.length());
        if (StringUtils.isEmpty(paramString)) {
            return map;
        }
        if (paramString.contains("&")) {
            String[] paramSplit = paramString.split("&");
            for (String param : paramSplit) {
                Map<String, String> tempMap = parseKeyValue(param);
                map.putAll(tempMap);
            }
        } else {
            Map<String, String> tempMap = parseKeyValue(paramString);
            map.putAll(tempMap);
        }

        return map;
    }

    /**
     * parse parameter
     *
     * @param kv
     * @return
     */
    public static Map<String, String> parseKeyValue(String kv) {
        AssertUtils.isFalse(StringUtils.isEmpty(kv), "Config parameter should not empty.");
        Map<String, String> map = new HashMap<String, String>();
        String[] kvSplit = kv.split("=");
        AssertUtils.isTrue(kvSplit.length == 2,
            String.format("Config parameter %s is invalid format.", kv));
        String key = kvSplit[0];
        String value = kvSplit[1];
        map.put(key, value);
        return map;
    }
}