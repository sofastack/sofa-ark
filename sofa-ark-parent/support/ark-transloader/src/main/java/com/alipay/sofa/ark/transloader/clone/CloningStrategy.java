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
package com.alipay.sofa.ark.transloader.clone;

import com.alipay.sofa.ark.transloader.clone.reflect.MaximalCloningDecisionStrategy;
import com.alipay.sofa.ark.transloader.clone.reflect.MinimalCloningDecisionStrategy;
import com.alipay.sofa.ark.transloader.clone.reflect.ReflectionCloningStrategy;
import com.alipay.sofa.ark.transloader.clone.reflect.instance.ObjenesisInstantiationStrategy;

/**
 * The API by which an object can be cloned using a different <code>ClassLoader</code> than that/those which loaded
 * the <code>Class</code>es it references. It can be used directly or serves as the strategy interface for
 * customising the behaviour of {@link com.alipay.sofa.ark.transloader.ObjectWrapper}s.
 *
 * @author hanyue
 * @version : CloningStrategy.java, v 0.1 2022年06月03日 3:26 PM hanyue Exp $
 */
public interface CloningStrategy {

    /**
     * The implementation which clones as little as possible to make the given object graph use <code>Class</code>es
     * that are the same as those that would be loaded through the given <code>ClassLoader</code>. This is the most
     * efficient implementation to use simply for the purpose of making sure the returned object graph is perfectly
     * compatible with all other objects referencing <code>Class</code>es loaded through the given
     * <code>ClassLoader</code>. It is also the implementation that is most likely to succeed in cloning any given
     * object graph because by attempting to clone as few objects as possible it is less likely to attempt to clone an
     * object that cannot be cloned (e.g. a non-{@link java.io.Serializable} with <code>final</code> fields in a
     * pre-Java-5 JVM).
     * <p>
     * However, the fact that it only clones what is necessary to make the object graph compatible with the given
     * <code>ClassLoader</code> means that usually <b>not all</b> of the objects in the graph will be cloned. This
     * means that, depending on the <code>Class</code>es in the object graph and which of these is the same if loaded
     * through the given <code>ClassLoader</code>, it is possible that a top level object is not cloned but objects
     * it references through fields are cloned. This effectively means that the existing object graph can be altered
     * rather than a new, purely seperate object graph being created. This <b>may not be what you want</b> if you want
     * to continue using the given object in its original context. In which case, use {@link CloningStrategy#MAXIMAL}.
     * </p>
     *
     * @see ReflectionCloningStrategy
     * @see MinimalCloningDecisionStrategy
     */
    CloningStrategy MINIMAL = new ReflectionCloningStrategy(new MinimalCloningDecisionStrategy(),
            new ObjenesisInstantiationStrategy(),
            new SerializationCloningStrategy());

    /**
     * The implementation which clones every <code>Object</code> in the given object graph. The given object graph is
     * not altered in any way and a completely new copy of the object graph referencing only <code>Class</code>es
     * loaded through the given <code>ClassLoader</code> is returned. Only primitives, which are not
     * <code>Object</code>s, are not cloned because they cannot be, as there is no concept of different references to
     * the same primitive value in Java. Similiar in behaviour to {@link SerializationCloningStrategy} except that it
     * can clone more object graphs because it does not rely on all referenced objects being
     * {@link java.io.Serializable} and also performs much faster than serialization.
     *
     * @see ReflectionCloningStrategy
     * @see MaximalCloningDecisionStrategy
     */
    CloningStrategy MAXIMAL = new ReflectionCloningStrategy(new MaximalCloningDecisionStrategy(),
            new ObjenesisInstantiationStrategy(),
            new SerializationCloningStrategy());

    /**
     * Clones the given object using the given <code>ClassLoader</code>.
     *
     * @param original          the original object to be cloned
     * @param targetClassLoader the <code>ClassLoader</code> by which to load <code>Class</code>es for clones
     * @return the result of cloning the object graph
     * @throws Exception can throw any <code>Exception</code> depending on the implementation
     */
    Object cloneObjectUsingClassLoader(Object original, ClassLoader targetClassLoader)
            throws Exception;
}