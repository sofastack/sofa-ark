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

import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.invoke.InvocationFieldDescription;
import com.alipay.sofa.ark.transloader.invoke.InvocationMethodDescription;
import com.alipay.sofa.ark.transloader.invoke.InvokerField;
import com.alipay.sofa.ark.transloader.invoke.InvokerMethod;
import com.alipay.sofa.ark.transloader.util.Assert;
import com.alipay.sofa.ark.transloader.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * The wrapper appropriate for wrapping around all <code>Object</code>s referencing <code>Class</code>es from
 * potentially foreign <code>ClassLoader</code>s.
 *
 * @author hanyue
 * @version : ObjectWrapper.java, v 0.1 2022年06月03日 2:51 PM hanyue Exp $
 */
public class ObjectWrapper implements InvokerMethod, InvokerField {
    private final Object          wrappedObject;
    private final CloningStrategy cloningStrategy;

    /**
     * Constructs a new <code>ObjectWrapper</code> around the given object, which will use the given
     * <code>CloningStrategy</code> when required. Note that using implementation of {@link Transloader} is the
     * recommended way to produce these.
     *
     * @param objectToWrap    the object to wrap
     * @param cloningStrategy the strategy for cloning
     */
    public ObjectWrapper(Object objectToWrap, CloningStrategy cloningStrategy) {
        wrappedObject = objectToWrap;
        this.cloningStrategy = cloningStrategy;
    }

    /**
     * Indicates whether or not <code>null</code> is what is wrapped.
     *
     * @return true if the wrapped "object" is actually <code>null</code>
     */
    public boolean isNull() {
        return wrappedObject == null;
    }

    /**
     * Provides direct access to the wrapped object.
     *
     * @return the actual wrapped object without any wrapping
     */
    public Object getWrappedSelf() {
        return wrappedObject;
    }

    /**
     * Indicates whether or not the wrapped object is an instance of the type with the given name in the wrapped
     * object's <code>ClassLoader</code>(s). It takes a parameter of type <code>String</code> instead of
     * <code>Class</code> so that the test can be performed for <code>Class</code>es that do not have an equivalent
     * in the caller's <code>ClassLoader</code>.
     *
     * @param typeName the name of the type against which the wrapped object will be checked
     * @return true if the wrapped object is an instance of the type with the given name in the wrapped object's
     * <code>ClassLoader</code>(s)
     */
    public boolean isInstanceOf(String typeName) {
        Assert.isNotNull(typeName);
        return Transloader.DEFAULT.wrap(wrappedObject.getClass()).isAssignableTo(typeName);
    }

    /**
     * Gets an equivalent of the wrapped object with all <code>Class</code>es referenced being loaded from the given
     * <code>ClassLoader</code>. Every object referenced in the object graph starting with the object returned will
     * be able to be cast to its respective types in the given <code>ClassLoader</code>.
     * <p>
     * This implementation employs the <code>CloningStrategy</code> configured at construction. Note that using
     * {@link CloningStrategy#MINIMAL} (which is not the default strategy in {@link Transloader#DEFAULT}) will often
     * effect some changes within the object graph that starts with the wrapped object itself, as opposed to producing a
     * completely new, seperate graph. Using {@link CloningStrategy#MAXIMAL} instead prevents this, producing a purely
     * seperate clone without any changes within the wrapped object graph, at the cost of potentially far greater
     * cloning effort. An object graph altered by cloning with {@link CloningStrategy#MINIMAL} can of course be restored
     * entirely for use with other objects of <code>Class</code>es from its original <code>ClassLoader</code>(s)
     * by cloning it back with those original <code>ClassLoader</code>(s), but this is an extra coding step and
     * somewhat reduces the effort saved by not using {@link CloningStrategy#MAXIMAL} in the first place.
     * </p>
     *
     * @param classLoader the <code>ClassLoader</code> to use in creating an equivalent of the wrapped object
     * @return an equivalent of the wrapped object with all <code>Class</code>es referenced being loaded from the
     * given <code>ClassLoader</code>
     */
    public Object cloneWith(ClassLoader classLoader) {
        Assert.isNotNull(classLoader);
        if (isNull()) {
            return null;
        }
        try {
            return cloningStrategy.cloneObjectUsingClassLoader(getWrappedSelf(), classLoader);
        } catch (Exception e) {
            throw new TransloaderException("Unable to clone '" + getWrappedSelf() + "'.", e);
        }
    }

    @Override
    public Object invoke(InvocationMethodDescription description) {
        Assert.isNotNull(description);
        try {
            Class wrappedClass = getWrappedSelf().getClass();
            ClassLoader wrappedClassLoader = wrappedClass.getClassLoader();
            Class[] parameterTypes = ClassWrapper.getClasses(description.getParameterTypeNames(),
                    wrappedClassLoader);
            Object[] parameters = (Object[]) this.cloningStrategy.cloneObjectUsingClassLoader(
                    description.getParameters(), wrappedClassLoader);
            Method method = ReflectionUtils.findMethod(wrappedClass, description.getMethodName(),
                    parameterTypes);
            Assert.isNotNull(method);
            ReflectionUtils.makeAccessible(method);
            return ReflectionUtils.invokeMethod(method, getWrappedSelf(), parameters);
        } catch (Exception e) {
            throw new TransloaderException("Unable to invoke '" + description.getMethodName()
                    + Arrays.asList(description.getParameterTypeNames())
                    + "' on '" + getWrappedSelf() + "'.", e);
        }
    }

    @Override
    public Object invoke(InvocationFieldDescription description) {
        Assert.isNotNull(description);
        try {
            Class wrappedClass = getWrappedSelf().getClass();
            Field declaredField = ReflectionUtils.findField(wrappedClass,
                    description.getFiledName());
            Assert.isNotNull(declaredField);
            ReflectionUtils.makeAccessible(declaredField);
            return ReflectionUtils.getField(declaredField, getWrappedSelf());
        } catch (Exception e) {
            throw new TransloaderException("Unable to invoke '" + description.getFiledName()
                    + "' on '" + getWrappedSelf() + "'.", e);
        }
    }

    @Override
    public Object invokeCastable(InvocationFieldDescription fieldDescription) {
        Object invoke = invoke(fieldDescription);
        return Transloader.DEFAULT.wrap(invoke).cloneWith(getClass().getClassLoader());
    }

    @Override
    public Object invokeCastable(InvocationMethodDescription methodDescription) {
        Object invoke = invoke(methodDescription);
        return Transloader.DEFAULT.wrap(invoke).cloneWith(getClass().getClassLoader());
    }

    @Override
    public <T> T makeCastableTo(Class<T> targetInterface) {
        Assert.isNotNull(targetInterface);
        return (T) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[] {targetInterface}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] parameters)
                            throws Throwable {
                        Object invoke = ObjectWrapper.this.invoke(new InvocationMethodDescription(
                                method, parameters));
                        return Transloader.DEFAULT.wrap(invoke).cloneWith(getClass().getClassLoader());
                    }
                });
    }
}