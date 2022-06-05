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
package com.alipay.sofa.ark.transloader.function;

import com.alipay.sofa.ark.transloader.BaseTestCase;
import com.alipay.sofa.ark.transloader.ObjectWrapper;
import com.alipay.sofa.ark.transloader.Transloader;
import com.alipay.sofa.ark.transloader.TransloaderException;
import com.alipay.sofa.ark.transloader.Triangulate;
import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.fixture.IndependentClassLoader;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaObject;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaType;
import com.alipay.sofa.ark.transloader.fixture.NonCommonJavaTypeWithMethods;
import com.alipay.sofa.ark.transloader.fixture.WithMapFields;
import com.alipay.sofa.ark.transloader.fixture.WithMethods;
import com.alipay.sofa.ark.transloader.fixture.WithPrimitiveFields;
import com.alipay.sofa.ark.transloader.fixture.WithStringField;
import com.alipay.sofa.ark.transloader.invoke.InvocationMethodDescription;
import junit.extensions.ActiveTestSuite;
import junit.framework.Test;

public class GeneralTransloaderTest extends BaseTestCase {
    private Object      foreignObject;
    private Object      foreignObjectWithMethods;
    private Transloader transloader      = Transloader.DEFAULT;
    private ClassLoader dummyClassLoader = (ClassLoader) Triangulate
                                             .anyInstanceOf(ClassLoader.class);

    public static Test suite() throws Exception {
        return new ActiveTestSuite(GeneralTransloaderTest.class);
    }

    protected void setUp() throws Exception {
        foreignObject = getNewInstanceFromOtherClassLoader(WithMapFields.class);
        foreignObjectWithMethods = getNewInstanceFromOtherClassLoader(WithMethods.class);
    }

    private static Object getNewInstanceFromOtherClassLoader(Class clazz) throws Exception {
        return IndependentClassLoader.getInstance().loadClass(clazz.getName()).newInstance();
    }

    public void testReportsIsNullWhenGivenNull() throws Exception {
        assertTrue(transloader.wrap(null).isNull());
    }

    public void testReportsIsNotNullWhenGivenNonNullObject() throws Exception {
        assertFalse(transloader.wrap(new Object()).isNull());
    }

    public void testReportsIsNotInstanceOfUnrelatedType() throws Exception {
        assertFalse(transloader.wrap(new Object()).isInstanceOf(NonCommonJavaType.class.getName()));
    }

    public void testReportsIsInstanceOfSameClass() throws Exception {
        assertTrue(transloader.wrap(foreignObject).isInstanceOf(foreignObject.getClass().getName()));
    }

    public void testReportsIsInstanceOfSuperClass() throws Exception {
        assertTrue(transloader.wrap(foreignObject)
            .isInstanceOf(NonCommonJavaObject.class.getName()));
    }

    public void testReportsIsInstanceOfImplementedInterface() throws Exception {
        assertTrue(transloader.wrap(foreignObject).isInstanceOf(NonCommonJavaType.class.getName()));
    }

    public void testReturnsNullWhenAskedToCloneNull() throws Exception {
        assertNull(transloader.wrap((Object) null).cloneWith(dummyClassLoader));
    }

    public void testReturnsCloneReturnedFromGivenCloningStrategy() throws Exception {
        final Object expectedOriginal = new Object();
        final ClassLoader expectedClassloader = new ClassLoader() {
        };
        final Object expectedClone = new Object();
        CloningStrategy cloningStrategy = new CloningStrategy() {
            public Object cloneObjectUsingClassLoader(Object original, ClassLoader cloneClassLoader)
                                                                                                    throws Exception {
                assertSame(expectedOriginal, original);
                assertSame(expectedClassloader, cloneClassLoader);
                return expectedClone;
            }
        };
        assertSame(expectedClone,
            new ObjectWrapper(expectedOriginal, cloningStrategy).cloneWith(expectedClassloader));
    }

    public void testWrapsExceptionThrownByGivenCloningStrategy() throws Exception {
        final Object expectedOriginal = new Object();
        final Exception expectedException = new Exception(Triangulate.anyString());
        final CloningStrategy throwingCloningStrategy = new CloningStrategy() {
            public Object cloneObjectUsingClassLoader(Object original, ClassLoader cloneClassLoader)
                                                                                                    throws Exception {
                throw expectedException;
            }
        };
        Thrower thrower = new Thrower() {
            public void executeUntilThrow() throws Throwable {
                new ObjectWrapper(expectedOriginal, throwingCloningStrategy)
                    .cloneWith(dummyClassLoader);
            }
        };
        assertThrows(thrower, new TransloaderException("Unable to clone '" + expectedOriginal
                                                       + "'.", expectedException));
    }

    public void testProvidesWrappedObjectOnRequest() throws Exception {
        final Object expected = new Object();
        assertSame(expected, transloader.wrap(expected).getWrappedSelf());
    }

    public void testPassesAndReturnsStringsToAndFromInvocations() throws Exception {
        ObjectWrapper objectWrapper = transloader.wrap(foreignObjectWithMethods);
        String expectedStringFieldValue = Triangulate.anyString();
        objectWrapper.invoke(new InvocationMethodDescription("setStringField",
            expectedStringFieldValue));
        assertEquals(expectedStringFieldValue,
            objectWrapper.invoke(new InvocationMethodDescription("getStringField")));
    }

    public void testClonesParametersOfNonCommonJavaTypesInInvocations() throws Exception {
        NonCommonJavaType first = new WithStringField(Triangulate.anyString());
        NonCommonJavaType second = new WithPrimitiveFields();
        String expected = new WithMethods().concatenate(first, second);
        Class[] paramTypes = { NonCommonJavaType.class, NonCommonJavaType.class };
        Object[] params = { first, second };
        String actual = (String) transloader.wrap(foreignObjectWithMethods).invoke(
            new InvocationMethodDescription("concatenate", paramTypes, params));
        assertEqualExceptForClassLoader(expected, actual);
    }

    public void testCreatesAnImplementationOfAGivenInterfaceThatCallsThroughToTheWrappedObject()
                                                                                                throws Exception {
        String expectedStringFieldValue = Triangulate.anyString();
        Transloader.DEFAULT.wrap(foreignObjectWithMethods).invoke(
            new InvocationMethodDescription("setStringField", expectedStringFieldValue));
        NonCommonJavaTypeWithMethods withMethods = (NonCommonJavaTypeWithMethods) transloader.wrap(
            foreignObjectWithMethods).makeCastableTo(NonCommonJavaTypeWithMethods.class);
        assertEquals(expectedStringFieldValue, withMethods.getStringField());
    }
}
