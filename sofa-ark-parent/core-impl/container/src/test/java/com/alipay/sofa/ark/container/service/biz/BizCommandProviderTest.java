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
package com.alipay.sofa.ark.container.service.biz;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizDeployService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class BizCommandProviderTest extends BaseTest {

    private BizManagerService  bizManagerService;
    private InjectionService   injectionService;
    private BizCommandProvider bizCommandProvider;
    private BizDeployService   bizDeployService;

    @Override
    public void before() {
        super.before();
        bizManagerService = arkServiceContainer.getService(BizManagerService.class);
        injectionService = arkServiceContainer.getService(InjectionService.class);
        bizDeployService = arkServiceContainer.getService(BizDeployService.class);

        mockBiz();

        bizDeployService.deploy(new String[] {});

        bizCommandProvider = new BizCommandProvider();
        injectionService.inject(bizCommandProvider);

        // trigger telnet command thread pool to be created
        new ArkCommandHandler();
    }

    @Test
    public void testBizCommandPattern() {
        Assert.assertFalse(bizCommandProvider.validate("biz"));
        Assert.assertFalse(bizCommandProvider.validate("biz -m"));
        Assert.assertFalse(bizCommandProvider.validate("biz -d"));
        Assert.assertFalse(bizCommandProvider.validate("biz -s"));
        Assert.assertFalse(bizCommandProvider.validate("biz -i"));
        Assert.assertFalse(bizCommandProvider.validate("biz -u"));
        Assert.assertFalse(bizCommandProvider.validate("biz -o"));
        Assert.assertTrue(bizCommandProvider.validate("biz -a"));
        Assert.assertTrue(bizCommandProvider.validate("biz -h"));

        Assert.assertFalse(bizCommandProvider.validate("biz -ah"));
        Assert.assertFalse(bizCommandProvider.validate("biz -am A1:V1"));
        Assert.assertFalse(bizCommandProvider.validate("biz -hm A1:V1"));
        Assert.assertFalse(bizCommandProvider.validate("biz -mi A1:V1"));
        Assert.assertFalse(bizCommandProvider.validate("biz -mu A1:V1"));
        Assert.assertFalse(bizCommandProvider.validate("biz -mo A1:V1"));
        Assert.assertTrue(bizCommandProvider.validate("biz -msd A1:V1"));
        Assert.assertTrue(bizCommandProvider.validate("biz -msd A1:V1 A2:V2"));

        Assert.assertFalse(bizCommandProvider.validate("biz -io A1:V1"));

        Assert.assertFalse(bizCommandProvider.validate("biz -i A1:V1 A2:V2"));
        Assert.assertTrue(bizCommandProvider.validate("biz -i A1:V1"));

        Assert.assertFalse(bizCommandProvider.validate("biz -u A1:V1 A2:V2"));
        Assert.assertTrue(bizCommandProvider.validate("biz -u A1:V1"));

        Assert.assertFalse(bizCommandProvider.validate("biz -o A1:V1 A2:V2"));
        Assert.assertTrue(bizCommandProvider.validate("biz -o A1:V1"));
    }

    @Test
    public void testBizInfo() {
        String multiBizInfo = bizCommandProvider.handleCommand("biz -m A1:V1 A1:V2");
        String multiOptionBizInfo = bizCommandProvider.handleCommand("biz -md A1:V1 B1:V1");

        Assert.assertTrue(multiBizInfo.contains("MainClassA1"));
        Assert.assertTrue(multiBizInfo.contains("MainClassA2"));
        Assert.assertFalse(multiBizInfo.contains("ClassLoader"));
        Assert.assertFalse(multiBizInfo.contains("ClassPath"));
        Assert.assertFalse(multiBizInfo.contains("MainClassB1"));

        Assert.assertTrue(multiOptionBizInfo.contains("MainClassA1"));
        Assert.assertTrue(multiOptionBizInfo.contains("MainClassB1"));
        Assert.assertFalse(multiOptionBizInfo.contains("MainClassA2"));
        Assert.assertTrue(multiOptionBizInfo.contains("ClassLoader"));
        Assert.assertTrue(multiOptionBizInfo.contains("ClassPath"));
    }

    @Test
    public void testInstallBiz() {
        String msg = bizCommandProvider.handleCommand("biz -i C1:V1");
        Assert.assertTrue(msg.contains("Exists some biz"));

        ((MockBiz) bizManagerService.getBizByIdentity("A1:V1")).setBizState(BizState.ACTIVATED);
        ((MockBiz) bizManagerService.getBizByIdentity("A1:V2")).setBizState(BizState.DEACTIVATED);
        ((MockBiz) bizManagerService.getBizByIdentity("B1:V1")).setBizState(BizState.ACTIVATED);

        msg = bizCommandProvider.handleCommand("biz -i C1:V1");
        Assert
            .assertTrue(msg.contains("Start to process install command now, pls wait and check."));
    }

    @Test
    public void testSwitchBiz() {
        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1"))
            .setBizState(BizState.ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(BizState.DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1"))
            .setBizState(BizState.ACTIVATED);
        bizCommandProvider.handleCommand("biz -o A1:V2");

        sleep(200);

        Assert.assertTrue(bizA1.getBizState().equals(BizState.DEACTIVATED));
        Assert.assertTrue(bizA2.getBizState().equals(BizState.ACTIVATED));
        Assert.assertTrue(bizB1.getBizState().equals(BizState.ACTIVATED));
    }

    @Test
    public void testUninstallBiz() {
        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1"))
            .setBizState(BizState.ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(BizState.DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1"))
            .setBizState(BizState.ACTIVATED);
        bizCommandProvider.handleCommand("biz -u B1:V1");

        sleep(200);

        Assert.assertTrue(bizA1.getBizState().equals(BizState.ACTIVATED));
        Assert.assertTrue(bizA2.getBizState().equals(BizState.DEACTIVATED));
        Assert.assertNull(bizManagerService.getBizByIdentity("B1:V1"));
    }

    @Test
    public void testUninstallMasterBiz() {
        ArkConfigs.putStringValue(Constants.MASTER_BIZ, "B1");
        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1"))
            .setBizState(BizState.ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(BizState.DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1"))
            .setBizState(BizState.ACTIVATED);
        bizCommandProvider.handleCommand("biz -u B1:V1");

        sleep(200);

        Assert.assertTrue(bizA1.getBizState().equals(BizState.ACTIVATED));
        Assert.assertTrue(bizA2.getBizState().equals(BizState.DEACTIVATED));
        Assert.assertTrue(bizB1.getBizState().equals(BizState.ACTIVATED));
        Assert.assertNotNull(bizManagerService.getBizByIdentity("B1:V1"));
    }

    private void mockBiz() {
        MockBiz bizA1 = new MockBiz();
        bizA1.setBizName("A1").setBizVersion("V1").setWebContextPath("/A1")
            .setBizState(BizState.RESOLVED).setMainClass("MainClassA1");

        MockBiz bizA2 = new MockBiz();
        bizA2.setBizName("A1").setBizVersion("V2").setWebContextPath("/A2")
            .setBizState(BizState.RESOLVED).setMainClass("MainClassA2");

        MockBiz bizB1 = new MockBiz();
        bizB1.setBizName("B1").setBizVersion("V1").setWebContextPath("/B1")
            .setBizState(BizState.RESOLVED).setMainClass("MainClassB1");

        bizManagerService.registerBiz(bizA1);
        bizManagerService.registerBiz(bizA2);
        bizManagerService.registerBiz(bizB1);
    }

    private void sleep(long mill) {
        try {
            Thread.sleep(mill);
        } catch (Throwable t) {
            // ignore
        }
    }

    class MockBiz extends BizModel {
        @Override
        public void start(String[] args) throws Throwable {
        }

        @Override
        public void stop() {
            // just to mock stop
            Biz biz = bizManagerService.getBiz(this.getBizName(), this.getBizVersion());
            if (biz.getBizState() != BizState.RESOLVED) {
                bizManagerService.unRegisterBiz(this.getBizName(), this.getBizVersion());
            }
        }
    }

}