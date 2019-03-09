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

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class RegistryConfig {
    /**
     * protocol
     */
    private String              protocol;

    /**
     * config server address
     */
    private String              address;

    /**
     * config parameters
     */
    private Map<String, String> parameters;

    public String getProtocol() {
        return protocol;
    }

    public RegistryConfig setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public RegistryConfig setAddress(String address) {
        this.address = address;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public RegistryConfig setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public String getParameter(String key) {
        return getParameters().get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        return value == null ? defaultValue : value;
    }

    public int getConnectTimeout() {
        return ArkConfigs.getIntValue(Constants.CONFIG_CONNECT_TIMEOUT,
            Constants.DEFAULT_CONFIG_CONNECT_TIMEOUT);
    }
}