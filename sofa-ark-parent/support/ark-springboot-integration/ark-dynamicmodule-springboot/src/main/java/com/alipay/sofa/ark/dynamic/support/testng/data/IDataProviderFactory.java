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
package com.alipay.sofa.ark.dynamic.support.testng.data;

import com.alipay.sofa.ark.transloader.Transloader;
import com.alipay.sofa.ark.transloader.invoke.InvocationMethodDescription;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The type Data provider factory.
 *
 * @author hanyue
 * @version : IDataProviderUtils.java, v 0.1 2022年06月02日 9:49 AM hanyue Exp $
 */
public class IDataProviderFactory {

    /**
     * Create object.
     *
     * @param source the source
     * @return the object
     */
    public static Object create(Object source) {
        return create(source, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Create object.
     *
     * @param source      the source
     * @param classLoader the class loader
     * @return the object
     */
    public static Object create(Object source, ClassLoader classLoader) {
        Object target = Transloader.DEFAULT.wrap(source).cloneWith(classLoader);
        return Transloader.DEFAULT.wrap(target).invoke(new InvocationMethodDescription("instance"));
    }
}