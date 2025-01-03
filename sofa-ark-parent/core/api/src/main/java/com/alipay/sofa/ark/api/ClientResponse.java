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

import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.ark.spi.model.Plugin;

import java.util.Set;

/**
 * API operation response
 *
 * @author qilong.zql
 * @since 0.6.0
 */
public class ClientResponse {

    private String       message;
    private ResponseCode code;
    private Set<BizInfo> bizInfos;
    private Set<Plugin>  pluginInfos;

    public String getMessage() {
        return message;
    }

    public ClientResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    public ResponseCode getCode() {
        return code;
    }

    public ClientResponse setCode(ResponseCode code) {
        this.code = code;
        return this;
    }

    public Set<BizInfo> getBizInfos() {
        return bizInfos;
    }

    public ClientResponse setBizInfos(Set<BizInfo> bizInfos) {
        this.bizInfos = bizInfos;
        return this;
    }

    public Set<Plugin> getPluginInfos() {
        return pluginInfos;
    }

    public ClientResponse setPluginInfos(Set<Plugin> pluginInfos) {
        this.pluginInfos = pluginInfos;
        return this;
    }
}