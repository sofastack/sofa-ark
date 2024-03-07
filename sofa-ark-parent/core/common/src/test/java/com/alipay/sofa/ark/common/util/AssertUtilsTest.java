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

import org.junit.Test;

import java.io.File;

import static com.alipay.sofa.ark.common.util.AssertUtils.*;
import static com.alipay.sofa.ark.common.util.FileUtils.file;
import static java.lang.System.getProperty;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class AssertUtilsTest {

    public static File getTmpDir() {
        String tmpPath = getProperty("java.io.tmpdir");
        return file(tmpPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssertNotNullNull() {
        String msg = "object is null";
        try {
            assertNotNull(null, msg);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertTrue(e.getMessage().contains(msg));
            throw e;
        }
    }

    @Test
    public void testAssertNotNullNotNull() {
        assertNotNull(new Object(), "object is null");
    }

    @Test
    public void testAssertIsTrue() {
        isTrue(true, "Exception %s", "error");
        try {
            isTrue(false, "Exception %s", "error");
        } catch (IllegalArgumentException ex) {
            assertTrue("Exception error".equals(ex.getMessage()));
        }
    }

    @Test
    public void testAssertIsFalse() {
        isFalse(false, "Exception %s", "error");
        try {
            isFalse(true, "Exception %s", "error");
        } catch (IllegalArgumentException ex) {
            assertTrue("Exception error".equals(ex.getMessage()));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void assertNull() {
        AssertUtils.assertNull(new Object(), "should be nul!");
    }
}
