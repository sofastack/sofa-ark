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
import com.alipay.sofa.ark.transloader.util.Assert;

/**
 * The default implementation of <code>Transloader</code>.
 *
 * @author hanyue
 * @version : DefaultTransloader.java, v 0.1 2022年06月03日 2:50 PM hanyue Exp $
 */
public class DefaultTransloader implements Transloader {

    private final CloningStrategy cloningStrategy;

    /**
     * Contructs a new <code>Transloader</code> to produce wrappers, the <code>ObjectWrapper</code>s being
     * configured with the given <code>CloningStrategy</code>.
     *
     * @param cloningStrategy the <code>CloningStrategy</code> with which to configure <code>ObjectWrapper</code>s
     */
    public DefaultTransloader(CloningStrategy cloningStrategy) {
        Assert.isNotNull(cloningStrategy);
        this.cloningStrategy = cloningStrategy;
    }

    /**
     * {@inheritDoc}
     *
     * @return an <code>ObjectWrapper</code> around the given object, configured with the {@link CloningStrategy} that
     * this factory is configured with
     */
    @Override
    public ObjectWrapper wrap(Object objectToWrap) {
        return new ObjectWrapper(objectToWrap, cloningStrategy);
    }

    /**
     * {@inheritDoc}
     *
     * @return a <code>ClassWrapper</code> around the given <code>Class</code>
     */
    @Override
    public ClassWrapper wrap(Class classToWrap) {
        return new ClassWrapper(classToWrap, cloningStrategy);
    }
}