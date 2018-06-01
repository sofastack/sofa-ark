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

import com.alipay.sofa.ark.spi.registry.ServiceProvider;
import com.alipay.sofa.ark.spi.registry.ServiceProviderType;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;

/**
 * Abstract Service Provider
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public abstract class AbstractServiceProvider implements ServiceProvider {

    protected ServiceProviderType providerType;

    public AbstractServiceProvider(ServiceProviderType providerType) {
        this.providerType = providerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        if (getPriority() != ((PriorityOrdered) obj).getPriority()) {
            return false;
        }

        return getServiceProviderType() == ((ServiceProvider) obj).getServiceProviderType();
    }

    @Override
    public ServiceProviderType getServiceProviderType() {
        return providerType;
    }

    @Override
    public String getServiceProviderDesc() {
        return getServiceProviderType().getDesc();
    }

    @Override
    public String toString() {
        return String.format("ServiceProvider{provider=\'%s\', order=%d}",
            getServiceProviderDesc(), getPriority());
    }

    @Override
    public int hashCode() {
        int result = getServiceProviderType().hashCode();
        result = 31 * result + getPriority();
        return result;
    }
}