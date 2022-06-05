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
package com.alipay.sofa.ark.transloader.invoke;

import java.lang.reflect.Proxy;

/**
 * Invoker Method in class or object
 * {@link com.alipay.sofa.ark.transloader.ClassWrapper}
 * {@link com.alipay.sofa.ark.transloader.ObjectWrapper}
 *
 * @author hanyue
 * @version : InvokerMethod.java, v 0.1 2022年06月04日 3:32 PM hanyue Exp $
 */
public interface InvokerMethod extends Invoker {

    /**
     * Invokes on the wrapped object the method described by the given invocation description, with the parameters given
     * by the same. Finds the method reflectively using parameter types loaded from the wrapped object's
     * <code>ClassLoader</code>(s). Any parameters which refer to <code>Class</code>es that are foreign to the
     * wrapped object's <code>ClassLoader</code>(s) are cloned using the <code>CloningStrategy</code> injected at
     * construction.
     *
     * @param methodDescription the description of the invocation to be performed
     * @return the result of performing the invocation described by <code>description</code>
     */
    Object invoke(InvocationMethodDescription methodDescription);

    /**
     * You can use it to call methods and can castable in current ClassLoader
     * There is performance loss, please try not to use
     *
     * @param methodDescription the description of the invocation to be performed
     * @return the result of performing the invocation described by <code>description</code>
     */
    Object invokeCastable(InvocationMethodDescription methodDescription);

    /**
     * Makes an implementation of the given <code>interface</code> that calls through to the wrapped object. This is
     * particularly useful if you have access in the current <code>ClassLoader</code> to an <code>interface</code>
     * that the wrapped object is expected to implement, except that it actually implements the equivalent from a
     * different <code>ClassLoader</code>. It is therefore usefully employed in conjunction with
     * {@link com.alipay.sofa.ark.transloader.ObjectWrapper#isInstanceOf(String)}.
     * <p>
     * This method will not fail fast if the wrapped object does not implement its own <code>ClassLoader</code>'s
     * equivalent to the given <code>interface</code>, so can also be used for "duck"-typing, as a more syntactically
     * elegant alternative to using {@link #invoke(InvocationMethodDescription)}, if desired.
     * </p>
     *
     * @param targetInterface the <code>interface</code> that the returned object can be cast to
     * @return a {@link Proxy} to the wrapped object that implements <code>desiredInterface</code>
     */
    <T> T makeCastableTo(Class<T> targetInterface);
}