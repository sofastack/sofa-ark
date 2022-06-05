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

import com.alipay.sofa.ark.transloader.util.Assert;

import java.lang.reflect.Field;

/**
 * Describes a field by its name, declaring class name and whether or not it is it of primitive type.
 *
 * @author hanyue
 * @version : FieldDescription.java, v 0.1 2022年06月04日 9:34 AM hanyue Exp $
 */
public class FieldDescription {
    private final String   declaringClassName;
    private final String   fieldName;
    private final Class<?> fieldType;
    private final boolean  primitive;

    /**
     * Constructs a <code>FieldDescription</code> with the given declaring <code>Class</code>, field name and
     * declared field type.
     *
     * @param declaringClass the <code>Class</code> that declares the field
     * @param field          the field
     */
    public FieldDescription(Class declaringClass, Field field) {
        Assert.areNotNull(declaringClass, field);
        this.declaringClassName = declaringClass.getName();
        this.fieldName = field.getName();
        this.fieldType = field.getType();
        this.primitive = this.fieldType.isPrimitive();
    }

    /**
     * Getter method for property <tt>declaringClassName</tt>.
     *
     * @return property value of declaringClassName
     */
    public String getDeclaringClassName() {
        return declaringClassName;
    }

    /**
     * Getter method for property <tt>fieldName</tt>.
     *
     * @return property value of fieldName
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Getter method for property <tt>primitive</tt>.
     *
     * @return property value of primitive
     */
    public boolean isPrimitive() {
        return primitive;
    }
}