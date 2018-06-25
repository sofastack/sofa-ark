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
package com.alipay.sofa.ark.container.registry;

import com.alipay.sofa.ark.spi.registry.ServiceMetadata;
import com.alipay.sofa.ark.spi.registry.ServiceReference;

/**
 * Service Reference Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ServiceReferenceImpl<T> implements ServiceReference<T> {

    private ServiceMetadata serviceMetadata;

    private T               serviceObject;

    public ServiceReferenceImpl(ServiceMetadata serviceMetadata, T serviceObject) {
        this.serviceMetadata = serviceMetadata;
        this.serviceObject = serviceObject;
    }

    @Override
    public T getService() {
        return serviceObject;
    }

    @Override
    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    @Override
    public int getPriority() {
        return getServiceMetadata().getServiceProvider().getPriority();
    }

    @Override
    public int hashCode() {
        return serviceMetadata.hashCode();
    }

    @Override
    public String toString() {
        return serviceMetadata.toString();
    }
}