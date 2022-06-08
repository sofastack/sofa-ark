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
package com.alipay.sofa.ark.transloader.invoke;

import com.alipay.sofa.ark.transloader.util.Assert;

import java.lang.reflect.Method;

/**
 * Describes a method invocation by method name, parameter type names and parameters.
 *
 * @author hanyue
 * @version : InvocationMethodDescription.java, v 0.1 2022年06月03日 2:55 PM hanyue Exp $
 */
public class InvocationMethodDescription {
    private static final String[] NO_PARAMS = new String[] {};

    private final String   methodName;
    private final String[] parameterTypeNames;
    private final Object[] parameters;

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name.
     *
     * @param methodName the name of a zero-parameter method
     */
    public InvocationMethodDescription(String methodName) {
        this(methodName, NO_PARAMS, NO_PARAMS);
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name and parameter.
     *
     * @param methodName the name of a single-parameter method
     * @param parameter  a parameter whose concrete implementation <code>Class</code> has the same name as its type
     *                   declared in the targeted method (therefore cannot be <code>null</code>)
     */
    public InvocationMethodDescription(String methodName, Object parameter) {
        this(methodName, new Object[] {parameter});
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name and parameters.
     *
     * @param methodName the name of a multi-parameter method
     * @param parameters some parameters whose concrete implementation <code>Class</code>es have the same names as
     *                   their types declared in the targeted method (therefore cannot contain <code>null</code>s)
     */
    public InvocationMethodDescription(String methodName, Object[] parameters) {
        this(methodName, getClasses(parameters), parameters);
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name, parameter type and parameter.
     *
     * @param methodName    the name of a single-parameter method
     * @param parameterType a <code>Class</code> whose name is the same as the parameter type declared in the method
     * @param parameter     the parameter to the method invocation (can be <code>null</code>)
     */
    public InvocationMethodDescription(String methodName, Class parameterType, Object parameter) {
        this(methodName, new Class[] {parameterType}, new Object[] {parameter});
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name, parameter type name and parameter.
     *
     * @param methodName     the name of a single-parameter method
     * @param parameterTypes the name of the parameter type declared in the method
     * @param parameters     the parameter to the method invocation (can be <code>null</code>)
     */
    public InvocationMethodDescription(String methodName, Class[] parameterTypes,
                                       Object[] parameters) {
        this(methodName, getNames(parameterTypes), parameters);
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name, parameter types and parameters.
     *
     * @param methodName        the name of a multi-parameter method
     * @param parameterTypeName some <code>Class</code>es whose names are the same as the parameter types declared in
     *                          the targeted method
     * @param parameter         the parameters to the method invocation (cannot be but can <i>contain</i> <code>null</code>)
     */
    public InvocationMethodDescription(String methodName, String parameterTypeName, Object parameter) {
        this(methodName, new String[] {parameterTypeName}, new Object[] {parameter});
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given method name, parameter type name and parameter.
     *
     * @param methodName         the name of a single-parameter method
     * @param parameterTypeNames the names of the parameter types declared in the method (cannot be or contain
     *                           <code>null</code>)
     * @param parameters         the parameters to the method invocation (cannot be but can <i>contain</i> <code>null</code>)
     */
    public InvocationMethodDescription(String methodName, String[] parameterTypeNames,
                                       Object[] parameters) {
        Assert.isNotNull(methodName);
        Assert.areNotNull(parameters);
        Assert.areNotNull(parameterTypeNames);
        Assert.areSameLength(parameterTypeNames, parameters);

        this.methodName = methodName;
        this.parameterTypeNames = parameterTypeNames;
        this.parameters = parameters;
    }

    /**
     * Constructs an <code>InvocationDescription</code> with the given {@link Method} and parameters.
     *
     * @param method     the <code>Method</code> to invoke
     * @param parameters the parameters to the method invocation (can actually be <code>null</code>)
     */
    public InvocationMethodDescription(Method method, Object[] parameters) {
        this(((Method) Assert.isNotNull(method)).getName(), method.getParameterTypes(),
                parameters == null ? NO_PARAMS : parameters);
    }

    private static Class[] getClasses(Object[] objects) {
        Assert.areNotNull(objects);
        Class[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; i++) {
            classes[i] = objects[i].getClass();
        }
        return classes;
    }

    private static String[] getNames(Class[] classes) {
        Assert.areNotNull(classes);
        String[] names = new String[classes.length];
        for (int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getName();
        }
        return names;
    }

    /**
     * Getter method for property <tt>methodName</tt>.
     *
     * @return property value of methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Getter method for property <tt>parameterTypeNames</tt>.
     *
     * @return property value of parameterTypeNames
     */
    public String[] getParameterTypeNames() {
        return parameterTypeNames;
    }

    /**
     * Getter method for property <tt>parameters</tt>.
     *
     * @return property value of parameters
     */
    public Object[] getParameters() {
        return parameters;
    }
}