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
package com.alipay.sofa.ark.spi.model;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class BizConfig {

    /**
     * Biz部署参数指定version
     */
    private String       specifiedVersion;

    /**
     * Biz扩展classpath
     */
    private URL[]        extensionUrls;

    /**
     * Biz依赖的plugins
     */
    private List<String> dependentPlugins;

    String[]             args;

    Map<String, String>  envs;

    public String getSpecifiedVersion() {
        return specifiedVersion;
    }

    public void setSpecifiedVersion(String specifiedVersion) {
        this.specifiedVersion = specifiedVersion;
    }

    public URL[] getExtensionUrls() {
        return extensionUrls;
    }

    public void setExtensionUrls(URL[] extensionUrls) {
        this.extensionUrls = extensionUrls;
    }

    public List<String> getDependentPlugins() {
        return dependentPlugins;
    }

    public void setDependentPlugins(List<String> dependentPlugins) {
        this.dependentPlugins = dependentPlugins;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }
}
