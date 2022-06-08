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
import com.alipay.sofa.ark.transloader.ClassWrapper;
import com.alipay.sofa.ark.transloader.ObjectWrapper;
import com.alipay.sofa.ark.transloader.ProductionClassFinder;
import com.alipay.sofa.ark.transloader.Triangulate;
import com.alipay.sofa.ark.transloader.util.Assert;
import com.alipay.sofa.ark.transloader.util.ClassUtils;
import com.alipay.sofa.ark.transloader.util.ReflectionUtils;
import com.alipay.sofa.ark.transloader.util.SerializationUtils;
import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NullParameterRejectionTest extends BaseTestCase {
    private static final Class   PRODUCTION_ASSERT_CLASS = Assert.class;
    private static final String  PRODUCTION_PACKAGE_NAME = PRODUCTION_ASSERT_CLASS.getPackage()
            .getName();
    private static final String  TEST_PACKAGE_NAME       = "com.googlecode.transloader.test";
    private static final Class[] ALL_PRODUCTION_CLASSES  = ProductionClassFinder
            .getAllProductionClasses(
                    PRODUCTION_ASSERT_CLASS,
                    TEST_PACKAGE_NAME);

    private static class ExemptParam {
        String methodDescription;
        int    paramNumber;

        ExemptParam(String methodDescription, int paramNumber) {
            this.methodDescription = methodDescription;
            this.paramNumber = paramNumber;
        }

        public boolean equals(Object obj) {
            ExemptParam other = (ExemptParam) obj;
            return StringUtils.contains(methodDescription, other.methodDescription)
                    && paramNumber == other.paramNumber;
        }
    }

    private static class ExemptClass {
        Class<?> declaredClass;

        public ExemptClass(Class<?> declaredClass) {
            this.declaredClass = declaredClass;
        }

        @Override
        public boolean equals(Object obj) {
            ExemptClass other = (ExemptClass) obj;
            return Objects.equals(other.declaredClass.getName(), this.declaredClass.getName());
        }
    }

    private static final List EXEMPT_PARAMS = Arrays.asList(new ExemptParam[] {
            new ExemptParam("DefaultTransloader.wrap(java.lang.Class)", 0),
            new ExemptParam(
                    "InvocationMethodDescription(java.lang.reflect.Method,java.lang.Object[])", 1),
            new ExemptParam("FieldDescription(java.lang.Class,java.lang.reflect.Field)", 0),});

    private static final List EXEMPT_CLASS = Arrays.asList(new ExemptClass[] {
            new ExemptClass(ReflectionUtils.class), new ExemptClass(ObjectWrapper.class),
            new ExemptClass(ClassWrapper.class), new ExemptClass(ClassUtils.class),
            new ExemptClass(com.alipay.sofa.ark.transloader.util.StringUtils.class),
            new ExemptClass(SerializationUtils.class),});

    public static Test suite() throws Exception {
        return new ActiveTestSuite(NullParameterRejectionTest.class);
    }

    public void testAllPublicMethodsRejectNullParameters() throws Exception {
        for (int i = 0; i < ALL_PRODUCTION_CLASSES.length; i++) {
            Class productionClass = ALL_PRODUCTION_CLASSES[i];
            if (isPublicConcreteClassOtherThanAssert(productionClass)) {
                assertPublicMethodsRejectNullParameters(productionClass);
            }
        }
    }

    public void testAllPublicConstuctorsRejectNullParameters() throws Exception {
        for (int i = 0; i < ALL_PRODUCTION_CLASSES.length; i++) {
            Class productionClass = ALL_PRODUCTION_CLASSES[i];
            if (isPublicConcreteClassOtherThanAssert(productionClass)) {
                assertPublicConstuctorsRejectNullParameters(productionClass);
            }
        }
    }

    private boolean isPublicConcreteClassOtherThanAssert(Class productionClass) {
        return !productionClass.isInterface() && Modifier.isPublic(productionClass.getModifiers())
                && productionClass != PRODUCTION_ASSERT_CLASS
                && !EXEMPT_CLASS.contains(new ExemptClass(productionClass));
    }

    private void assertPublicMethodsRejectNullParameters(Class productionClass) throws Exception {
        Object instance = Triangulate.anyInstanceOf(productionClass);
        Method[] methods = productionClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getDeclaringClass().getPackage().getName()
                    .startsWith(PRODUCTION_PACKAGE_NAME)) {
                assertRejectsNullParameters(instance, methods[i]);
            }
        }
    }

    private void assertRejectsNullParameters(final Object instance, final Method method)
            throws Exception {
        Class[] parameterTypes = method.getParameterTypes();
        List nonNullParameters = getNonNullParameters(parameterTypes);
        for (int i = 0; i < parameterTypes.length; i++) {
            if (shouldTestNullRejectionForParameter(method, parameterTypes, i)) {
                List parameters = new ArrayList(nonNullParameters);
                parameters.set(i, null);
                assertExceptionThrownFromInvoking(instance, method, parameters);
            }
        }
    }

    private void assertExceptionThrownFromInvoking(final Object instance, final Method method,
                                                   final List parameters) {
        dump(method.toString() + parameters);
        Thrower thrower = new Thrower() {
            public void executeUntilThrow() throws Throwable {
                method.invoke(instance, parameters.toArray());
            }
        };
        assertThrows(thrower, new InvocationTargetException(new IllegalArgumentException(
                "Expecting no null parameters but received " + parameters + ".")));
    }

    private void assertPublicConstuctorsRejectNullParameters(Class productionClass) {
        Constructor[] constructors = productionClass.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            assertRejectsNullParameters(constructors[i]);
        }
    }

    private void assertRejectsNullParameters(Constructor constructor) {
        Class[] parameterTypes = constructor.getParameterTypes();
        List nonNullParameters = getNonNullParameters(parameterTypes);
        for (int i = 0; i < parameterTypes.length; i++) {
            if (shouldTestNullRejectionForParameter(constructor, parameterTypes, i)) {
                List parameters = new ArrayList(nonNullParameters);
                parameters.set(i, null);
                assertExceptionThrownFromInvoking(constructor, parameters);
            }
        }
    }

    private void assertExceptionThrownFromInvoking(final Constructor constructor,
                                                   final List parameters) {
        dump(constructor.toString());
        Thrower thrower = new Thrower() {
            public void executeUntilThrow() throws Throwable {
                constructor.newInstance(parameters.toArray());
            }
        };
        assertThrows(thrower, new InvocationTargetException(new IllegalArgumentException(
                "Expecting no null parameters but received ")));
    }

    private List getNonNullParameters(Class[] parameterTypes) {
        List nonNullParameters = new ArrayList();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            nonNullParameters.add(Triangulate.anyInstanceOf(parameterType));
        }
        return nonNullParameters;
    }

    private boolean shouldTestNullRejectionForParameter(Object methodOrConstructor,
                                                        Class[] parameterTypes, int i) {
        return !(parameterTypes[i].isPrimitive() || EXEMPT_PARAMS.contains(new ExemptParam(
                methodOrConstructor.toString(), i)));
    }
}
