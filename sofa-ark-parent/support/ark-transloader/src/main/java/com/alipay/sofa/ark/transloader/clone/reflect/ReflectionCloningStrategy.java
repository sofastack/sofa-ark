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

import com.alipay.sofa.ark.transloader.clone.CloningStrategy;
import com.alipay.sofa.ark.transloader.clone.reflect.CyclicReferenceSafeTraverser.Traversal;
import com.alipay.sofa.ark.transloader.clone.reflect.instance.InstantiationStrategy;
import com.alipay.sofa.ark.transloader.util.Assert;
import com.alipay.sofa.ark.transloader.util.ExecuteUtils;

import java.util.Map;

/**
 * A <code>CloningStrategy</code> that uses Java Reflection as its mechanism. Can clone whole object graphs or just
 * necessary parts depending on how it is configured.
 *
 * @author hanyue
 * @version : ReflectionCloningStrategy.java, v 0.1 2022年06月04日 8:50 AM hanyue Exp $
 */
public final class ReflectionCloningStrategy implements CloningStrategy {
    private final CyclicReferenceSafeTraverser cyclicReferenceSafeTraverser = new CyclicReferenceSafeTraverser();

    private final CloningDecisionStrategy decider;
    private final InnerCloner             arrayCloner;
    private final InnerCloner             normalObjectCloner;
    private final CloningStrategy         fallbackCloner;

    /**
     * Contructs a new <code>ReflectionCloningStrategy</code> with its dependencies injected.
     *
     * @param decider        the strategy by which the decision to clone or not to clone a particular given
     *                       object is made
     * @param instantiator   the strategy by which to use instantiate normal objects (as opposed to arrays, for which
     *                       standard reflection is always adequate)
     * @param fallbackCloner the <code>CloningStrategy</code> to fall back to when <code>this</code>
     *                       strategy fails
     */
    public ReflectionCloningStrategy(CloningDecisionStrategy decider,
                                     InstantiationStrategy instantiator,
                                     CloningStrategy fallbackCloner) {
        Assert.areNotNull(decider, instantiator, fallbackCloner);
        this.decider = decider;
        this.arrayCloner = new InnerArrayCloner(this);
        this.normalObjectCloner = new InnerNormalObjectCloner(this, instantiator);
        this.fallbackCloner = fallbackCloner;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link CyclicReferenceSafeTraverser} to sucessfully handle cyclic references in the
     * given object graph.
     * </p>
     *
     * @return a completely or partially cloned object graph, depending on the <code>CloningDecisionStrategy</code>
     * injected in
     * {@link #ReflectionCloningStrategy(CloningDecisionStrategy, InstantiationStrategy, CloningStrategy)},
     * with potentially the <code>original</code> itself being the top-level object in the graph returned if
     * it was not cloned
     */
    @Override
    public Object cloneObjectUsingClassLoader(Object original, ClassLoader targetClassLoader)
            throws Exception {
        if (original == null) {
            return null;
        }
        Assert.isNotNull(targetClassLoader);
        Traversal cloningTraversal = new Traversal() {
            public Object traverse(Object currentObject, Map referenceHistory) throws Exception {
                return ReflectionCloningStrategy.this.clone(currentObject, targetClassLoader,
                        referenceHistory);
            }
        };
        return cyclicReferenceSafeTraverser.performWithoutFollowingCircles(cloningTraversal,
                original);
    }

    private Object clone(Object original, ClassLoader targetClassLoader, Map cloneHistory) throws Exception {
        if (original == null) {return null;}
        try {
            return performIntendedCloning(original, targetClassLoader, cloneHistory);
        } catch (Throwable e) {
            return ExecuteUtils.executeAttachExecption(() -> {
                try {
                    return performFallbackCloning(original, targetClassLoader);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }, e);
        }
    }

    private Object performIntendedCloning(Object original, ClassLoader targetClassLoader,
                                          Map cloneHistory) throws Exception {
        InnerCloner innerCloner = original.getClass().isArray() ? arrayCloner : normalObjectCloner;
        Object clone = original;
        if (decider.shouldCloneObjectItself(original, targetClassLoader)) {
            clone = innerCloner.instantiateClone(original, targetClassLoader);
        }
        cloneHistory.put(original, clone);
        if (decider.shouldCloneObjectContent(original, targetClassLoader)) {
            innerCloner.cloneContent(original, clone, targetClassLoader);
        }
        return clone;
    }

    private Object performFallbackCloning(Object original, ClassLoader targetClassLoader)
            throws Exception {
        return fallbackCloner.cloneObjectUsingClassLoader(original, targetClassLoader);
    }
}