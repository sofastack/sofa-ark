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
package com.alipay.sofa.ark.container.service.event;

import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class EventAdminServiceImpl implements EventAdminService {

    @Inject
    private RegistryService registryService;

    @Override
    public void sendEvent(ArkEvent event) {
        List<ServiceReference<EventHandler>> eventHandlers = registryService
            .referenceServices(EventHandler.class);
        Collections.sort(eventHandlers, new EventComparator());
        for (ServiceReference<EventHandler> eventHandler : eventHandlers) {
            eventHandler.getService().handleEvent(event);
        }
    }

    class EventComparator implements Comparator<ServiceReference<EventHandler>> {
        @Override
        public int compare(ServiceReference<EventHandler> o1, ServiceReference<EventHandler> o2) {
            return Integer.compare(o1.getService().getPriority(), o2.getService().getPriority());
        }
    }
}