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
package com.alipay.sofa.ark.transloader.clone.reflect.instance;

import com.alipay.sofa.ark.transloader.clone.reflect.ReflectionCloningStrategy;

/**
 * A strategy interface for customising how normal objects (as opposed to arrays) are instantiated by
 * {@link ReflectionCloningStrategy}.
 *
 * @author hanyue
 * @version : InstantiationStrategy.java, v 0.1 2022年06月04日 9:25 AM hanyue Exp $
 */
public interface InstantiationStrategy {

    /**
     * Creates a new instance of the given type.
     *
     * @param type the type of which to make a new instance
     * @return the new instance of the given <code>type</code>
     * @throws Exception can throw any <code>Exception</code> depending on the implementation
     */
    Object newInstance(Class type) throws Exception;
}