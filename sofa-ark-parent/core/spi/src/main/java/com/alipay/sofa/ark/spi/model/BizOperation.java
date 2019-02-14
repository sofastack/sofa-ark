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

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class BizOperation {
    private String  bizName;
    private String  bizVersion;
    private String  bizUrl;
    private OperationType operationType;

    public boolean isValid() {
        return operationType == OperationType.UNKNOWN;
    }

    public String getBizName() {
        return bizName;
    }

    public BizOperation setBizName(String bizName) {
        this.bizName = bizName;
        return this;
    }

    public String getBizVersion() {
        return bizVersion;
    }

    public BizOperation setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
        return this;
    }

    public String getBizUrl() {
        return bizUrl;
    }

    public BizOperation setBizUrl(String bizUrl) {
        this.bizUrl = bizUrl;
        return this;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public BizOperation setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public static OperationType transformOperationType(String operation) {
        if (OperationType.INSTALL.name().equalsIgnoreCase(operation)) {
            return OperationType.INSTALL;
        } else if (OperationType.UNINSTALL.name().equalsIgnoreCase(operation)) {
            return OperationType.UNINSTALL;
        } else if (OperationType.SWITCH.name().equalsIgnoreCase(operation)) {
            return OperationType.SWITCH;
        } else if (OperationType.CHECK.name().equalsIgnoreCase(operation)) {
            return OperationType.CHECK;
        }
        return OperationType.UNKNOWN;
    }

    public enum OperationType {
        INSTALL, UNINSTALL, SWITCH, CHECK, UNKNOWN
    }
}