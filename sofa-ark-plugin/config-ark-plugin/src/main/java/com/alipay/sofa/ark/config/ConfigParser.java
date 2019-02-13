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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.config.ConfigCommand;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ConfigParser {

    public static ConfigCommand parseConfig(String config) {
        AssertUtils.assertNotNull(config, "Config should not be null.");
        String[] configStr = config.split(Constants.CONFIG_PUBLISH_SPLIT);
        Map<String, String> configMap = new HashMap<>();
        for (String kv : configStr) {
            AssertUtils.isTrue(kv.contains(Constants.CONFIG_PUBLISH_KV),
                "Config content is invalid.");
            String[] kvPair = kv.split(Constants.CONFIG_PUBLISH_KV);
            configMap.put(kvPair[0], kvPair[1]);
        }

        return transformConfigCommand(configMap);
    }

    public static ConfigCommand transformConfigCommand(Map<String, String> configMap) {
        ConfigCommand configCommand = new ConfigCommand();
        configCommand.setCommand(configMap.get("command")).setBizName(configMap.get("bizName"))
            .setBizVersion(configMap.get("bizVersion")).setBizUrl(configMap.get("url"));
        boolean isValidConfig = "install".equalsIgnoreCase(configCommand.getCommand())
                                || "uninstall".equalsIgnoreCase(configCommand.getCommand())
                                || "switch".equalsIgnoreCase(configCommand.getCommand());
        isValidConfig = isValidConfig && !StringUtils.isEmpty(configCommand.getBizName())
                        && !StringUtils.isEmpty(configCommand.getBizVersion());
        return configCommand.setValid(isValidConfig);
    }

}