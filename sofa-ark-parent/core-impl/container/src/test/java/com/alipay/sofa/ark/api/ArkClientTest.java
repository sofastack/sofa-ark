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
package com.alipay.sofa.ark.api;

import com.alipay.sofa.ark.container.service.biz.BizFactoryServiceImpl;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.container.service.event.EventAdminServiceImpl;
import com.alipay.sofa.ark.container.service.injection.InjectionServiceImpl;
import com.alipay.sofa.ark.container.service.plugin.PluginManagerServiceImpl;
import com.alipay.sofa.ark.spi.model.BizOperation;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.alipay.sofa.ark.api.ArkClient.*;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_BIZ_URL;
import static com.alipay.sofa.ark.spi.model.BizOperation.OperationType.INSTALL;

public class ArkClientTest {

//    private BizFactoryServiceImpl bizFactoryService = new BizFactoryServiceImpl();
//
//    private BizManagerServiceImpl bizManagerService = new BizManagerServiceImpl();
//
//    private PluginManagerServiceImpl pluginManagerService = new PluginManagerServiceImpl();
//
//    private InjectionServiceImpl injectionService = new InjectionServiceImpl();
//
//    private EventAdminServiceImpl eventAdminService = new EventAdminServiceImpl();
//
//    public void setUp() {
//        setArguments(null);
//        setBizFactoryService(bizFactoryService);
//        setBizManagerService(bizManagerService);
//        setPluginManagerService(pluginManagerService);
//        setInjectionService(injectionService);
//        setEventAdminService(eventAdminService);
//    }
//
//    public void tearDown() {
//        setBizFactoryService(bizFactoryService);
//        setBizManagerService(bizManagerService);
//        setPluginManagerService(pluginManagerService);
//        setInjectionService(injectionService);
//        setEventAdminService(eventAdminService);
//        setArguments(null);
//    }
//
//    @Test
//    public void testInstallOperation() throws Throwable {
//
//        setArguments(new String[] {"a"});
//        BizOperation bizOperation = new BizOperation();
//        bizOperation.setOperationType(INSTALL);
//        Map<String, String> parameters = new HashMap<>();
//        parameters.put(CONFIG_BIZ_URL, "sample-ark-1.0.0-ark-biz.jar");
//
//
//        bizOperation.setParameters(parameters);
//        installOperation(bizOperation);
//
//    }
}
