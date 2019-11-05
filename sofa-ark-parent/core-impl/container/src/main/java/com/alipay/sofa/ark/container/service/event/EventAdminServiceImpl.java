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

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.plugin.AfterPluginStopEvent;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class EventAdminServiceImpl implements EventAdminService, EventHandler {

    private final static ConcurrentMap<ClassLoader, CopyOnWriteArraySet<EventHandler>> SUBSCRIBER_MAP = new ConcurrentHashMap<>();

    private final static Logger                                                        LOGGER         = ArkLoggerFactory
                                                                                                          .getDefaultLogger();

    @Inject
    private RegistryService                                                            registryService;

    public EventAdminServiceImpl() {
        register(this);
    }

    @Override
    public void sendEvent(ArkEvent event) {
        List<EventHandler> eventHandlers = new ArrayList<>();
        for (CopyOnWriteArraySet<EventHandler> values : SUBSCRIBER_MAP.values()) {
            eventHandlers.addAll(values);
        }
        for (ServiceReference<EventHandler> eventHandler : registryService.referenceServices(
            EventHandler.class, null)) {
            eventHandlers.add(eventHandler.getService());
        }
        Collections.sort(eventHandlers, new OrderComparator());
        for (EventHandler eventHandler : eventHandlers) {
            if (isSupportEventType(eventHandler, event)) {
                eventHandler.handleEvent(event);
            }
        }
    }

    @Override
    public void register(EventHandler eventHandler) {
        CopyOnWriteArraySet<EventHandler> set = SUBSCRIBER_MAP.get(eventHandler.getClass()
            .getClassLoader());
        if (set == null) {
            set = new CopyOnWriteArraySet<>();
            CopyOnWriteArraySet<EventHandler> old = SUBSCRIBER_MAP.putIfAbsent(eventHandler
                .getClass().getClassLoader(), set);
            if (old != null) {
                set = old;
            }
        }
        set.add(eventHandler);
        LOGGER.debug(String.format("Register event handler: %s.", eventHandler));
    }

    @Override
    public void unRegister(EventHandler eventHandler) {
        CopyOnWriteArraySet<EventHandler> set = SUBSCRIBER_MAP.get(eventHandler.getClass()
            .getClassLoader());
        if (set != null) {
            set.remove(eventHandler);
            LOGGER.debug(String.format("Unregister event handler: %s.", eventHandler));
        }
    }

    @Override
    public void unRegister(ClassLoader classLoader) {
        SUBSCRIBER_MAP.remove(classLoader);
        LOGGER.debug(String.format("Unregister event handler of classLoader: %s.", classLoader));

    }

    @Override
    public void handleEvent(ArkEvent event) {
        ClassLoader classLoader = null;

        if (event instanceof AfterBizStopEvent) {
            classLoader = ((AfterBizStopEvent) event).getSource().getBizClassLoader();
        } else if (event instanceof AfterPluginStopEvent) {
            classLoader = ((AfterPluginStopEvent) event).getSource().getPluginClassLoader();
        }

        if (classLoader != null) {
            unRegister(classLoader);
        }
    }

    @Override
    public int getPriority() {
        return PriorityOrdered.LOWEST_PRECEDENCE;
    }

    private boolean isSupportEventType(EventHandler eventHandler, ArkEvent event) {
        boolean isSupport = false;
        String typeName = Constants.EMPTY_STR;
        try {
            Class<? extends EventHandler> aClass = eventHandler.getClass();
            Type[] types = aClass.getGenericInterfaces();
            if (types != null && types.length == 1) {
                Type type = types[0];
                if (type instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) type)
                        .getActualTypeArguments();
                    if (actualTypeArguments.length == 1) {
                        typeName = actualTypeArguments[0].getTypeName();
                        isSupport = typeName.equalsIgnoreCase(event.getClass().getName());
                    }
                }
            }
        } catch (Throwable t) {
            // ignore
        }

        if (!isSupport) {
            isSupport = !(event instanceof AbstractArkEvent)
                        && (typeName.equalsIgnoreCase(ArkEvent.class.getName()) || typeName
                            .equalsIgnoreCase(Constants.EMPTY_STR));
        }

        return isSupport;
    }
}