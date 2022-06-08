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
import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.util.Assert;

import java.lang.reflect.Array;

/**
 * The type Inner array cloner.
 *
 * @author hanyue
 * @version : InnerArrayCloner.java, v 0.1 2022年06月04日 9:32 AM hanyue Exp $
 */
public class InnerArrayCloner implements InnerCloner {
    private final CloningStrategy parent;

    /**
     * Instantiates a new Inner array cloner.
     *
     * @param parent the parent
     */
    public InnerArrayCloner(CloningStrategy parent) {
        Assert.isNotNull(parent);
        this.parent = parent;
    }

    @Override
    public Object instantiateClone(Object originalArray, ClassLoader targetClassLoader)
            throws Exception {
        Class originalComponentType = originalArray.getClass().getComponentType();
        Class cloneComponentType = ClassWrapper.getClass(originalComponentType.getName(),
                targetClassLoader);
        return Array.newInstance(cloneComponentType, Array.getLength(originalArray));
    }

    @Override
    public void cloneContent(Object original, Object clone, ClassLoader targetClassLoader)
            throws Exception {
        for (int i = 0; i < Array.getLength(original); i++) {
            Object originalComponent = Array.get(original, i);
            Object cloneComponent = parent.cloneObjectUsingClassLoader(originalComponent,
                    targetClassLoader);
            Array.set(clone, i, cloneComponent);
        }
    }
}