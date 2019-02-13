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
package com.alipay.sofa.ark.spi.config;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ConfigCommand {
    String  bizName;
    String  bizVersion;
    String  bizUrl;
    String  command;
    boolean valid;

    public boolean isValid() {
        return valid;
    }

    public ConfigCommand setValid(boolean valid) {
        this.valid = valid;
        return this;
    }

    public String getBizName() {
        return bizName;
    }

    public ConfigCommand setBizName(String bizName) {
        this.bizName = bizName;
        return this;
    }

    public String getBizVersion() {
        return bizVersion;
    }

    public ConfigCommand setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
        return this;
    }

    public String getBizUrl() {
        return bizUrl;
    }

    public ConfigCommand setBizUrl(String bizUrl) {
        this.bizUrl = bizUrl;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public ConfigCommand setCommand(String command) {
        this.command = command;
        return this;
    }
}