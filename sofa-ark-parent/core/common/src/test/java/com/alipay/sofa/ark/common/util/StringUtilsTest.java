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

import java.util.*;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class StringUtilsTest {

    @Test
    public void testEmpty() {
        Assert.assertTrue(StringUtils.isEmpty(""));
        Assert.assertTrue(StringUtils.isEmpty(null));
        Assert.assertFalse(StringUtils.isEmpty("aaa"));
    }

    @Test
    public void testSameStr() {
        Assert.assertTrue(StringUtils.isSameStr(null, null));
        Assert.assertFalse(StringUtils.isSameStr(null, ""));
        Assert.assertFalse(StringUtils.isSameStr(null, "a"));

        Assert.assertFalse(StringUtils.isSameStr("aa", null));
        Assert.assertFalse(StringUtils.isSameStr("aa", ""));
        Assert.assertFalse(StringUtils.isSameStr("aa", "a"));
        Assert.assertTrue(StringUtils.isSameStr("aa", "aa"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testListToStr() {
        Assert.assertTrue("".equals(StringUtils.setToStr(null, ",")));
        Assert.assertTrue("".equals(StringUtils.setToStr(Collections.<String> emptySet(), ",")));
        Assert.assertTrue("ast".equals(StringUtils.setToStr(Collections.singleton("ast"), "&&")));

        LinkedHashSet linkedHashSet = new LinkedHashSet();

        linkedHashSet.add("ab");
        linkedHashSet.add("  ba  ");
        Assert.assertTrue("ab,ba".equals(StringUtils.setToStr(linkedHashSet, ",")));

        linkedHashSet.add("cb");
        Assert.assertTrue("ab&&ba&&cb".equals(StringUtils.setToStr(linkedHashSet, "&&")));

        Assert.assertTrue("abbacb".equals(StringUtils.setToStr(linkedHashSet, "")));
    }

    @Test
    public void testContains() {
        String a = "This is my test source string";
        Assert.assertTrue(StringUtils.contains(a, "my"));
        Assert.assertTrue(StringUtils.contains(a, ""));
        Assert.assertFalse(StringUtils.contains(a, null));
        Assert.assertFalse(StringUtils.contains(null, null));
        Assert.assertFalse(StringUtils.contains(null, "my"));
    }

    @Test
    public void testStrToList() {
        List<String> list = StringUtils.strToList("ab,ba,cb", ",");
        Assert.assertTrue(list.size() == 3);
        Assert.assertTrue(list.get(0).equals("ab"));
        Assert.assertTrue(list.get(1).equals("ba"));
        Assert.assertTrue(list.get(2).equals("cb"));
    }

    @Test
    public void testStrToSet() {
        Set<String> set = StringUtils.strToSet(null, "&&");
        Assert.assertTrue(set.isEmpty());

        set = StringUtils.strToSet("ab&&ba&&cb&&cb", "&&");
        Assert.assertTrue(set.size() == 3);

        Object[] array = set.toArray();
        Assert.assertTrue(array[0].equals("ab"));
        Assert.assertTrue(array[1].equals("ba"));
        Assert.assertTrue(array[2].equals("cb"));
    }

    @Test
    public void testStartWithToLowerCase() {
        Assert.assertFalse(StringUtils.startWithToLowerCase("ab", "abc"));
        Assert.assertTrue(StringUtils.startWithToLowerCase("AbC", "abc"));
        Assert.assertTrue(StringUtils.startWithToLowerCase("aB#", "ab#"));
        Assert.assertTrue(StringUtils.startWithToLowerCase("~Ab", "~ab"));
    }

    @Test
    public void testRemoveCR() {
        Assert.assertNull(StringUtils.removeCR(null));
        Assert.assertEquals("", StringUtils.removeCR(""));
        Assert.assertEquals("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\n",
            StringUtils.removeCR("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\r\n"));
    }

    @Test
    public void testRemoveSpcChar() {
        Assert.assertThrows(IllegalArgumentException.class, () -> {
            StringUtils.removeSpcChar("", null);
        });
        // 顺序无关
        Assert.assertEquals("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test", StringUtils.removeSpcChar("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\r\n\t", "\t\n\r"));
        Assert.assertEquals("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test", StringUtils.removeSpcChar("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\r\n", "\r\n"));
        Assert.assertEquals("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\r", StringUtils.removeSpcChar("com.alipay.sofa:biz-child1-child1:1.0.0:jar:test\r\n", "\n"));
    }
}
