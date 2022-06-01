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
package com.alipay.sofa.ark.dynamic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author hanyue
 * @version : SimpileTest.java, v 0.1 2022年05月30日 4:33 PM hanyue Exp $
 */
public class SimpileTest extends BaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void test() {
        Assert.assertNotNull(applicationContext);
    }

    @Test
    public void testBean() {
        Object testBean = applicationContext.getBean("testBean");
        Assert.assertNotNull(testBean);
    }
}