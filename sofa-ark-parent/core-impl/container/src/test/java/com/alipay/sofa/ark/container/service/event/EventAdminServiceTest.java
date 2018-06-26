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
import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class EventAdminServiceTest extends BaseTest {

    private static int mark = 5;

    @Test
    public void test() throws Throwable {
        RegistryService registryService = ArkServiceContainerHolder.getContainer().getService(
            RegistryService.class);
        registryService.publishService(EventHandler.class, new LowPriorityMockEventHandler(),
            "low", new ContainerServiceProvider());
        registryService.publishService(EventHandler.class, new HighPriorityMockEventHandler(),
            "high", new ContainerServiceProvider());

        Biz biz = new BizModel().setBizState(BizState.DEACTIVATED).setBizName("mock name")
            .setBizVersion("mock name");
        biz.stop();
        Assert.assertTrue(mark == 50);
    }

    class HighPriorityMockEventHandler implements EventHandler {

        @Override
        public void handleEvent(ArkEvent event) {
            if (Constants.BIZ_EVENT_TOPIC_UNINSTALL.equals(event.getTopic())) {
                mark *= mark;
            }
        }

        @Override
        public int getPriority() {
            return 10;
        }
    }

    class LowPriorityMockEventHandler implements EventHandler {

        @Override
        public void handleEvent(ArkEvent event) {
            if (Constants.BIZ_EVENT_TOPIC_UNINSTALL.equals(event.getTopic())) {
                mark += mark;
            }
        }

        @Override
        public int getPriority() {
            return 100;
        }
    }
}