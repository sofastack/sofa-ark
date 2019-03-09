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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class BizOperation {
    private String              bizName;
    private String              bizVersion;
    private OperationType       operationType;
    private Map<String, String> parameters = new HashMap<>();

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

    public OperationType getOperationType() {
        return operationType;
    }

    public BizOperation setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public BizOperation setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public BizOperation putParameter(String key, String value) {
        this.parameters.put(key, value);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof BizOperation)) {
            return false;
        }
        BizOperation that = (BizOperation) obj;
        if (!Objects.equals(this.getBizName(), that.getBizName())) {
            return false;
        }
        if (!Objects.equals(this.getBizVersion(), that.getBizVersion())) {
            return false;
        }
        if (!Objects.equals(this.getOperationType(), that.getOperationType())) {
            return false;
        }
        return true;
    }

    public static BizOperation createBizOperation() {
        return new BizOperation();
    }

    public enum OperationType {
        INSTALL, UNINSTALL, SWITCH, CHECK, UNKNOWN
    }
}