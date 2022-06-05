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
import com.alipay.sofa.ark.transloader.clone.reflect.instance.InstantiationStrategy;
import com.alipay.sofa.ark.transloader.util.Assert;

/**
 * The type Inner normal object cloner.
 *
 * @author hanyue
 * @version : InnerNormalObjectCloner.java, v 0.1 2022年06月04日 9:31 AM hanyue Exp $
 */
public class InnerNormalObjectCloner implements InnerCloner {

    private final CloningStrategy       parent;
    private final InstantiationStrategy instantiator;

    /**
     * Instantiates a new Inner normal object cloner.
     *
     * @param parent       the parent
     * @param instantiator the instantiator
     */
    public InnerNormalObjectCloner(CloningStrategy parent, InstantiationStrategy instantiator) {
        Assert.areNotNull(parent, instantiator);
        this.instantiator = instantiator;
        this.parent = parent;
    }

    @Override
    public Object instantiateClone(Object original, ClassLoader targetClassLoader) throws Exception {
        Class cloneClass = ClassWrapper.getClass(original.getClass().getName(), targetClassLoader);
        return instantiator.newInstance(cloneClass);
    }

    @Override
    public void cloneContent(Object original, Object clone, ClassLoader targetClassLoader)
                                                                                          throws Exception {
        FieldReflector originalReflector = new FieldReflector(original);
        FieldReflector cloneReflector = new FieldReflector(clone, targetClassLoader);
        FieldDescription[] fieldDescriptions = originalReflector.getAllInstanceFieldDescriptions();
        for (int i = 0; i < fieldDescriptions.length; i++) {
            cloneField(fieldDescriptions[i], originalReflector, cloneReflector, targetClassLoader);
        }
    }

    private void cloneField(FieldDescription description, FieldReflector originalReflector,
                            FieldReflector cloneReflector, ClassLoader targetClassLoader)
                                                                                         throws Exception {
        Object originalFieldValue = originalReflector.getValue(description);
        Object cloneFieldValue = originalFieldValue;
        if (!description.isPrimitive()) {
            cloneFieldValue = parent.cloneObjectUsingClassLoader(originalFieldValue,
                targetClassLoader);
        }
        cloneReflector.setValue(description, cloneFieldValue);
    }
}