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
import com.alipay.sofa.ark.spi.registry.ServiceProvider;

/**
 * Service Metadata Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ServiceMetadataImpl implements ServiceMetadata {

    private String          serviceName;
    private Class<?>        interfaceClass;
    private ServiceProvider serviceProvider;

    public ServiceMetadataImpl(String serviceName, Class<?> interfaceClass,
                               ServiceProvider serviceProvider) {
        this.serviceName = serviceName;
        this.interfaceClass = interfaceClass;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ServiceMetadata serviceMetadata = (ServiceMetadataImpl) obj;

        if (!serviceName.equals(serviceMetadata.getServiceName())) {
            return false;
        }

        return serviceProvider.equals(serviceMetadata.getServiceProvider());
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + serviceName.hashCode();
        result = 31 * result + serviceProvider.hashCode();
        return result;
    }
}