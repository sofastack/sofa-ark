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
package com.alipay.sofa.ark.container.service.retrieval;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yanzhu
 * @date 2023/10/17 17:55
 */
public class InfoQueryCommandProviderTest extends BaseTest {

    private InjectionService         injectionService;
    private InfoQueryCommandProvider queryCommandProvider;

    @Override
    public void before() {
        super.before();

        injectionService = arkServiceContainer.getService(InjectionService.class);

        queryCommandProvider = new InfoQueryCommandProvider();
        injectionService.inject(queryCommandProvider);

        // trigger telnet command thread pool to be created
        new ArkCommandHandler();
    }

    @Test
    public void testInfoQueryCommandPattern() {
        Assert.assertFalse(queryCommandProvider.validate("ck"));

        Assert.assertTrue(queryCommandProvider.validate("ck -h"));
        Assert.assertFalse(queryCommandProvider.validate("ck -c"));
        Assert.assertFalse(queryCommandProvider.validate("ck -ch"));

        Assert.assertTrue(queryCommandProvider.validate("ck -c com.example.HelloWorld"));
    }

    @Test
    public void testClassInfo() {
        String classInfo = queryCommandProvider
            .handleCommand("ck -c com.alipay.sofa.ark.container.session.handler.ArkCommandHandler");

        Assert.assertTrue(classInfo.contains("class-info"));
        Assert.assertTrue(classInfo.contains("code-source"));
        Assert.assertTrue(classInfo.contains("container-name"));
        Assert.assertTrue(classInfo.contains("class-loader"));
    }

}
