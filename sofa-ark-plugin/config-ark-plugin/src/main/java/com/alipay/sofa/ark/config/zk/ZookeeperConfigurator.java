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
        AssertUtils.isFalse(StringUtils.isEmpty(config), "Zookeeper config should not be empty.");
        AssertUtils.isTrue(config.startsWith(Constants.CONFIG_PROTOCOL_ZOOKEEPER_HEADER),
            "Zookeeper config should start with 'zookeeper://'.");
        String value = config.substring(Constants.CONFIG_PROTOCOL_ZOOKEEPER_HEADER.length());
        int idx = value.indexOf(Constants.QUESTION_MARK_SPLIT);
        String address = (idx == -1) ? value : value.substring(0, idx);
        AssertUtils.isFalse(StringUtils.isEmpty(address), "Zookeeper address should not be empty");
        return address;
    }

    /**
     * parse params
     *
     * @param config
     * @return
     */
    public static Map<String, String> parseParam(String config) {
        AssertUtils.assertNotNull(config, "Params should not be null.");
        Map<String, String> map = new HashMap<>();
        int idx = config.indexOf(Constants.QUESTION_MARK_SPLIT);
        String paramString = (idx == -1) ? Constants.EMPTY_STR : config.substring(idx + 1);
        String[] paramSplit = paramString.split(Constants.AMPERSAND_SPLIT);
        for (String param : paramSplit) {
            if (!StringUtils.isEmpty(param)) {
                String[] kvSplit = param.split(Constants.EQUAL_SPLIT);
                AssertUtils.isTrue(kvSplit.length == 2,
                    String.format("Config parameter %s is invalid format.", kvSplit));
                map.put(kvSplit[0], kvSplit[1]);
            }
        }
        return map;
    }
}