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
package com.alipay.sofa.ark.container.test;

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.container.ArkContainerTest;
import org.junit.Test;

import java.net.URL;

import static com.alipay.sofa.ark.container.ArkContainer.main;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHelperTest {

    private URL jarURL = ArkContainerTest.class.getClassLoader().getResource("test.jar");

    @Test
    public void testCreateNoneDelegateTestClassLoader() {
        ArkContainer arkContainer = null;
        try {
            String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm(),
                    "-Aclasspath=" + jarURL.toString() };
            arkContainer = (ArkContainer) main(args);
            TestHelper testHelper = new TestHelper(arkContainer);
            assertTrue(testHelper.isStarted());
            assertEquals(NoneDelegateTestClassLoader.class, testHelper
                .createNoneDelegateTestClassLoader().getClass());
        } finally {
            if (arkContainer != null) {
                arkContainer.stop();
            }
        }
    }
}
