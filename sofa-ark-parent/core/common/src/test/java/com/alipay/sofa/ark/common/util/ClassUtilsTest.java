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

import com.alipay.sofa.ark.spi.constant.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ClassUtilsTest {

    @Test
    public void testGetPackageName() {
        Assert.assertTrue(ClassUtils.getPackageName("a.b.C").equals("a.b"));
        Assert.assertTrue(ClassUtils.getPackageName("C").equals(Constants.DEFAULT_PACKAGE));
    }

    @Test
    public void testIsAdaptedToPackagePattern() {
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a", "a"));
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a.b", "a.b"));
        Assert.assertFalse(ClassUtils.isAdaptedToPackagePattern("a.b.c", "a"));
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a", "a.*"));
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a.b", "a.*"));
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a.b.c", "a.*"));
        Assert.assertTrue(ClassUtils.isAdaptedToPackagePattern("a.b.c.d", "a.*"));

    }

}