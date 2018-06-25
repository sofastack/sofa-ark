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
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.registry.ServiceMetadata;
import com.alipay.sofa.ark.spi.registry.ServiceProvider;

/**
 * Service Metadata Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ServiceMetadataImpl implements ServiceMetadata {

    private String          uniqueId;
    private Class<?>        interfaceClass;
    private ServiceProvider serviceProvider;

    public ServiceMetadataImpl(Class<?> interfaceClass, String uniqueId,
                               ServiceProvider serviceProvider) {
        AssertUtils.assertNotNull(interfaceClass, "Service interface should not be null.");
        AssertUtils.assertNotNull(serviceProvider, "Service provider should not be null.");
        this.uniqueId = uniqueId;
        this.interfaceClass = interfaceClass;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
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
    public String getServiceName() {
        if (StringUtils.isEmpty(uniqueId)) {
            return interfaceClass.getCanonicalName();
        } else {
            return String.format("%s:%s", interfaceClass.getCanonicalName(), uniqueId);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ServiceMetadata serviceMetadata = (ServiceMetadata) obj;

        if (!uniqueId.equals(serviceMetadata.getUniqueId())) {
            return false;
        }

        if (!interfaceClass.equals(serviceMetadata.getInterfaceClass())) {
            return false;
        }

        return serviceProvider.equals(serviceMetadata.getServiceProvider());
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + uniqueId.hashCode();
        result = 31 * result + interfaceClass.hashCode();
        result = 31 * result + serviceProvider.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("ServiceMetadata{service=\'%s\', provider=\'%s\'}", getServiceName(),
            getServiceProvider().toString());
    }
}