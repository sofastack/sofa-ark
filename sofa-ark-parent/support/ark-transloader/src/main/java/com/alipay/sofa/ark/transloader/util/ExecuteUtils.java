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
package com.alipay.sofa.ark.transloader.util;

import java.util.function.Supplier;

/**
 * @author hanyue
 * @version : ExecuteUtils.java, v 0.1 2022年06月04日 1:36 PM hanyue Exp $
 */
public class ExecuteUtils {

    public static <T> T executeAttachExecption(Supplier<T> supplier, Throwable parent) {
        Assert.areNotNull(supplier, parent);
        try {
            return supplier.get();
        } catch (Exception child) {
            child.setStackTrace(parent.getStackTrace());
            throw child;
        }
    }
}