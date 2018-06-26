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
package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.exception.ArkException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class ReflectionUtils {

    /**
     * Callback interface invoked on each field in the hierarchy.
     */
    public interface FieldCallback {

        /**
         * Perform an operation using the given field.
         * @param field the field to operate on
         * @throws ArkException throw exception when handle with field
         */
        void doWith(Field field) throws ArkException;
    }

    public static void doWithFields(Class<?> clazz, FieldCallback fc) {
        AssertUtils.assertNotNull(clazz, "Class should not be null");
        Class<?> searchType = clazz;
        do {
            Field[] fields = searchType.getDeclaredFields();
            for (Field field : fields) {
                fc.doWith(field);
            }
            searchType = searchType.getSuperclass();
        } while (searchType != null && searchType != Object.class);
    }

    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers())
             || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier
            .isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }
}