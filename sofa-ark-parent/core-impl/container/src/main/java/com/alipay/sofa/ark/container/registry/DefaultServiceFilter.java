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

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.spi.registry.*;

/**
 * Filter Service according to the given {@linkplain com.alipay.sofa.ark.spi.registry.ServiceProvider}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class DefaultServiceFilter<T> implements ServiceFilter<T> {

    private ServiceProviderType providerType;

    private Class<?>            serviceInterface;

    private String              uniqueId;

    @Override
    public boolean match(ServiceReference serviceReference) {
        AssertUtils.assertNotNull(serviceReference, "ServiceReference should not be null");
        ServiceMetadata serviceMetadata = serviceReference.getServiceMetadata();
        ServiceProvider provider = serviceMetadata.getServiceProvider();

        boolean isMatch = matchProviderType(provider.getServiceProviderType());
        isMatch &= matchServiceInterface(serviceMetadata.getInterfaceClass());
        isMatch &= matchUniqueId(serviceMetadata.getUniqueId());
        return isMatch;
    }

    private boolean matchProviderType(ServiceProviderType serviceProviderType) {
        if (providerType == null) {
            return true;
        }
        return providerType.equals(serviceProviderType);
    }

    private boolean matchServiceInterface(Class serviceInterface) {
        if (this.serviceInterface == null) {
            return true;
        }
        return this.serviceInterface.equals(serviceInterface);
    }

    private boolean matchUniqueId(String uniqueId) {
        if (this.uniqueId == null) {
            return true;
        }
        return this.uniqueId.equals(uniqueId);
    }

    public ServiceProviderType getProviderType() {
        return providerType;
    }

    public DefaultServiceFilter setProviderType(ServiceProviderType providerType) {
        this.providerType = providerType;
        return this;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public DefaultServiceFilter setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public DefaultServiceFilter setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
        return this;
    }
}