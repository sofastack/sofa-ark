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
package com.alipay.sofa.ark.sample;

import com.alipay.sofa.ark.support.listener.TestNGOnArk;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * A TestNGTest Demo
 *
 * @author qilong.zql
 * @since 0.3.0
 */
@TestNGOnArk
public class TestNGTest {

    public static final String TEST_CLASSLOADER = "com.alipay.sofa.ark.container.test.TestClassLoader";

    @Test
    public void test() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = this.getClass().getClassLoader();
        Assert.assertTrue(tccl.equals(loader));
        Assert.assertTrue(TEST_CLASSLOADER.equals(tccl.getClass().getCanonicalName()));
    }

}