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

import com.alipay.sofa.ark.transloader.util.Assert;
import com.alipay.sofa.ark.transloader.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Proxy;

/**
 * A <code>CloningStrategy</code> that uses Java Serialization as its mechanism.
 *
 * @author hanyue
 * @version : SerializationCloningStrategy.java, v 0.1 2022年06月03日 4:32 PM hanyue Exp $
 */
public final class SerializationCloningStrategy implements CloningStrategy {

    /**
     * {@inheritDoc}
     *
     * @throws ClassCastException if the given <code>original</code> object is not {@link Serializable}
     * @throws Exception if serialization fails
     * @throws IOException if input fails during deserialization
     * @throws ClassNotFoundException if the <code>targetClassLoader</code> cannot find a required class
     */
    @Override
    public Object cloneObjectUsingClassLoader(Object original, ClassLoader targetClassLoader)
                                                                                             throws Exception {
        Assert.areNotNull(original, targetClassLoader);
        byte[] serializedOriginal = SerializationUtils.serialize((Serializable) original);
        return new ClassLoaderObjectInputStream(targetClassLoader, new ByteArrayInputStream(
            serializedOriginal)).readObject();
    }

    class ClassLoaderObjectInputStream extends ObjectInputStream {
        private final ClassLoader classLoader;

        public ClassLoaderObjectInputStream(ClassLoader classLoader, InputStream inputStream)
                                                                                             throws IOException {
            super(inputStream);
            this.classLoader = classLoader;
        }

        protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException,
                                                                            ClassNotFoundException {
            try {
                return Class.forName(objectStreamClass.getName(), false, this.classLoader);
            } catch (ClassNotFoundException var3) {
                return super.resolveClass(objectStreamClass);
            }
        }

        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException,
                                                                 ClassNotFoundException {
            Class<?>[] interfaceClasses = new Class[interfaces.length];

            for (int i = 0; i < interfaces.length; ++i) {
                interfaceClasses[i] = Class.forName(interfaces[i], false, this.classLoader);
            }

            try {
                return Proxy.getProxyClass(this.classLoader, interfaceClasses);
            } catch (IllegalArgumentException var4) {
                return super.resolveProxyClass(interfaces);
            }
        }
    }
}