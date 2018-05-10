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
package com.alipay.sofa.ark.container.testdata.activator;

import com.alipay.sofa.ark.container.testdata.ITest;
import com.alipay.sofa.ark.container.testdata.impl.TestObjectB;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PluginActivatorB implements PluginActivator {

    @Override
    public void start(PluginContext context) throws ArkException {
        context.publishService(ITest.class, new TestObjectB());
    }

    @Override
    public void stop(PluginContext context) throws ArkException {

    }
}