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

import net.sf.cglib.proxy.Enhancer;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.objenesis.ObjenesisHelper;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Random;

/**
 * @author hanyue
 */
public final class Triangulate {
    private static final Random   RANDOM          = new Random(System.currentTimeMillis());
    private static final byte[]   ONE_BYTE_BUFFER = new byte[1];
    private static final Method[] MY_METHODS      = Triangulate.class.getDeclaredMethods();

    private Triangulate() {
    }

    public static String anyString() {
        return anyDouble() + "";
    }

    public static String anyAlphaNumbericString() {
        return Long.toHexString(anyLong());
    }

    public static boolean eitherBoolean() {
        return RANDOM.nextBoolean();
    }

    public static byte anyByte() {
        RANDOM.nextBytes(ONE_BYTE_BUFFER);
        return ONE_BYTE_BUFFER[0];
    }

    public static char anyChar() {
        return (char) anyByte();
    }

    public static short anyShort() {
        return (short) anyByte();
    }

    public static int anyInt() {
        return RANDOM.nextInt();
    }

    public static int anyIntFromZeroTo(int upperBound) {
        return RANDOM.nextInt(upperBound);
    }

    public static Integer anyInteger() {
        return new Integer(anyInt());
    }

    public static long anyLong() {
        return RANDOM.nextLong();
    }

    public static float anyFloat() {
        return RANDOM.nextFloat();
    }

    public static double anyDouble() {
        return RANDOM.nextDouble();
    }

    public static Class anyClass() {
        return anyMethod().getReturnType();
    }

    public static Method anyMethod() {
        return MY_METHODS[anyIntFromZeroTo(MY_METHODS.length - 1)];
    }

    public static Object anyInstanceOf(Class type) {
        try {
            if (type == null || type == void.class)
                return null;
            if (type.isArray())
                return anyArrayOf(type.getComponentType());
            Object triangulatedInstance = tryToTriangulateFromThisClass(type);
            if (triangulatedInstance != null)
                return triangulatedInstance;
            if (type.isInterface())
                return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[] { type }, new TriangulatingInvocationHandler());
            if (Modifier.isAbstract(type.getModifiers()))
                return Enhancer.create(type, new TriangulatingInvocationHandler());
            return ObjenesisHelper.newInstance(type);
        } catch (Exception e) {
            throw new NestableRuntimeException(e);
        }
    }

    private static Object anyArrayOf(Class componentType) throws Exception {
        int length = anyIntFromZeroTo(3);
        Object array = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            Array.set(array, i, anyInstanceOf(componentType));
        }
        return array;
    }

    private static Object tryToTriangulateFromThisClass(Class type) throws Exception {
        for (int i = 0; i < MY_METHODS.length; i++) {
            Method method = MY_METHODS[i];
            Class returnType = method.getReturnType();
            boolean hasNoParameters = method.getParameterTypes() == null
                                      || method.getParameterTypes().length == 0;
            if (returnType == type && hasNoParameters) {
                return method.invoke(null, new Object[0]);
            }
        }
        return null;
    }

    private static class TriangulatingInvocationHandler implements InvocationHandler,
                                                       net.sf.cglib.proxy.InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getReturnType() == Void.class)
                return null;
            if (method.getName().equals("equals") && method.getParameterTypes().length == 1
                && method.getParameterTypes()[0] == Object.class)
                return new Boolean(proxy == args[0]);
            if (method.getName().equals("hashCode") && method.getParameterTypes().length == 0)
                return new Integer(System.identityHashCode(proxy));
            if (method.getName().equals("toString") && method.getParameterTypes().length == 0)
                return TriangulatingInvocationHandler.class.getName() + '#'
                       + System.identityHashCode(proxy);
            return anyInstanceOf(method.getReturnType());
        }
    }
}
