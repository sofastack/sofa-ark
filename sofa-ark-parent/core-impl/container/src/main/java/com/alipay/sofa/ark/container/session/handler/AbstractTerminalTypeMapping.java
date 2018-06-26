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
package com.alipay.sofa.ark.container.session.handler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Map special key value according to different terminal type
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public abstract class AbstractTerminalTypeMapping {

    public static String getDefaultTerminalType() {
        return File.separatorChar == '/' ? "XTERM" : "ANSI";
    }

    protected Map<String, KEYS> escKeys;

    protected byte              backSpace;

    protected byte              del;

    public AbstractTerminalTypeMapping(byte backSpace, byte del) {
        this.backSpace = backSpace;
        this.del = del;
        escKeys = new HashMap<>();
        escKeys.put("[C", KEYS.RIGHT);
        escKeys.put("[D", KEYS.LEFT);
        escKeys.put("[3~", KEYS.DEL);
    }

    public byte getBackspace() {
        return backSpace;
    }

    public byte getDel() {
        return del;
    }

    public KEYS getMatchKeys(String str) {
        if (escKeys.get(str) != null) {
            return escKeys.get(str);
        }
        if (isPossibleEscKeys(str)) {
            return KEYS.UNFINISHED;
        }
        return KEYS.UNKNOWN;
    }

    protected boolean isPossibleEscKeys(String str) {
        for (String key : escKeys.keySet()) {
            if (key.startsWith(str)) {
                return true;
            }
        }
        return false;
    }

    public enum KEYS {
        RIGHT, LEFT, DEL, UNFINISHED, UNKNOWN
    }

}