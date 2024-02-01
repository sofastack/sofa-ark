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

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.biz.BizCommandProvider.BizCommand;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizDeployService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.alipay.sofa.ark.api.ArkConfigs.putStringValue;
import static com.alipay.sofa.ark.container.service.biz.BizCommandProvider.HELP_MESSAGE;
import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;
import static com.alipay.sofa.ark.spi.model.BizState.ACTIVATED;
import static com.alipay.sofa.ark.spi.model.BizState.DEACTIVATED;
import static org.junit.Assert.*;

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

        assertFalse(bizCommandProvider.validate("biz"));
        assertFalse(bizCommandProvider.validate("biz -m"));
        assertFalse(bizCommandProvider.validate("biz -d"));
        assertFalse(bizCommandProvider.validate("biz -s"));
        assertFalse(bizCommandProvider.validate("biz -i"));
        assertFalse(bizCommandProvider.validate("biz -u"));
        assertFalse(bizCommandProvider.validate("biz -o"));
        assertTrue(bizCommandProvider.validate("biz -a"));
        assertTrue(bizCommandProvider.validate("biz -h"));

        assertFalse(bizCommandProvider.validate("biz -ah"));
        assertFalse(bizCommandProvider.validate("biz -am A1:V1"));
        assertFalse(bizCommandProvider.validate("biz -hm A1:V1"));
        assertFalse(bizCommandProvider.validate("biz -mi A1:V1"));
        assertFalse(bizCommandProvider.validate("biz -mu A1:V1"));
        assertFalse(bizCommandProvider.validate("biz -mo A1:V1"));
        assertTrue(bizCommandProvider.validate("biz -msd A1:V1"));
        assertTrue(bizCommandProvider.validate("biz -msd A1:V1 A2:V2"));

        assertFalse(bizCommandProvider.validate("biz -io A1:V1"));

        assertFalse(bizCommandProvider.validate("biz -i A1:V1 A2:V2"));
        assertTrue(bizCommandProvider.validate("biz -i A1:V1"));

        assertFalse(bizCommandProvider.validate("biz -u A1:V1 A2:V2"));
        assertTrue(bizCommandProvider.validate("biz -u A1:V1"));

        assertFalse(bizCommandProvider.validate("biz -o A1:V1 A2:V2"));
        assertTrue(bizCommandProvider.validate("biz -o A1:V1"));
    }

    @Test
    public void testBizInfo() {

        String multiBizInfo = bizCommandProvider.handleCommand("biz -m A1:V1 A1:V2");
        String multiOptionBizInfo = bizCommandProvider.handleCommand("biz -md A1:V1 B1:V1");

        assertTrue(multiBizInfo.contains("MainClassA1"));
        assertTrue(multiBizInfo.contains("MainClassA2"));
        assertFalse(multiBizInfo.contains("ClassLoader"));
        assertFalse(multiBizInfo.contains("ClassPath"));
        assertFalse(multiBizInfo.contains("MainClassB1"));

        assertTrue(multiOptionBizInfo.contains("MainClassA1"));
        assertTrue(multiOptionBizInfo.contains("MainClassB1"));
        assertFalse(multiOptionBizInfo.contains("MainClassA2"));
        assertTrue(multiOptionBizInfo.contains("ClassLoader"));
        assertTrue(multiOptionBizInfo.contains("ClassPath"));
    }

    @Test
    public void testInstallBiz() {
        String msg = bizCommandProvider.handleCommand("biz -i C1:V1");
        assertTrue(msg.contains("Exists some biz"));

        ((MockBiz) bizManagerService.getBizByIdentity("A1:V1")).setBizState(ACTIVATED);
        ((MockBiz) bizManagerService.getBizByIdentity("A1:V2")).setBizState(DEACTIVATED);
        ((MockBiz) bizManagerService.getBizByIdentity("B1:V1")).setBizState(ACTIVATED);

        msg = bizCommandProvider.handleCommand("biz -i C1:V1");
        assertTrue(msg.contains("Start to process install command now, pls wait and check."));
    }

    @Test
    public void testSwitchBiz() {

        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1")).setBizState(ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1")).setBizState(ACTIVATED);
        bizCommandProvider.handleCommand("biz -o A1:V2");

        sleep(200);

        assertTrue(bizA1.getBizState().equals(DEACTIVATED));
        assertTrue(bizA2.getBizState().equals(ACTIVATED));
        assertTrue(bizB1.getBizState().equals(ACTIVATED));
    }

    @Test
    public void testUninstallBiz() {

        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1")).setBizState(ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1")).setBizState(ACTIVATED);
        bizCommandProvider.handleCommand("biz -u B1:V1");

        sleep(200);

        assertTrue(bizA1.getBizState().equals(ACTIVATED));
        assertTrue(bizA2.getBizState().equals(DEACTIVATED));
        assertNull(bizManagerService.getBizByIdentity("B1:V1"));
    }

    @Test
    public void testUninstallMasterBiz() {

        putStringValue(MASTER_BIZ, "B1");
        Biz bizA1 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V1")).setBizState(ACTIVATED);
        Biz bizA2 = ((MockBiz) bizManagerService.getBizByIdentity("A1:V2"))
            .setBizState(DEACTIVATED);
        Biz bizB1 = ((MockBiz) bizManagerService.getBizByIdentity("B1:V1")).setBizState(ACTIVATED);
        bizCommandProvider.handleCommand("biz -u B1:V1");

        sleep(200);

        assertTrue(bizA1.getBizState().equals(ACTIVATED));
        assertTrue(bizA2.getBizState().equals(DEACTIVATED));
        assertTrue(bizB1.getBizState().equals(ACTIVATED));
        //        assertNotNull(bizManagerService.getBizByIdentity("B1:V1"));
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

    @Test
    public void testBizCommandInvalidate() throws MalformedURLException {

        BizCommand bizCommand = bizCommandProvider.new BizCommand("");
        assertFalse(bizCommand.isValidate());
        bizCommand = bizCommandProvider.new BizCommand("biz -");
        assertFalse(bizCommand.isValidate());
        bizCommand = bizCommandProvider.new BizCommand("biz -x");
        assertFalse(bizCommand.isValidate());
        bizCommand = bizCommandProvider.new BizCommand("biz -h a");
        assertFalse(bizCommand.isValidate());
        assertTrue(bizCommand.process().startsWith("Error command format"));

        bizCommand = bizCommandProvider.new BizCommand("biz -h");
        assertEquals(HELP_MESSAGE, bizCommand.process());

        mockBiz();
        bizCommand = bizCommandProvider.new BizCommand("biz -a");
        assertEquals("A1:V1:resolved\nA1:V2:resolved\nB1:V1:resolved\nbiz count = 3\n",
            bizCommand.process());
        assertTrue(bizCommand.bizInfo("a:b").startsWith("Invalid bizIdentity: "));

        String bizCommandStr = bizCommand.join(
            new URL[] { new URL("file:\\a"), new URL("file:\\b") }, "&");
        assertTrue(bizCommandStr.equals("\\a&\\b") || bizCommandStr.equals("/a&/b"));
    }
}
