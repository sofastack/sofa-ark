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
package com.alipay.sofa.ark.spi.service.extension;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;

import java.util.Objects;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ExtensionClass<I, T> implements PriorityOrdered {
    /**
     * extensible interface type
     */
    private Class<I>   interfaceClass;

    /**
     * extension implementation type
     */
    private Class<I>   implementClass;

    /**
     * annotation on {@link ExtensionClass#interfaceClass}
     */
    private Extensible extensible;

    /**
     * annotation on {@link ExtensionClass#implementClass}
     */
    private Extension  extension;

    /**
     * where extension implementation is defined, ark plugin or ark biz.
     * now it only support ark plugin.
     */
    private T          definedLocation;

    /**
     * if extensible interface type is singleton, return this as extension implementation.
     */
    private I          singleton;

    public Class<I> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<I> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Class<I> getImplementClass() {
        return implementClass;
    }

    public void setImplementClass(Class<I> implementClass) {
        this.implementClass = implementClass;
    }

    public Extensible getExtensible() {
        return extensible;
    }

    public void setExtensible(Extensible extensible) {
        this.extensible = extensible;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    public T getDefinedLocation() {
        return definedLocation;
    }

    public void setDefinedLocation(T definedLocation) {
        this.definedLocation = definedLocation;
    }

    public I getSingleton() {
        if (singleton == null) {
            synchronized (this) {
                singleton = newInstance();
            }
        }
        return singleton;
    }

    public I getObject() {
        if (extensible.singleton()) {
            return getSingleton();
        } else {
            return newInstance();
        }
    }

    private I newInstance() {
        try {
            return implementClass.newInstance();
        } catch (Throwable throwable) {
            throw new ArkRuntimeException(String.format("Create %s instance error.",
                implementClass.getCanonicalName()), throwable);
        }
    }

    @Override
    public int getPriority() {
        return extension.order();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceClass, implementClass, extensible, extension, definedLocation,
            singleton);
    }
}
