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
package com.alipay.sofa.ark.plugin.mojo;

import java.util.*;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class LinkedProperties extends Properties {

    private static final long             serialVersionUID = 1L;
    private LinkedHashMap<Object, Object> linkedHashMap    = new LinkedHashMap<>(20);

    @Override
    public synchronized Object put(Object key, Object value) {
        linkedHashMap.put(key, value);
        return super.put(key, value);
    }

    @Override
    public synchronized Object remove(Object key) {
        linkedHashMap.remove(key);
        return super.remove(key);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(linkedHashMap.keySet());
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return linkedHashMap.entrySet();
    }
}