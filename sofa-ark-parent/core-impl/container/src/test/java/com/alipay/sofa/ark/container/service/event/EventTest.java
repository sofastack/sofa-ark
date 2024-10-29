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
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.event.AfterFinishDeployEvent;
import com.alipay.sofa.ark.spi.event.AfterFinishStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizSwitchEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizSwitchEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupFailedEvent;
import com.alipay.sofa.ark.spi.event.plugin.AfterPluginStartupEvent;
import com.alipay.sofa.ark.spi.event.plugin.AfterPluginStopEvent;
import com.alipay.sofa.ark.spi.event.plugin.BeforePluginStartupEvent;
import com.alipay.sofa.ark.spi.event.plugin.BeforePluginStopEvent;
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
 * test for specified event type
 *
 * @author guolei.sgl (guolei.sgl@antfin.com) 2019/11/5 10:39 AM
 * @since
 **/
public class EventTest extends BaseTest {

    EventAdminService   eventAdminService;

    static List<String> result = new ArrayList<>();

    @Before
    public void before() {
        super.before();
        eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        eventAdminService.register(new AfterBizStartupEventHandler());
        eventAdminService.register(new AfterBizStopEventHandler());
        eventAdminService.register(new AfterBizSwitchEventHandler());
        eventAdminService.register(new BeforeBizStartupEventHandler());
        eventAdminService.register(new BeforeBizStopEventHandler());
        eventAdminService.register(new BeforeBizSwitchEventHandler());
        eventAdminService.register(new AfterPluginStartupEventHandler());
        eventAdminService.register(new BeforePluginStopEventHandler());
        eventAdminService.register(new AfterPluginStopEventHandler());
        eventAdminService.register(new BeforePluginStartupEventHandler());
        eventAdminService.register(new AfterFinishDeployEventHandler());
        eventAdminService.register(new AfterFinishStartupEventHandler());
        eventAdminService.register(new BeforeBizRecycleEventEventHandler());
        eventAdminService.register(new TestArkEventHandler());
        eventAdminService.register(new BizFailedEventHandler());
    }

    @After
    public void after() {
        result.clear();
        super.after();
    }

