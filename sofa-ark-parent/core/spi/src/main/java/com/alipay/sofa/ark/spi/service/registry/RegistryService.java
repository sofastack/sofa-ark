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
package com.alipay.sofa.ark.spi.service.registry;

import com.alipay.sofa.ark.spi.registry.ServiceFilter;
import com.alipay.sofa.ark.spi.registry.ServiceProvider;
import com.alipay.sofa.ark.spi.registry.ServiceProviderComparator;
import com.alipay.sofa.ark.spi.registry.ServiceReference;

import java.util.List;

/**
 * Registry Service Interface
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface RegistryService {

    /**
     * Publish Service
     * @param ifClass service interface
     * @param implObject service implement object
     * @param <T>
     * @return
     */
    <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject);

    /**
     * Publish Service
     * @param ifClass service interface
     * @param implObject service implement object
     * @param serviceProvider service provider
     * @param <T>
     * @return
     */
    <T> ServiceReference<T> publishService(Class<T> ifClass, T implObject,
                                           ServiceProvider serviceProvider);

    /**
     * Get Service, when there are multiple services, return the highest priority service
     * see {@link ServiceProviderComparator } to get service priority definition
     *
     * @param ifClass service interface
     * @param <T>
     * @return service reference
     */
    <T> ServiceReference<T> referenceService(Class<T> ifClass);

    /**
     * Get Service with filter, see {@link ServiceFilter}
     * when there are multiple services, return the highest priority service matched with filter
     * see {@link ServiceProviderComparator } to get service priority definition
     * @param ifClass service interface
     * @param <T>
     * @param serviceFilter service filter
     * @return service reference
     */
    <T> ServiceReference<T> referenceService(Class<T> ifClass, ServiceFilter serviceFilter);

    /**
     * Get Service List
     * @param ifClass service interface
     * @param <T>
     * @return service reference list
     */
    <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass);

    /**
     * Get Service List with filter, see {@link ServiceFilter}
     * @param ifClass service interface
     * @param <T>
     * @param serviceFilter service filter
     * @return service reference list
     */
    <T> List<ServiceReference<T>> referenceServices(Class<T> ifClass, ServiceFilter serviceFilter);

}