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

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class EventAdminServiceTest extends BaseTest {

    private static int mark = 5;
    static Map         map  = new HashMap();

    @Test
    public void test() throws Throwable {

        try {
            Field field = EventAdminServiceImpl.class.getDeclaredField("SUBSCRIBER_MAP");
            field.setAccessible(true);
            map = (Map) field.get(null);
        } catch (Throwable throwable) {
            Assert.assertNull(throwable);
        }
        EventAdminService eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        eventAdminService.register(new LowPriorityMockEventHandler());
        eventAdminService.register(new HighPriorityMockEventHandler());
        eventAdminService.register(new BeforeBizStopEventHandler());

        ClassLoader bizClassLoader = getClass().getClassLoader();
        Biz biz = new BizModel().setBizState(BizState.DEACTIVATED).setBizName("mock name")
            .setBizVersion("mock name").setClassLoader(bizClassLoader);
        Assert.assertNotNull(map.get(bizClassLoader));
        biz.stop();
        Assert.assertNull(map.get(bizClassLoader));
        Assert.assertTrue(mark == 50);
        EventHandler eventHandler = new LowPriorityMockEventHandler();
        eventAdminService.register(eventHandler);
        Assert.assertNotNull(map.get(bizClassLoader));
        eventAdminService.unRegister(eventHandler);
        Assert.assertFalse(((Set) map.get(bizClassLoader)).contains(eventHandler));
    }

    class HighPriorityMockEventHandler implements EventHandler<BeforeBizStopEvent> {

        @Override
        public void handleEvent(BeforeBizStopEvent event) {
            mark *= mark;
        }

        @Override
        public int getPriority() {
            return 10;
        }
    }

    class LowPriorityMockEventHandler implements EventHandler<BeforeBizStopEvent> {

        @Override
        public void handleEvent(BeforeBizStopEvent event) {
            mark += mark;
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }

    class BeforeBizStopEventHandler implements EventHandler<AfterBizStopEvent> {

        @Override
        public void handleEvent(AfterBizStopEvent event) {
            map.remove(getClass().getClassLoader());
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }
}