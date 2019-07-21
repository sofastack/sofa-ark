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
package com.alipay.sofa.ark.spi.replay;

import java.util.Stack;

/**
 * @author qilong.zql 17/11/10-12:15
 * @author guolei.sgl 19/7/21 16:48
 */
public class ReplayContext {

    private static final ThreadLocal<Stack<String>> moduleVersions = new ThreadLocal<Stack<String>>() {
                                                                       @Override
                                                                       protected Stack<String> initialValue() {
                                                                           return new Stack<String>();
                                                                       }
                                                                   };

    public static final String                      PLACEHOLDER    = "__call_placeholder__";

    public static void set(String version) {
        moduleVersions.get().push(version);
    }

    public static void unset() {
        moduleVersions.get().pop();
    }

    public static String get() {
        if (moduleVersions.get().size() == 0) {
            return null;
        }
        return moduleVersions.get().peek();
    }

    public static void setPlaceHolder() {
        // PLACEHOLDER needs to be placed when the link is currently invoked at the version number
        if (moduleVersions.get().size() != 0) {
            moduleVersions.get().push(PLACEHOLDER);
        }
    }

    public static void clearPlaceHolder() {
        if (PLACEHOLDER.equals(get())) {
            moduleVersions.get().pop();
        }
    }

}