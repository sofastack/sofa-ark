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

import com.alipay.sofa.ark.spi.model.BizInfo.BizStateRecord;
import com.alipay.sofa.ark.spi.model.BizState;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void testBizStateChanged() {
        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz1");
        bizModel.setBizVersion("0.0.1-SNAPSHOT");
        List<BizStateRecord> changeLogs = bizModel.getBizStateRecords();

        assertEquals(0, changeLogs.size());

        // create Biz
        bizModel.setBizState(BizState.RESOLVED);
        bizModel.setClassLoader(this.getClass().getClassLoader());
        assertEquals(1, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));

        // activate Biz
        bizModel.setBizState(BizState.ACTIVATED);
        assertEquals(2, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));

        // deactivate Biz
        bizModel.setBizState(BizState.DEACTIVATED);
        assertEquals(3, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));
        assertTrue(bizModel.toString().contains("-> deactivated"));

        bizModel.setBizState(BizState.UNRESOLVED);
        assertEquals(4, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));
        assertTrue(bizModel.toString().contains("-> deactivated"));
        assertTrue(bizModel.toString().contains("-> unresolved"));
    }
}
