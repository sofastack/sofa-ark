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

import java.util.Arrays;
import java.util.List;

/**
 * Static utility for making assertions.
 * 
 * @author Jeremy Wales
 */
public final class Assert {

    private Assert() {
    }

    /**
     * Asserts that the given parameter is not <code>null</code>.
     * 
     * @param parameter the parameter to check
     * @throws IllegalArgumentException if <code>parameter</code> is <code>null</code>
     * @return the given <code>parameter</code> (if an <code>Exception</code> was not already thrown because it was
     *         <code>null</code>)
     */
    public static Object isNotNull(Object parameter) {
        areNotNull(new Object[] { parameter });
        return parameter;
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     * 
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code> or <code>parameter2</code> is
     *             <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2) {
        areNotNull(new Object[] { parameter1, parameter2 });
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     * 
     * @param parameter1 the first parameter to check
     * @param parameter2 the second parameter to check
     * @param parameter3 the third parameter to check
     * @throws IllegalArgumentException if either <code>parameter1</code>, <code>parameter2</code> or
     *             <code>parameter3</code> is <code>null</code>
     */
    public static void areNotNull(Object parameter1, Object parameter2, Object parameter3) {
        areNotNull(new Object[] { parameter1, parameter2, parameter3 });
    }

    /**
     * Asserts that the given parameters are not <code>null</code>.
     * 
     * @param parameters the parameters to check
     * @throws IllegalArgumentException if any elements of <code>parameters</code> are <code>null</code>
     */
    public static void areNotNull(Object[] parameters) {
        if (parameters == null) {
            throw newNullParameterException(parameters);
        }
        List parameterList = Arrays.asList(parameters);
        if (parameterList.contains(null)) {
            throw newNullParameterException(parameterList);
        }
    }

    private static IllegalArgumentException newNullParameterException(Object parameters) {
        return new IllegalArgumentException("Expecting no null parameters but received "
                                            + parameters + ".");
    }

    /**
     * Asserts that the given arrays are of the same length.
     * 
     * @param array1 the first array in the comparison
     * @param array2 the second array in the comparison
     * @throws IllegalArgumentException if the length of <code>array1</code> is not equal to the length of
     *             <code>array2</code>
     */
    public static void areSameLength(Object[] array1, Object[] array2) {
        if (array1.length != array2.length) {
            throw new IllegalArgumentException("Expecting equal length arrays but received "
                                               + Arrays.asList(array1) + " and "
                                               + Arrays.asList(array2) + ".");
        }
    }
}