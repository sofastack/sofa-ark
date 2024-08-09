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

import java.io.File;
import java.util.List;

public class PluginOperation {

    private String        pluginName;

    private String        pluginVersion;

    private OperationType operationType;

    private String        url;

    private File          localFile;

    private List<String>  extensionLibs;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getLocalFile() {
        return localFile;
    }

    public void setLocalFile(File localFile) {
        this.localFile = localFile;
    }

    public List<String> getExtensionLibs() {
        return extensionLibs;
    }

    public void setExtensionLibs(List<String> extensionLibs) {
        this.extensionLibs = extensionLibs;
    }

    public enum OperationType {
        INSTALL, UNINSTALL
    }

}
