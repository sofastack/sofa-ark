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
package com.alipay.sofa.ark.transloader;

import com.alipay.sofa.ark.transloader.clone.CloningStrategy;

/**
 * The API by which to wrap objects that reference <code>Class</code>es from foreign <code>ClassLoader</code>s.
 *
 * @author hanyue
 * @version : Transloader.java, v 0.1 2022年06月03日 2:26 PM hanyue Exp $
 */
public interface Transloader {

    /**
     * The default implementation of <code>Transloader</code> which will produce {@link ObjectWrapper}s configured
     * with {@link CloningStrategy#MAXIMAL} for <code>Object</code>s and {@link ClassWrapper}s for
     * <code>Class</code>es.
     */
    Transloader DEFAULT = new DefaultTransloader(CloningStrategy.MINIMAL);

    /**
     * Wraps the given object in an <code>ObjectWrapper</code>.
     *
     * @param objectToWrap the object to wrap
     * @return the wrapper around the given object
     */
    ObjectWrapper wrap(Object objectToWrap);

    /**
     * Wraps the given <code>Class</code> in a <code>ClassWrapper</code>.
     *
     * @param classToWrap the <code>Class</code> to wrap
     * @return the wrapper around the given <code>Class</code>
     */
    ClassWrapper wrap(Class classToWrap);
}