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
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.event.BizEvent;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.google.inject.Singleton;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_EVENT_TOPIC_UNINSTALL;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class EventAdminServiceImpl implements EventAdminService {

    private final static ConcurrentMap<ClassLoader, CopyOnWriteArraySet<EventHandler>> SUBSCRIBER_MAP = new ConcurrentHashMap<>();
    private final static Logger                                                        LOGGER         = ArkLoggerFactory
                                                                                                          .getDefaultLogger();

    public EventAdminServiceImpl() {
        register(new DefaultBizEventHandler());
    }

    @Override
    public void sendEvent(ArkEvent event) {
        List<EventHandler> eventHandlers = new ArrayList<>();
        for (CopyOnWriteArraySet<EventHandler> values : SUBSCRIBER_MAP.values()) {
            eventHandlers.addAll(values);
        }
        Collections.sort(eventHandlers, new EventComparator());
        for (EventHandler eventHandler : eventHandlers) {
            eventHandler.handleEvent(event);
        }
    }

    @Override
    public void register(EventHandler eventHandler) {
        CopyOnWriteArraySet<EventHandler> set = SUBSCRIBER_MAP.get(eventHandler.getClass()
            .getClassLoader());
        if (set == null) {
            set = new CopyOnWriteArraySet<EventHandler>();
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

    class EventComparator implements Comparator<EventHandler> {
        @Override
        public int compare(EventHandler o1, EventHandler o2) {
            return Integer.compare(o1.getPriority(), o2.getPriority());
        }
    }

    class DefaultBizEventHandler implements EventHandler {
        @Override
        public void handleEvent(ArkEvent event) {
            if (!(event instanceof BizEvent)) {
                return;
            }
            if (BIZ_EVENT_TOPIC_UNINSTALL.equals(event.getTopic())) {
                unRegister(((BizEvent) event).getBiz().getBizClassLoader());
            }
        }

        @Override
        public int getPriority() {
            return PriorityOrdered.LOWEST_PRECEDENCE;
        }
    }
}