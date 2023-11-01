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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LinkedPropertiesTest {

    private LinkedProperties linkedProperties = new LinkedProperties();

    @Test
    public void testLinkedPropertiesAllMethods() {

        assertEquals(false, linkedProperties.keys().hasMoreElements());
        linkedProperties.put("a", "b");
        assertEquals(1, linkedProperties.entrySet().size());

        linkedProperties.remove("a");
        assertEquals(0, linkedProperties.entrySet().size());

        Map<Object, Object> properties = new HashMap<>();
        properties.put("c", "d");
        properties.put("e", "f");
        linkedProperties.putAll(properties);
        assertEquals(2, linkedProperties.entrySet().size());
    }
}
