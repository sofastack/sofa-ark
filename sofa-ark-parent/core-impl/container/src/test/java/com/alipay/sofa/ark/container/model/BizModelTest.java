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
package com.alipay.sofa.ark.container.model;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

import static org.junit.Assert.*;

public class BizModelTest {

    @Test
    public void testDoCheckDeclared() throws MalformedURLException {

        BizModel bizModel = new BizModel();
        assertEquals(new HashSet(), bizModel.setAttribute("a", "b").setAttributes(new HashMap<>())
            .getInjectExportPackages());
        assertEquals(new HashSet(), bizModel.getInjectPluginDependencies());
        bizModel.setCustomBizName("abc");
        assertNotNull(bizModel.getAttributes());
        assertNull(bizModel.getBizTempWorkDir());
        bizModel.toString();

        bizModel.setPluginClassPath(new URL[] { new URL("file://b/a.jar!/") });
        assertTrue(bizModel.doCheckDeclared("file://b/a.jar!/b.jar"));
        assertTrue(bizModel.doCheckDeclared(this.getClass().getClassLoader()
            .getResource("test.jar").getPath()));
    }
}
