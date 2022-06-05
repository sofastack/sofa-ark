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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WithListFields extends NonCommonJavaObject {
    List         listFromArray    = Arrays.asList(new NonCommonJavaType[] {
            new WithPrimitiveFields(), new WithStringField(Triangulate.anyString()) });
    private List list             = new ArrayList();
    {
        list.add(Triangulate.anyString());
        list.add(Triangulate.anyInteger());
        list.add(Triangulate.anyString());
    }
    private List empty            = Collections.EMPTY_LIST;
    private List unmodifiable     = Collections.unmodifiableList(list);
    private List synchronizedList = Collections.synchronizedList(list);
    private List singelton        = Collections.singletonList(Triangulate.anyInteger());
}
