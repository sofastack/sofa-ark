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
import com.alipay.sofa.ark.container.registry.ArkContainerServiceProvider;
import com.alipay.sofa.ark.container.registry.ServiceMetadataImpl;
import com.alipay.sofa.ark.spi.registry.ServiceProviderComparator;
import com.alipay.sofa.ark.container.registry.ServiceReferenceImpl;
import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceMetadata;
import com.alipay.sofa.ark.spi.registry.ServiceProvider;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry Service Implement
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class RegistryServiceImpl implements RegistryService {

    private static final ArkLogger                               LOGGER                    = ArkLoggerFactory
                                                                                               .getDefaultLogger();

    private ConcurrentHashMap<String, List<ServiceReference<?>>> services                  = new ConcurrentHashMap<>();

    private ServiceProviderComparator                            serviceProviderComparator = new ServiceProviderComparator();

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject) {
        return publishService(ifClass, implObject, new ArkContainerServiceProvider());
    }

    @Override
    public <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject,
                                                  ServiceProvider serviceProvider) {
        ServiceMetadata serviceMetadata = new ServiceMetadataImpl(ifClass.getName(), ifClass,
            serviceProvider);
        if (!services.containsKey(serviceMetadata.getServiceName())) {
            services.putIfAbsent(serviceMetadata.getServiceName(),
                new CopyOnWriteArrayList<ServiceReference<?>>());
        }

        List<ServiceReference<?>> serviceReferences = services
            .get(serviceMetadata.getServiceName());

        for (ServiceReference<?> serviceReference : serviceReferences) {
            if (serviceMetadata.equals(serviceReference.getServiceMetadata())) {
                LOGGER.warn(String.format("Service: %s publish by: %s already exist",
                    serviceMetadata.getServiceName(), serviceProvider));
                return (ServiceReference<T>) serviceReference;
            }
        }

        ServiceReference<T> serviceReference = new ServiceReferenceImpl<>(serviceMetadata,
            implObject);

        LOGGER.info(String.format("Service: %s publish by: %s succeed",
            serviceMetadata.getServiceName(), serviceProvider));

        serviceReferences.add(serviceReference);

        return serviceReference;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass) {
        return referenceService(ifClass, null);
    }

    @Override
    public <T> ServiceReference<T> referenceService(Class<T> ifClass, ServiceFilter serviceFilter) {
        String serviceName = ifClass.getName();
        if (services.containsKey(serviceName)) {
            return findHighestOrderService(services.get(serviceName), serviceFilter);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceReference<T> findHighestOrderService(List<ServiceReference<?>> serviceReferences,
                                                            ServiceFilter serviceFilter) {
        ServiceReference<T> result = null;
        for (ServiceReference<?> serviceReference : serviceReferences) {
            if (serviceFilter != null
                && !serviceFilter.match(serviceReference.getServiceMetadata().getServiceProvider())) {
                continue;
            }
            if (result == null) {
                result = (ServiceReference<T>) serviceReference;
            } else if (serviceProviderComparator.compare(serviceReference.getServiceMetadata()
                .getServiceProvider(), result.getServiceMetadata().getServiceProvider()) < 0) {

                result = (ServiceReference<T>) serviceReference;
            }

        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass) {
        return referenceServices(ifClass, null);
    }

    @Override
    public <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass,
                                                           ServiceFilter serviceFilter) {
        String serviceName = ifClass.getName();
        if (services.containsKey(serviceName)) {
            List<ServiceReference<T>> serviceReferences = new ArrayList<>();
            for (ServiceReference<?> reference : services.get(serviceName)) {
                if (serviceFilter == null
                    || serviceFilter.match(reference.getServiceMetadata().getServiceProvider())) {
                    serviceReferences.add((ServiceReference<T>) reference);
                }
            }
            return serviceReferences;
        }
        return Collections.emptyList();
    }
}