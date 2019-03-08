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
package com.alipay.sofa.ark.test.springboot.impl;

import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import com.alipay.sofa.ark.test.springboot.TestValueHolder;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class TestBizEventHandler implements EventHandler {
    @Override
    public void handleEvent(ArkEvent event) {
        if (event.getTopic().equals("test-event-A")) {
            TestValueHolder.setTestValue(10);
        } else if (event.getTopic().equals("test-event-B")) {
            TestValueHolder.setTestValue(20);
        }
    }

    @Override
    public int getPriority() {
        return LOWEST_PRECEDENCE + 20;
    }
}