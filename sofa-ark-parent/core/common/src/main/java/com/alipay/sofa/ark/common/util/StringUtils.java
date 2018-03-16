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

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class StringUtils {

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * @param str  the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
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

}
