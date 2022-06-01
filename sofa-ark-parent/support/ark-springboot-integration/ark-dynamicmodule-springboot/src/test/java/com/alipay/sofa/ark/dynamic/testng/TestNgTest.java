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
package com.alipay.sofa.ark.dynamic.testng;

import com.alipay.sofa.ark.dynamic.BaseTest;
import com.alipay.sofa.ark.dynamic.support.testng.AbstractTestNGSofaArkContextTests;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author hanyue
 * @version : TestNgTest.java, v 0.1 2022年05月31日 11:42 AM hanyue Exp $
 */
public class TestNgTest extends BaseTest {

    private static int i;

    @BeforeTest
    public void beforeTest() {
        i = 1;
    }

    @BeforeClass
    public void beforeClass() {
        Assert.assertEquals(i, 1);
        i = 2;
    }

    @Test
    public void test() {
        Assert.assertEquals(i, 2);
        i = 3;
    }

    @AfterClass
    public void afterClass() {
        Assert.assertEquals(i, 3);
        i = 4;
    }

    @AfterTest
    public void afterTest() {
        Assert.assertEquals(i, 4);
    }
}