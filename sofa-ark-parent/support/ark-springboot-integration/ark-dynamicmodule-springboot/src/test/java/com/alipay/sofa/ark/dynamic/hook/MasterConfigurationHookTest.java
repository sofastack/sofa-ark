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
package com.alipay.sofa.ark.dynamic.hook;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.dynamic.BaseTest;
import com.alipay.sofa.ark.springboot.util.RootApplicationContextUtils;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author hanyue
 * @version : ConfigurationHookTest.java, v 0.1 2022年05月31日 1:48 PM hanyue Exp $
 */
public class MasterConfigurationHookTest extends BaseTest {

    @Test
    public void test() {
        ApplicationContext applicationContext = RootApplicationContextUtils
            .getApplicationContext(ArkClient.getMasterBiz());
        Assert.assertNotNull(applicationContext);
        Object sofaArkTestBean = applicationContext.getBean("sofaArkTestBean");
        Assert.assertNotNull(sofaArkTestBean);
    }
}