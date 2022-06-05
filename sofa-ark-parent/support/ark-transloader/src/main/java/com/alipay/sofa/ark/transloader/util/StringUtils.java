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
package com.alipay.sofa.ark.transloader.util;

/**
 * refs: org.apache.commons.lang.StringUtils
 *
 * @author hanyue
 * @version : StringUtils.java, v 0.1 2022年06月05日 1:57 AM hanyue Exp $
 */
public class StringUtils {

    /**
     * <p>Deletes all 'space' characters from a String as defined by
     * {@link Character#isSpace(char)}.</p>
     *
     * <p>This is the only StringUtils method that uses the
     * <code>isSpace</code> definition. You are advised to use
     * {@link #deleteWhitespace(String)} instead as whitespace is much
     * better localized.</p>
     *
     * <pre>
     * StringUtils.deleteSpaces(null)           = null
     * StringUtils.deleteSpaces("")             = ""
     * StringUtils.deleteSpaces("abc")          = "abc"
     * StringUtils.deleteSpaces(" \t  abc \n ") = "abc"
     * StringUtils.deleteSpaces("ab  c")        = "abc"
     * StringUtils.deleteSpaces("a\nb\tc     ") = "abc"
     * </pre>
     *
     * <p>Spaces are defined as <code>{' ', '\t', '\r', '\n', '\b'}</code>
     * in line with the deprecated <code>isSpace</code> method.</p>
     *
     * @param str the String to delete spaces from, may be null
     * @return the String without 'spaces', <code>null</code> if null String input
     * @deprecated Use the better localized {@link #deleteWhitespace(String)}.
     * Method will be removed in Commons Lang 3.0.
     */
    public static String deleteWhitespace(String str) {
        if (isEmpty(str)) {
            return str;
        }
        int sz = str.length();
        char[] chs = new char[sz];
        int count = 0;
        for (int i = 0; i < sz; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                chs[count++] = str.charAt(i);
            }
        }
        if (count == sz) {
            return str;
        }
        return new String(chs, 0, count);
    }

    /**
     * Checks if a String is empty ("") or null.
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     *
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String. That functionality is available in isBlank().
     * Params:
     * str – the String to check, may be null
     * Returns:
     * true if the String is empty or null
     *
     * @param str str type is String
     * @return true or false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}