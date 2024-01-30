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
package com.alipay.sofa.ark.support.runner;

import com.alipay.sofa.ark.bootstrap.ContainerClassLoader;
import com.alipay.sofa.ark.container.test.TestClassLoader;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
@RunWith(ArkJUnit4Runner.class)
public class ArkJUnit4RunnerTest {

    private static String state;

    @BeforeClass
    public static void beforeClass() {
        state = "@BeforeClass";
    }

    @Before
    public void before() {
        state = "@Before";
    }

    @Test
    public void test() {
        ClassLoader testClassLoader = getClass().getClassLoader();
        Assert.assertTrue(testClassLoader.getClass().getCanonicalName()
            .equals(TestClassLoader.class.getCanonicalName()));

        Assert.assertTrue("@Before".equals(state));
        state = "@Test";

        ClassLoader testClCl = testClassLoader.getClass().getClassLoader();
        Assert.assertTrue(testClCl.getClass().getCanonicalName()
            .equals(ContainerClassLoader.class.getCanonicalName()));
    }

    @Test
    public void testJUnitRunner() {
        try {
            Assert.assertTrue("@Before".equals(state));
            state = "@Test";

            ArkJUnit4Runner runner = new ArkJUnit4Runner(ArkJUnit4RunnerTest.class);
            ClassLoader loader = runner.getTestClass().getJavaClass().getClassLoader();
            Assert.assertTrue(loader.getClass().getCanonicalName()
                .equals(TestClassLoader.class.getCanonicalName()));
        } catch (InitializationError error) {
            Assert.fail(error.getMessage());
        }
    }

    @After
    public void after() {
        Assert.assertTrue("@Test".equals(state));
        state = "@After";
    }

    @AfterClass
    public static void afterClass() {
        Assert.assertTrue("@After".equals(state));
    }

}