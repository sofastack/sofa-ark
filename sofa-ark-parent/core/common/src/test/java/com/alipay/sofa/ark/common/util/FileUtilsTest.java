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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/7/28 11:24 PM
 * @since
 **/
public class FileUtilsTest {

    private static final String ORIGIN = System.getProperty("os.name");

    @Before
    public void before() {
        System.setProperty("os.name", "windows");
    }

    @After
    public void after() {
        System.setProperty("os.name", ORIGIN);
    }

    @Test
    public void testGetCompatiblePath() {
        String winPath = FileUtils.getCompatiblePath("C:\\a\\b\\c");
        Assert.assertTrue(winPath.contains("/"));
        String macPath = FileUtils.getCompatiblePath("/a/b/c");
        Assert.assertTrue(winPath.contains(macPath));
    }
}
