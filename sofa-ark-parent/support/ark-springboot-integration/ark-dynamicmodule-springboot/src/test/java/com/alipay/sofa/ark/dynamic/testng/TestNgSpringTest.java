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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author hanyue
 * @version : TestNgSpringTest.java, v 0.1 2022年05月31日 11:49 AM hanyue Exp $
 */
public class TestNgSpringTest extends BaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    private static int         i;

    @BeforeTest
    public void beforeTest() {
        Assert.assertNull(applicationContext);
        i = 1;
    }

    @BeforeClass
    public void beforeClass() {
        Assert.assertEquals(i, 1);
        Assert.assertNotNull(applicationContext);
        Object testBean = applicationContext.getBean("testBean");
        Assert.assertNotNull(testBean);
        i = 2;
    }

    @Test
    public void test() {
        Assert.assertEquals(i, 2);
        Assert.assertNotNull(applicationContext);
        Object testBean = applicationContext.getBean("testBean");
        Assert.assertNotNull(testBean);
        i = 3;
    }

    @AfterClass
    public void afterClass() {
        Assert.assertEquals(i, 3);
        Assert.assertNotNull(applicationContext);
        Object testBean = applicationContext.getBean("testBean");
        Assert.assertNotNull(testBean);
        i = 4;
    }

    @AfterTest
    public void afterTest() {
        Assert.assertEquals(i, 4);
        Assert.assertNotNull(applicationContext);
        Object testBean = applicationContext.getBean("testBean");
        Assert.assertNotNull(testBean);
    }
}