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

public class PluginConfig {

    /**
     * Plugin部署参数指定name
     */
    private String specifiedName;

    /**
     * Plugin部署参数指定version
     */
    private String specifiedVersion;

    /**
     * Plugin扩展classpath
     */
    private URL[]  extensionUrls;

    public String getSpecifiedName() {
        return specifiedName;
    }

    public void setSpecifiedName(String specifiedName) {
        this.specifiedName = specifiedName;
    }

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
}
