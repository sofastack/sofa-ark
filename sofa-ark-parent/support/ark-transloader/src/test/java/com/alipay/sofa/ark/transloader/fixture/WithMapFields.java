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
package com.alipay.sofa.ark.transloader.fixture;

import com.alipay.sofa.ark.transloader.Triangulate;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class WithMapFields extends NonCommonJavaObject {
    private SortedMap map             = new TreeMap();
    {
        map.put(Triangulate.anyInteger(), Triangulate.anyString());
        map.put(Triangulate.anyInteger(), new SerializableWithFinalFields(Triangulate.anyInteger()));
    }

    private Map       unmodifiable    = Collections.unmodifiableMap(map);
    private Map       synchronizedMap = Collections.synchronizedSortedMap(map);
    private Map       singleton       = Collections.singletonMap(Triangulate.anyInteger(),
                                          Triangulate.anyString());
}
