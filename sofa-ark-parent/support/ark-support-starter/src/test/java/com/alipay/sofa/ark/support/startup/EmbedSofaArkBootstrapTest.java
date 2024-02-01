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
package com.alipay.sofa.ark.support.startup;

import com.alipay.sofa.ark.container.ArkContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap.deployStaticBizAfterEmbedMasterBizStarted;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmbedSofaArkBootstrapTest {

    private ArkContainer arkContainer = mock(ArkContainer.class);

    private Object       originalArkContainer;

    @Before
    public void setUp() {
        originalArkContainer = EmbedSofaArkBootstrap.arkContainer;
    }

    @After
    public void tearDown() {
        EmbedSofaArkBootstrap.arkContainer = originalArkContainer;
    }

    @Test
    public void testDeployStaticBizAfterEmbedMasterBizStarted() throws Exception {
        when(arkContainer.deployBizAfterMasterBizReady()).thenReturn(arkContainer);
        EmbedSofaArkBootstrap.arkContainer = arkContainer;
        deployStaticBizAfterEmbedMasterBizStarted();
    }

    @Test(expected = RuntimeException.class)
    public void testDeployStaticBizAfterEmbedMasterBizStartedWithNull() throws Exception {
        EmbedSofaArkBootstrap.arkContainer = null;
        deployStaticBizAfterEmbedMasterBizStarted();
    }

    @Test
    public void testEntryMethod() {
        EntryMethod entryMethod = new EntryMethod();
        assertTrue(entryMethod.getMethodName().contains("main"));
    }
}
