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
package com.alipay.sofa.ark.api;

import org.junit.Test;

import java.net.URL;

import static com.alipay.sofa.ark.api.ArkConfigs.getStringValue;
import static com.alipay.sofa.ark.api.ArkConfigs.init;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ArkConfigsTest {

    @Test
    public void testLoadConfigFile() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("test.props");
        init(asList(resource));
        assertEquals("b123", getStringValue("a123"));
        assertEquals("d123", getStringValue("c123"));
    }
}
