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
import com.alipay.sofa.ark.transloader.util.ClassUtils;
import com.alipay.sofa.ark.transloader.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * The wrapper appropriate for wrapping around all <code>Class</code>es from potentially foreign
 * <code>ClassLoader</code>s.
 *
 * @author hanyue
 * @version : ClassWrapper.java, v 0.1 2022年06月03日 2:38 PM hanyue Exp $
 */
public class ClassWrapper implements InvokerMethod, InvokerField {

    private final Class           wrappedClass;
    private final CloningStrategy cloningStrategy;

    /**
     * Constructs a new <code>ClassWrapper</code> around the given <code>Class</code>. Note that using
     * implementation of {@link Transloader} is the recommended way to produce these.
     *
     * @param wrappedClass    the <code>Class</code> to wrap
     * @param cloningStrategy cloningStrategy
     */
    public ClassWrapper(Class wrappedClass, CloningStrategy cloningStrategy) {
        this.wrappedClass = wrappedClass;
        this.cloningStrategy = cloningStrategy;
    }

    /**
     * Indicates whether or not <code>null</code> is what is wrapped.
     *
     * @return true if the wrapped "Class" is actually <code>null</code>
     */
    public boolean isNull() {
        return wrappedClass == null;
    }

    /**
     * Provides direct access to the wrapped <code>Class</code>.
     *
     * @return the actual wrapped <code>Class</code> without any wrapping
     */
    public Class getWrappedSelf() {
        return wrappedClass;
    }

    /**
     * Indicates whether or not the wrapped <code>Class</code> is assignable to a <code>Class</code> with the given
     * name. It takes a parameter of type <code>String</code> instead of <code>Class</code> so that the test can be
     * performed for <code>Class</code>es that do not have an equivalent in the caller's <code>ClassLoader</code>.
     *
     * @param typeName the name of the type to check against
     * @return true if the wrapped <code>Class</code> is assignable to a <code>Class</code> with the given name
     */
    public boolean isAssignableTo(String typeName) {
        Assert.isNotNull(typeName);
        return classIsAssignableToType(wrappedClass, typeName);
    }

    /**
     * Provider direct access wrappedClass <code>ClassLoader</code>
     *
     * @return classloader of wrapperdClass
     */
    public ClassLoader getClassLoader() {
        return wrappedClass.getClassLoader();
    }

    /**
     * Loads the <code>Class</code> with the given name from the given <code>ClassLoader</code>.
     *
     * @param className   the name of the <code>Class</code>
     * @param classLoader the <code>ClassLoader</code> with which to load it
     * @return the <code>Class</code> with the given name loaded from the given <code>ClassLoader</code>
     * @throws TransloaderException if the <code>Class</code> cannot be found in the given <code>ClassLoader</code>
     */
    public static Class getClass(String className, ClassLoader classLoader) {
        Assert.areNotNull(className, classLoader);
        try {
            return ClassUtils.getClass(classLoader, className, false);
        } catch (ClassNotFoundException e) {
            throw new TransloaderException("Unable to load Class '" + className
                    + "' from ClassLoader '" + classLoader + "'.", e);
        }
    }

    /**
     * Loads the <code>Class[]</code> with the given parameterTypes from the given <code>ClassLoader</code>.
     *
     * @param parameterTypes the parameterTypes the <code>Class</code>
     * @param classLoader    the <code>ClassLoader</code> with which to load it
     * @return the <code>Class[]</code> with the given parameterTypes loaded from the given <code>ClassLoader</code>
     */
    public static Class[] getClasses(Class<?>[] parameterTypes, ClassLoader classLoader) {
        Assert.areNotNull(parameterTypes, classLoader);
        Class[] classes = new Class[parameterTypes.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = getClass(parameterTypes[i].getName(), classLoader);
        }
        return classes;
    }

