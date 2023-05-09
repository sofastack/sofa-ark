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
package com.alipay.sofa.ark.loader.test.util;

import com.alipay.sofa.ark.loader.util.ModifyPathUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author sususama
 * @since 2023-05-08
 */
public class ModifyPathUtilsTest {
    @Test
    public void modifyPathTest() {
        String path = "/C:/Users/XXX/Desktop/XXX-ark-biz.jar";
        Assert.assertEquals("C:/Users/XXX/Desktop/XXX-ark-biz.jar",
            ModifyPathUtils.modifyPath(path));
        String path1 = "C:/Users/XXX/Desktop/XXX-ark-biz.jar";
        Assert.assertEquals("C:/Users/XXX/Desktop/XXX-ark-biz.jar",
            ModifyPathUtils.modifyPath(path1));
        String path2 = "/home/user/XXX/test/XXX-ark-biz.jar";
        Assert.assertEquals("/home/user/XXX/test/XXX-ark-biz.jar",
            ModifyPathUtils.modifyPath(path2));
    }
}
