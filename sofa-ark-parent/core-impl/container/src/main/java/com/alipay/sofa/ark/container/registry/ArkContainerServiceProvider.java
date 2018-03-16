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

/**
 * Ark Container Service Provider, default service provider if provider is not set
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkContainerServiceProvider implements ServiceProvider {

    private static final String DEFAULT_ARK_SERVICE_PROVIDER_NAME     = "ArkContainer";
    private static final int    DEFAULT_ARK_SERVICE_PROVIDER_PRIORITY = 1000;

    private String              providerName                          = DEFAULT_ARK_SERVICE_PROVIDER_NAME;
    private int                 priority                              = DEFAULT_ARK_SERVICE_PROVIDER_PRIORITY;

    public ArkContainerServiceProvider() {
    }

    public ArkContainerServiceProvider(String providerName) {
        this.providerName = providerName;
    }

    public ArkContainerServiceProvider(String providerName, int priority) {
        this.providerName = providerName;
        this.priority = priority;
    }

    @Override
    public ServiceProviderType getServiceProviderType() {
        return ServiceProviderType.ARK_CONTAINER;
    }

    @Override
    public String getServiceProviderName() {
        return providerName;
    }

    @Override
    public int getServiceProviderPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "ArkContainerServiceProvider{" + "providerName='" + providerName + '\''
               + ", priority=" + priority + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ArkContainerServiceProvider that = (ArkContainerServiceProvider) o;

        if (priority != that.priority) {
            return false;
        }
        return providerName != null ? providerName.equals(that.providerName)
            : that.providerName == null;
    }

    @Override
    public int hashCode() {
        int result = providerName != null ? providerName.hashCode() : 0;
        result = 31 * result + priority;
        return result;
    }
}