    @Test
    public void testEvent() {
        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.0")
            .setBizState(BizState.RESOLVED);
        Plugin plugin = new PluginModel().setPluginName("test-plugin").setVersion("1.0.0");
        eventAdminService.sendEvent(new AfterBizStartupEvent(biz));
        Assert.assertTrue(result.get(0).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_START));
        eventAdminService.sendEvent(new BeforeBizStartupEvent(biz));
        Assert.assertTrue(result.get(1).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_START));
        eventAdminService.sendEvent(new BeforeBizStopEvent(biz));
        Assert.assertTrue(result.get(2).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_STOP));
        eventAdminService.sendEvent(new AfterBizStopEvent(biz));
        Assert.assertTrue(result.get(3).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_STOP));
        eventAdminService.sendEvent(new BeforeBizSwitchEvent(biz));
        Assert.assertTrue(result.get(4).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_SWITCH));
        eventAdminService.sendEvent(new AfterBizSwitchEvent(biz));
        Assert.assertTrue(result.get(5).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_SWITCH));
        eventAdminService.sendEvent(new AfterPluginStartupEvent(plugin));
        Assert.assertTrue(result.get(6).equalsIgnoreCase(
            Constants.PLUGIN_EVENT_TOPIC_AFTER_INVOKE_PLUGIN_START));
        eventAdminService.sendEvent(new AfterPluginStopEvent(plugin));
        Assert.assertTrue(result.get(7).equalsIgnoreCase(
            Constants.PLUGIN_EVENT_TOPIC_AFTER_INVOKE_PLUGIN_STOP));

        eventAdminService.sendEvent(new BeforePluginStartupEvent(plugin));
        Assert.assertTrue(result.get(8).equalsIgnoreCase(
            Constants.PLUGIN_EVENT_TOPIC_BEFORE_INVOKE_PLUGIN_START));
        eventAdminService.sendEvent(new BeforePluginStopEvent(plugin));
        Assert.assertTrue(result.get(9).equalsIgnoreCase(
            Constants.PLUGIN_EVENT_TOPIC_BEFORE_INVOKE_PLUGIN_STOP));

        eventAdminService.sendEvent(new AfterFinishDeployEvent());
        Assert.assertTrue(result.get(10).equalsIgnoreCase(
            Constants.ARK_EVENT_TOPIC_AFTER_FINISH_DEPLOY_STAGE));
        eventAdminService.sendEvent(new AfterFinishStartupEvent());
        Assert.assertTrue(result.get(11).equalsIgnoreCase(
            Constants.ARK_EVENT_TOPIC_AFTER_FINISH_STARTUP_STAGE));

        eventAdminService.sendEvent(new TestArkEvent(""));
        Assert.assertTrue(result.get(12).equalsIgnoreCase("test-ark"));

        eventAdminService.sendEvent(new BeforeBizRecycleEvent(biz));
        Assert.assertTrue(result.get(13).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_BEFORE_RECYCLE_BIZ));

        eventAdminService.sendEvent(new AfterBizStartupFailedEvent(biz, new Throwable()));
        Assert.assertTrue(result.get(14).equalsIgnoreCase(
            Constants.BIZ_EVENT_TOPIC_AFTER_BIZ_FAILED));
    }

    static class TestArkEvent extends AbstractArkEvent {

        public TestArkEvent(Object source) {
            super(source);
            this.topic = "test-ark";
        }
    }

    static class TestArkEventHandler implements EventHandler<TestArkEvent> {

        @Override
        public void handleEvent(TestArkEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterBizStartupEventHandler implements EventHandler<AfterBizStartupEvent> {

        @Override
        public void handleEvent(AfterBizStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforeBizRecycleEventEventHandler implements EventHandler<BeforeBizRecycleEvent> {

        @Override
        public void handleEvent(BeforeBizRecycleEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterBizStopEventHandler implements EventHandler<AfterBizStopEvent> {

        @Override
        public void handleEvent(AfterBizStopEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterBizSwitchEventHandler implements EventHandler<AfterBizSwitchEvent> {

        @Override
        public void handleEvent(AfterBizSwitchEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforeBizStartupEventHandler implements EventHandler<BeforeBizStartupEvent> {

        @Override
        public void handleEvent(BeforeBizStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforeBizStopEventHandler implements EventHandler<BeforeBizStopEvent> {

        @Override
        public void handleEvent(BeforeBizStopEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforeBizSwitchEventHandler implements EventHandler<BeforeBizSwitchEvent> {

        @Override
        public void handleEvent(BeforeBizSwitchEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterPluginStartupEventHandler implements EventHandler<AfterPluginStartupEvent> {

        @Override
        public void handleEvent(AfterPluginStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforePluginStopEventHandler implements EventHandler<BeforePluginStopEvent> {

        @Override
        public void handleEvent(BeforePluginStopEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterPluginStopEventHandler implements EventHandler<AfterPluginStopEvent> {

        @Override
        public void handleEvent(AfterPluginStopEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BeforePluginStartupEventHandler implements EventHandler<BeforePluginStartupEvent> {

        @Override
        public void handleEvent(BeforePluginStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterFinishDeployEventHandler implements EventHandler<AfterFinishDeployEvent> {

        @Override
        public void handleEvent(AfterFinishDeployEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class AfterFinishStartupEventHandler implements EventHandler<AfterFinishStartupEvent> {

        @Override
        public void handleEvent(AfterFinishStartupEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }

    static class BizFailedEventHandler implements EventHandler<AfterBizStartupFailedEvent> {

        @Override
        public void handleEvent(AfterBizStartupFailedEvent event) {
            result.add(event.getTopic());
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
