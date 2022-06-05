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

import com.alipay.sofa.ark.transloader.util.ExecuteUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisSerializer;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Uses {@link ObjenesisStd} to create new instances of <code>Class</code>es without invoking their constructors.
 *
 * @author hanyue
 * @version : ObjenesisInstantiationStrategy.java, v 0.1 2022年06月04日 9:26 AM hanyue Exp $
 */
public class ObjenesisInstantiationStrategy implements InstantiationStrategy {
    private final Objenesis                   defaultObjenesis    = new ObjenesisStd();
    private final Map<ClassLoader, Objenesis> objenesis           = new ConcurrentHashMap<>();

    private final ObjenesisSerializer         objenesisSerializer = new ObjenesisSerializer();

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Object newInstance(Class type) throws Exception {
        try {
            if (type.getClassLoader() == null) {
                return defaultObjenesis.newInstance(type);
            } else if (!objenesis.containsKey(type.getClassLoader())) {
                objenesis.put(type.getClassLoader(), new ObjenesisStd());
            }
            return objenesis.get(type.getClassLoader()).newInstance(type);
        } catch (Throwable e) {
            return ExecuteUtils.executeAttachExecption(() -> objenesisSerializer.newInstance(type), e);
        }
    }
}