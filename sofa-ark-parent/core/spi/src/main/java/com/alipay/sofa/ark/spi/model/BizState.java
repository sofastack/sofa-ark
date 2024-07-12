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
 * Biz State
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public enum BizState {
    /**
     * install not started yet
     */
    UNRESOLVED("unresolved"),
    /**
     * installing
     */
    RESOLVED("resolved"),

    /**
     * install succeed, and start serving
     */
    ACTIVATED("activated"),

    /**
     * uninstall
     */
    DEACTIVATED("deactivated"),

    /**
     * install failed.
     */
    BROKEN("broken");

    private String state;

    BizState(String state) {
        this.state = state;
    }

    public String getBizState() {
        return state;
    }

    @Override
    public String toString() {
        return getBizState();
    }

    public static BizState of(String state) {
        if (UNRESOLVED.name().equalsIgnoreCase(state)) {
            return UNRESOLVED;
        } else if (RESOLVED.name().equalsIgnoreCase(state)) {
            return RESOLVED;
        } else if (ACTIVATED.name().equalsIgnoreCase(state)) {
            return ACTIVATED;
        } else if (DEACTIVATED.name().equalsIgnoreCase(state)) {
            return DEACTIVATED;
        } else {
            return BROKEN;
        }
    }
}
