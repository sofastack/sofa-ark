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

/**
 * Invoker Field in class or object
 * {@link com.alipay.sofa.ark.transloader.ClassWrapper}
 * {@link com.alipay.sofa.ark.transloader.ObjectWrapper}
 *
 * @author hanyue
 * @version : InvokerField.java, v 0.1 2022年06月04日 3:32 PM hanyue Exp $
 */
public interface InvokerField extends Invoker {

    /**
     * Invokes on the wrapped object the field described by the given invocation description, with the parameters given
     * by the same. Finds the method reflectively using parameter types loaded from the wrapped object's
     * <code>ClassLoader</code>(s). Any parameters which refer to <code>Class</code>es that are foreign to the
     * wrapped object's <code>ClassLoader</code>(s) are cloned using the <code>CloningStrategy</code> injected at
     * construction.
     *
     * @param fieldDescription the description of the invocation to be performed
     * @return the result of performing the invocation described by <code>description</code>
     */
    Object invoke(InvocationFieldDescription fieldDescription);

    /**
     * You can use it to call fields and can castable in current ClassLoader
     * There is performance loss, please try not to use
     *
     * @param fieldDescription the description of the invocation to be performed
     * @return the result of performing the invocation described by <code>description</code>
     */
    Object invokeCastable(InvocationFieldDescription fieldDescription);
}