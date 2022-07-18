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
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.AfterFinishStartupEvent;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/11/19 11:43 AM
 * @since
 **/
public class MultiEventTest extends BaseTest {

    EventAdminService   eventAdminService;

    static List<String> result = new ArrayList<>();

    @Before
    public void before() {
        super.before();
        eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        eventAdminService.register(new MultiAfterFinishStartupEventHandler());

        eventAdminService.register(new AbstractEventHandler());
    }

    @After
    public void after() {
        result.clear();
        super.after();
    }

    @Test
    public void testEvent() {
        eventAdminService.sendEvent(new AfterFinishStartupEvent());
        Assert.assertTrue(result.get(0).equalsIgnoreCase(
            Constants.ARK_EVENT_TOPIC_AFTER_FINISH_STARTUP_STAGE));
    }

    static class MultiAfterFinishStartupEventHandler implements
                                                    EventHandler<AfterFinishStartupEvent>,
                                                    OtherHandler<TestEvent.SubTestEvent> {

        @Override
        public void handleEvent(AfterFinishStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    interface OtherHandler<T extends TestEvent> {
    }

    static class TestEvent {
        public static class SubTestEvent extends TestEvent {

        }
    }

    static class AbstractEventHandler implements EventHandler<AbstractArkEvent> {

        @Override
        public void handleEvent(AbstractArkEvent event) {
            if (event instanceof AfterFinishStartupEvent) {
                result.add(event.getTopic());
            }
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

}
