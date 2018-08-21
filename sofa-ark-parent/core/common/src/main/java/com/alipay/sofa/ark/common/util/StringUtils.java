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

import java.util.*;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class StringUtils {

    public static final String EMPTY_STRING = "";
    public static final int    CHAR_A       = 'A';
    public static final int    CHAR_Z       = 'Z';
    public static final int    CASE_GAP     = 32;

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    /**
     * <p>Checks whether two String are equal.</p>
     * @param a comparator string a
     * @param b comparator string b
     * @return whether two string equals
     */
    public static boolean isSameStr(String a, String b) {
        if (a == null && b != null) {
            return false;
        }

        if (a == null && b == null) {
            return true;
        }

        return a.equals(b);
    }

    /**
     * <p>Transform a string list to a long string separated by delimiter</p>
     * @param stringSet
     * @param delimiter
     * @return
     */
    public static String listToStr(Set<String> stringSet, String delimiter) {
        if (stringSet == null || stringSet.isEmpty()) {
            return EMPTY_STRING;
        }

        AssertUtils.assertNotNull(delimiter, "Delimiter should not be null.");

        StringBuilder sb = new StringBuilder();
        for (String str : stringSet) {
            sb.append(str.trim()).append(delimiter);
        }

        return sb.toString().substring(0, sb.length() - delimiter.length());
    }

    public static Set<String> strToSet(String str, String delimiter) {
        return new LinkedHashSet<>(strToList(str, delimiter));
    }

    public static List<String> strToList(String str, String delimiter) {
        if (str == null || str.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> stringList = new ArrayList<>();
        for (String s : str.split(delimiter)) {
            stringList.add(s.trim());
        }
        return stringList;
    }

    /**
     * Check whether start with anotherString when transformed to lower case.
     *
     * @param thisString
     * @param anotherString
     * @return
     */
    public static boolean startWithToLowerCase(String thisString, String anotherString) {
        AssertUtils.assertNotNull(thisString, "Param must not be null!");
        AssertUtils.assertNotNull(anotherString, "Param must not be null!");

        if (thisString.length() < anotherString.length()) {
            return false;
        }

        boolean ret;
        int index = 0;
        do {
            if (thisString.charAt(index) > CHAR_Z || thisString.charAt(index) < CHAR_A) {
                ret = thisString.charAt(index) == anotherString.charAt(index);
            } else {
                ret = thisString.charAt(index) + CASE_GAP == anotherString.charAt(index);
            }
        } while (ret && ++index < anotherString.length());
        return ret;
    }
}
