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

/**
 * When injected into a {@link ReflectionCloningStrategy}, decides that all given objects and those that they reference
 * should be cloned.
 *
 * @author hanyue
 * @version : MaximalCloningDecisionStrategy.java, v 0.1 2022年06月04日 8:48 AM hanyue Exp $
 */
public final class MaximalCloningDecisionStrategy implements CloningDecisionStrategy {

    /**
     * Decides that all objects should be shallow copied.
     *
     * @param original ignored; returns <code>true</code> regardless
     * @param targetClassLoader ignored; returns <code>true</code> regardless
     * @return <code>true</code> always
     */
    @Override
    public boolean shouldCloneObjectItself(Object original, ClassLoader targetClassLoader)
                                                                                          throws ClassNotFoundException {
        Assert.areNotNull(original, targetClassLoader);
        return true;
    }

    /**
     * Decides that all objects have their references copied.
     *
     * @param original ignored; returns <code>true</code> regardless
     * @param targetClassLoader ignored; returns <code>true</code> regardless
     * @return <code>true</code> always
     */
    @Override
    public boolean shouldCloneObjectContent(Object original, ClassLoader targetClassLoader)
                                                                                           throws ClassNotFoundException {
        Assert.areNotNull(original, targetClassLoader);
        return true;
    }
}