    /**
     * Loads the <code>Class</code>es with the given names from the given <code>ClassLoader</code>.
     *
     * @param classNames  the names of the <code>Class</code>es
     * @param classLoader the <code>ClassLoader</code> with which to load them
     * @return the <code>Class</code>es with the given names loaded from the given <code>ClassLoader</code>
     * @throws TransloaderException if even one of the <code>Class</code>es cannot be found in the given
     * <code>ClassLoader</code>
     */
    public static Class[] getClasses(String[] classNames, ClassLoader classLoader) {
        Assert.areNotNull(classNames, classLoader);
        Class[] classes = new Class[classNames.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = getClass(classNames[i], classLoader);
        }
        return classes;
    }

    private static boolean classIsAssignableToType(Class rootClass, String typeName) {
        List allClasses = new ArrayList();
        allClasses.add(rootClass);
        allClasses.addAll(getAllSuperclasses(rootClass));
        allClasses.addAll(getAllInterfaces(rootClass));
        return convertClassesToClassNames(allClasses).contains(typeName);
    }

    public static List<Class<?>> getAllSuperclasses(final Class<?> cls) {
        if (cls == null) {
            return null;
        }
        final List<Class<?>> classes = new ArrayList<>();
        Class<?> superclass = cls.getSuperclass();
        while (superclass != null) {
            classes.add(superclass);
            superclass = superclass.getSuperclass();
        }
        return classes;
    }

    public static List<Class<?>> getAllInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final LinkedHashSet<Class<?>> interfacesFound = new LinkedHashSet<>();
        getAllInterfaces(cls, interfacesFound);

        return new ArrayList<>(interfacesFound);
    }

    public static List<String> convertClassesToClassNames(final List<Class<?>> classes) {
        if (classes == null) {
            return null;
        }
        final List<String> classNames = new ArrayList<>(classes.size());
        for (final Class<?> cls : classes) {
            if (cls == null) {
                classNames.add(null);
            } else {
                classNames.add(cls.getName());
            }
        }
        return classNames;
    }

    private static void getAllInterfaces(Class<?> cls, final HashSet<Class<?>> interfacesFound) {
        while (cls != null) {
            final Class<?>[] interfaces = cls.getInterfaces();

            for (final Class<?> i : interfaces) {
                if (interfacesFound.add(i)) {
                    getAllInterfaces(i, interfacesFound);
                }
            }

            cls = cls.getSuperclass();
        }
    }

    @Override
    public Object invoke(InvocationFieldDescription description) {
        Assert.isNotNull(description);
        try {
            Class wrappedClass = getWrappedSelf();
            Field declaredField = ReflectionUtils.findField(wrappedClass,
                    description.getFiledName());
            Assert.isNotNull(declaredField);
            ReflectionUtils.makeAccessible(declaredField);
            return ReflectionUtils.getField(declaredField, wrappedClass);
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
    public Object invoke(InvocationMethodDescription description) {
        Assert.isNotNull(description);
        try {
            Class wrappedClass = getWrappedSelf();
            ClassLoader wrappedClassLoader = wrappedClass.getClassLoader();
            Class[] parameterTypes = ClassWrapper.getClasses(description.getParameterTypeNames(),
                    wrappedClassLoader);
            Object[] parameters = (Object[]) this.cloningStrategy.cloneObjectUsingClassLoader(
                    description.getParameters(), wrappedClassLoader);
            Method method = ReflectionUtils.findMethod(wrappedClass, description.getMethodName(),
                    parameterTypes);
            Assert.isNotNull(method);
            ReflectionUtils.makeAccessible(method);
            return ReflectionUtils.invokeMethod(method, null, parameters);
        } catch (Exception e) {
            throw new TransloaderException("Unable to invoke '" + description.getMethodName()
                    + Arrays.asList(description.getParameterTypeNames())
                    + "' on '" + getWrappedSelf() + "'.", e);
        }
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
                        Object invoke = ClassWrapper.this.invoke(new InvocationMethodDescription(
                                method, parameters));
                        return Transloader.DEFAULT.wrap(invoke).cloneWith(getClass().getClassLoader());
                    }
                });
    }
}