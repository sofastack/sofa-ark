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
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.plugin.BeforePluginStartupEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/11/19 3:46 PM
 * @since
 **/
public class GlobalEventHandlerTest extends BaseTest {

    EventAdminService       eventAdminService;

    ArkEventHandler         arkEventHandler         = new ArkEventHandler();
    ArkEventHandler1        arkEventHandler1        = new ArkEventHandler1();
    AbstractArkEventHandler abstractArkEventHandler = new AbstractArkEventHandler();

    static List<String>     result                  = new ArrayList<>();

    @Before
    public void before() {
        super.before();
        eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        eventAdminService.register(arkEventHandler);
        eventAdminService.register(arkEventHandler1);
        eventAdminService.register(abstractArkEventHandler);
    }

    @After
    public void after() {
        result.clear();
        eventAdminService.unRegister(arkEventHandler);
        eventAdminService.unRegister(arkEventHandler1);
        eventAdminService.unRegister(abstractArkEventHandler);
        arkEventHandler = null;
        arkEventHandler1 = null;
        super.after();
    }

    @Test
    public void testEvent() {
        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.0")
            .setBizState(BizState.RESOLVED);
        Plugin plugin = new PluginModel().setPluginName("test-plugin").setVersion("1.0.0");
        eventAdminService.sendEvent(new AfterBizStartupEvent(biz));
        Assert.assertTrue(result.size() == 3);
        Assert.assertTrue(result.contains("AbstractArkEvent->AfterBizStartupEvent"));
        eventAdminService.sendEvent(new BeforePluginStartupEvent(plugin));
        Assert.assertTrue(result.size() == 5);

        // test for ArkEvent.class.isAssignableFrom(event.getClass()
        eventAdminService.sendEvent(new ArkEvent() {
            @Override
            public String getTopic() {
                return "ark-event";
            }
        });
        Assert.assertTrue(result.size() == 7);
    }

    static class ArkEventHandler implements EventHandler {

        @Override
        public void handleEvent(ArkEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class ArkEventHandler1 implements EventHandler<ArkEvent> {

        @Override
        public void handleEvent(ArkEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AbstractArkEventHandler implements EventHandler<AbstractArkEvent> {

        @Override
        public void handleEvent(AbstractArkEvent event) {
            if (event instanceof AfterBizStartupEvent) {
                result.add("AbstractArkEvent->AfterBizStartupEvent");
            }
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

}
