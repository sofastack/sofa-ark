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
package com.alipay.sofa.ark.transloader;

import com.alipay.sofa.ark.transloader.fixture.IndependentClassLoader;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

public abstract class BaseTestCase extends TestCase {

    protected void dump(Object object) {
        System.out.println("[" + getName() + "] " + object.toString());
    }

    protected void assertEqualExceptForClassLoader(String originalString, Object clone) {
        String originalClassLoaderString = this.getClass().getClassLoader().toString();
        String cloneClassLoaderString = IndependentClassLoader.getInstance().toString();
        assertNotEquals(originalClassLoaderString, cloneClassLoaderString);
        String expectedCloneString = StringUtils.replace(originalString, originalClassLoaderString,
            cloneClassLoaderString);
        String cloneString = clone.toString();
        dump(originalString);
        dump(cloneString);
        assertEquals(expectedCloneString, cloneString);
    }

    protected static void assertNotEquals(Object notExpected, Object actual) {
        if (notExpected == null)
            assertNotNull(actual);
        else
            assertFalse("Expected: not '" + notExpected + "'. Actual: '" + actual
                        + "' which is equal.", notExpected.equals(actual));
    }

    protected static final void assertMatches(Throwable expected, Throwable actual) {
        assertTrue(getComparisonFailedMessage("type", expected.getClass().getName(), actual),
            expected.getClass().isAssignableFrom(actual.getClass()));
        assertTrue(getComparisonFailedMessage("message", expected.getMessage(), actual),
            expected.getMessage() == null || actual.getMessage().startsWith(expected.getMessage()));
        Throwable expectedCause = ExceptionUtils.getCause(expected);
        if (expectedCause != null && expectedCause != expected) {
            assertMatches(expectedCause, ExceptionUtils.getCause(actual));
        }
    }

    private static String getComparisonFailedMessage(String attributeName, String expectedValue,
                                                     Throwable actual) {
        return "Expected " + attributeName + ": '" + expectedValue + "'. Actual: '"
               + ExceptionUtils.getFullStackTrace(actual) + "'.";
    }

    protected static final void assertThrows(Thrower thrower, Throwable expected) {
        try {
            thrower.executeUntilThrow();
        } catch (Throwable actual) {
            assertMatches(expected, actual);
            return;
        }
        fail("Expected: '" + expected + "'. Actual: nothing thrown.");
    }

    protected static interface Thrower {
        void executeUntilThrow() throws Throwable;
    }
}