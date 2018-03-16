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
package com.alipay.sofa.ark.spi.registry;

import java.util.Comparator;

/**
 * Service Provider Comparator
 * 1. {@link ServiceProviderType#ARK_PLUGIN } is higher than  {@link ServiceProviderType#ARK_CONTAINER }
 * 2. If the same {@link ServiceProviderType} then priority is higher as {@link ServiceProvider#getServiceProviderPriority()} is lower
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ServiceProviderComparator implements Comparator<ServiceProvider> {

    @Override
    public int compare(ServiceProvider o1, ServiceProvider o2) {
        if (!o1.getServiceProviderType().equals(o2.getServiceProviderType())) {
            if (ServiceProviderType.ARK_PLUGIN.equals(o1.getServiceProviderType())) {
                return -1;
            } else {
                return 1;
            }
        }

        return Integer.compare(o1.getServiceProviderPriority(), o2.getServiceProviderPriority());
    }
}