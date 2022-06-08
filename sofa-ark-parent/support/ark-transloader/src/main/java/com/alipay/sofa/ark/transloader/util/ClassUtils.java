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

import java.util.HashMap;
import java.util.Map;

/**
 * @author hanyue
 * @version : ClassUtils.java, v 0.1 2022年06月05日 1:52 AM hanyue Exp $
 */
public class ClassUtils {

    private static Map abbreviationMap = new HashMap();

    static {
        abbreviationMap.put("int", "I");
        abbreviationMap.put("boolean", "Z");
        abbreviationMap.put("float", "F");
        abbreviationMap.put("long", "J");
        abbreviationMap.put("short", "S");
        abbreviationMap.put("byte", "B");
        abbreviationMap.put("double", "D");
        abbreviationMap.put("char", "C");
    }

    /**
     * Returns the class represented by <code>className</code> using the
     * <code>classLoader</code>.  This implementation supports names like
     * "<code>java.lang.String[]</code>" as well as "<code>[Ljava.lang.String;</code>".
     *
     * @param classLoader the class loader to use to load the class
     * @param className   the class name
     * @param initialize  whether the class must be initialized
     * @return the class represented by <code>className</code> using the <code>classLoader</code>
     * @throws ClassNotFoundException if the class is not found
     */
    public static Class getClass(ClassLoader classLoader, String className, boolean initialize)
            throws ClassNotFoundException {
        Assert.areNotNull(classLoader, classLoader);
        Class clazz;
        if (abbreviationMap.containsKey(className)) {
            String clsName = "[" + abbreviationMap.get(className);
            clazz = Class.forName(clsName, initialize, classLoader).getComponentType();
        } else {
            clazz = Class.forName(toProperClassName(className), initialize, classLoader);
        }
        return clazz;
    }

    /**
     * Converts a class name to a JLS style class name.
     * Params:
     * className – the class name
     * Returns:
     * the converted name
     *
     * @param className
     * @return
     */
    private static String toProperClassName(String className) {
        className = StringUtils.deleteWhitespace(className);
        if (className == null) {
            throw new IllegalArgumentException("className is null");
        } else if (className.endsWith("[]")) {
            StringBuffer classNameBuffer = new StringBuffer();
            while (className.endsWith("[]")) {
                className = className.substring(0, className.length() - 2);
                classNameBuffer.append("[");
            }
            String abbreviation = (String) abbreviationMap.get(className);
            if (abbreviation != null) {
                classNameBuffer.append(abbreviation);
            } else {
                classNameBuffer.append("L").append(className).append(";");
            }
            className = classNameBuffer.toString();
        }
        return className;
    }
}