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
package com.alipay.sofa.ark.transloader.clone.reflect;

import com.alipay.sofa.ark.transloader.ClassWrapper;
import com.alipay.sofa.ark.transloader.util.Assert;
import com.alipay.sofa.ark.transloader.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A reflective wrapper around any object, exposing its fields.
 *
 * @author hanyue
 * @version : FieldReflector.java, v 0.1 2022年06月04日 9:36 AM hanyue Exp $
 */
public class FieldReflector {

    /**
     * The list of primitive wrapper <code>Class</code>es.
     */
    public static final List PRIMITIVE_WRAPPERS = Arrays.asList(new Class[] {Boolean.class,
            Byte.class, Character.class, Short.class, Integer.class, Long.class, Float.class,
            Double.class});

    private final Object      wrappedObject;
    private final ClassLoader classLoader;

    /**
     * Wraps the given object in a new <code>FieldReflector</code>. Retrieves the <code>ClassLoader</code> of the
     * wrapped object's <code>Class</code> as the one to use for all class loading.
     *
     * @param objectToWrap the object whose fields you want to expose.
     */
    public FieldReflector(Object objectToWrap) {
        this(objectToWrap, getClassLoader(objectToWrap));
    }

    /**
     * Wraps the given object in a new <code>FieldReflector</code>.
     *
     * @param objectToWrap     the object whose fields you want to expose.
     * @param classLoaderToUse the <code>ClassLoader</code> to use for loading <code>Class</code>es from
     *                         {@link FieldDescription#getDeclaringClassName()}
     */
    public FieldReflector(Object objectToWrap, ClassLoader classLoaderToUse) {
        Assert.areNotNull(objectToWrap, classLoaderToUse);
        wrappedObject = objectToWrap;
        classLoader = classLoaderToUse;
    }

    /**
     * Gets all the instance fields in the entire class hierarchy extended by the wrapped object.
     *
     * @return a description of each instance field in the class hierarchy extended by the wrapped object (<code>final</code>
     * fields are not excluded, even though {@link #setValue(FieldDescription, Object)} can be attempted with
     * such descriptions)
     */
    public FieldDescription[] getAllInstanceFieldDescriptions() {
        return getAllInstanceFieldDescriptions(wrappedObject.getClass());
    }

    private static FieldDescription[] getAllInstanceFieldDescriptions(Class currentClass) {
        List descriptions = new ArrayList();
        while (currentClass != null) {
            descriptions.addAll(getInstanceFieldDescriptions(currentClass));
            currentClass = currentClass.getSuperclass();
        }
        return (FieldDescription[]) descriptions.toArray(new FieldDescription[descriptions.size()]);
    }

    private static List getInstanceFieldDescriptions(Class currentClass) {
        Field[] fields = currentClass.getDeclaredFields();
        List descriptions = new ArrayList(fields.length);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            if (!Modifier.isStatic(field.getModifiers())) {
                descriptions.add(new FieldDescription(currentClass, field));
            }
        }
        return descriptions;
    }

    /**
     * Gets the value of the field matching the given description on the wrapped object.
     *
     * @param description the description of the field
     * @return the value of the field from the wrapped object
     * @throws NoSuchFieldException if the field named by {@link FieldDescription#getFieldName()} does not exist on the
     * <code>Class</code> named by {@link FieldDescription#getDeclaringClassName()} or the
     * <code>Class</code> named by {@link FieldDescription#getDeclaringClassName()} is not in the wrapped
     * object's class hierarchy
     */
    public Object getValue(FieldDescription description) throws NoSuchFieldException {
        Assert.isNotNull(description);
        return ReflectionUtils.getField(getFieldHavingMadeItAccessible(description, classLoader),
                wrappedObject);
    }

    /**
     * Sets the value of the field matching the given description on the wrapped object.
     *
     * @param description the description of the field to set
     * @param fieldValue  the value to set
     * @throws NoSuchFieldException if the field named by {@link FieldDescription#getFieldName()} does not exist on the
     * <code>Class</code> named by {@link FieldDescription#getDeclaringClassName()} or the
     * <code>Class</code> named by {@link FieldDescription#getDeclaringClassName()} is not in the wrapped
     * object's class hierarchy
     */
    public void setValue(FieldDescription description, Object fieldValue)
            throws NoSuchFieldException {
        Assert.isNotNull(description);
        ReflectionUtils.setField(getFieldHavingMadeItAccessible(description, classLoader),
                wrappedObject, fieldValue);
    }

    private static Field getFieldHavingMadeItAccessible(FieldDescription description,
                                                        ClassLoader classLoader)
            throws NoSuchFieldException {
        Class declaringClass = ClassWrapper.getClass(description.getDeclaringClassName(),
                classLoader);
        Field field = declaringClass.getDeclaredField(description.getFieldName());
        field.setAccessible(true);
        return field;
    }

    private static ClassLoader getClassLoader(Object object) {
        Assert.isNotNull(object);
        ClassLoader classLoader = object.getClass().getClassLoader();
        return classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;
    }
}