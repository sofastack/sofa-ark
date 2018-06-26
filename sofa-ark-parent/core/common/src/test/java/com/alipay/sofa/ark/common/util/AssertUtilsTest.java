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
package com.alipay.sofa.ark.common.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class AssertUtilsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testAssertNotNullNull() {
        String msg = "object is null";
        try {
            AssertUtils.assertNotNull(null, msg);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
            Assert.assertTrue(e.getMessage().contains(msg));
            throw e;
        }
    }

    @Test
    public void testAssertNotNullNotNull() {
        AssertUtils.assertNotNull(new Object(), "object is null");
    }

    @Test
    public void testAssertIsTrue() {
        AssertUtils.isTrue(true, "Exception %s", "error");
        try {
            AssertUtils.isTrue(false, "Exception %s", "error");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Exception error".equals(ex.getMessage()));
        }
    }

    @Test
    public void testAssertIsFalse() {
        AssertUtils.isFalse(false, "Exception %s", "error");
        try {
            AssertUtils.isFalse(true, "Exception %s", "error");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue("Exception error".equals(ex.getMessage()));
        }
    }

}