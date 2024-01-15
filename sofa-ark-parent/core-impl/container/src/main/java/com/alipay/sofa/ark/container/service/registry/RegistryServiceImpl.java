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
package com.alipay.sofa.ark.container.service.registry;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.registry.DefaultServiceFilter;
import com.alipay.sofa.ark.container.registry.ServiceMetadataImpl;
import com.alipay.sofa.ark.container.registry.ServiceReferenceImpl;
import com.alipay.sofa.ark.spi.registry.*;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Registry Service Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class RegistryServiceImpl implements RegistryService {

    private CopyOnWriteArraySet<ServiceReference<?>> services        = new CopyOnWriteArraySet<>();

    private OrderComparator                          orderComparator = new OrderComparator();

    @Inject
    private InjectionService                         injectionService;

    @Override
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject,
                                                  ServiceProvider serviceProvider) {
        return publishService(ifClass, implObject, StringUtils.EMPTY_STRING, serviceProvider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject, String uniqueId,
                                                  ServiceProvider serviceProvider) {
        AssertUtils.assertNotNull(ifClass, "Service interface should not be null.");
        AssertUtils.assertNotNull(implObject, "Service implementation should not be null.");
        AssertUtils.assertNotNull(uniqueId, "Service uniqueId should not be null.");
        AssertUtils.assertNotNull(serviceProvider, "ServiceProvider should not be null.");

        ServiceMetadata serviceMetadata = new ServiceMetadataImpl(ifClass, uniqueId,
            serviceProvider);
        for (ServiceReference<?> serviceReference : services) {
            if (serviceMetadata.equals(serviceReference.getServiceMetadata())) {
                ArkLoggerFactory.getDefaultLogger().warn(
                    String.format("Service: %s publish by: %s already exist",
                        serviceMetadata.getServiceName(), serviceProvider));
                return (ServiceReference<T>) serviceReference;
            }
        }

        ServiceReference<T> serviceReference = new ServiceReferenceImpl<>(serviceMetadata,
            implObject);
        injectionService.inject(serviceReference);

        ArkLoggerFactory.getDefaultLogger().info(
            String.format("Service: %s publish by: %s succeed", serviceMetadata.getServiceName(),
                serviceProvider));

        services.add(serviceReference);

        return serviceReference;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass) {
        return referenceService(ifClass, StringUtils.EMPTY_STRING);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ServiceReference<T> referenceService(Class<T> ifClass, String uniqueId) {
        List<ServiceReference<T>> references = referenceServices(ifClass, uniqueId);
        return references.isEmpty() ? null : references.get(0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass) {
        return referenceServices(ifClass, StringUtils.EMPTY_STRING);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass, String uniqueId) {
        DefaultServiceFilter<T> defaultServiceFilter = new DefaultServiceFilter<>();
        // only conditional on interface and uniqueId
        defaultServiceFilter.setServiceInterface(ifClass).setUniqueId(uniqueId);
        List<ServiceReference<T>> references = referenceServices(defaultServiceFilter);
        return references;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<ServiceReference<T>> referenceServices(ServiceFilter<T> serviceFilter) {
        List<ServiceReference<T>> serviceReferences = new ArrayList<>();

        for (ServiceReference<?> reference : services) {
            if (serviceFilter.match(reference)) {
                serviceReferences.add((ServiceReference<T>) reference);
            }
        }
        Collections.sort(serviceReferences, orderComparator);

        return serviceReferences;
    }

    @Override
    public int unPublishServices(ServiceFilter serviceFilter) {
        int count = 0;

        for (ServiceReference<?> reference : services) {
            if (serviceFilter.match(reference)) {
                services.remove(reference);
                count += 1;
            }
        }

        return count;
    }

}
