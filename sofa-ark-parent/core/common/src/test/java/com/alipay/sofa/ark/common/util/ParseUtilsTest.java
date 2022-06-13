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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/11/23 11:18 AM
 * @since
 **/
public class ParseUtilsTest {

    Set<String> candidates  = new HashSet<>();

    Set<String> resources   = new HashSet<>();

    Set<String> stems       = new HashSet<>();

    Set<String> suffixStems = new HashSet<>();

    @Before
    public void before() {
        candidates.add("spring.xsd");
        candidates.add("*.xsd");
        candidates.add("spring/*");
        candidates.add(Constants.PACKAGE_PREFIX_MARK);
    }

    @Test
    public void testParseUtils() {
        ParseUtils.parseResourceAndStem(candidates, stems, suffixStems, resources);
        Assert.assertTrue(stems.size() == 1 && stems.contains("spring/"));
        Assert.assertTrue(resources.size() == 1 && resources.contains("spring.xsd"));
        Assert.assertTrue(suffixStems.size() == 1 && suffixStems.contains(".xsd"));
    }

    @After
    public void after() {
        candidates.clear();
        resources.clear();
        stems.clear();
        suffixStems.clear();
        candidates = null;
        resources = null;
        suffixStems = null;
        stems = null;
    }
}
