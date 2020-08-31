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
package com.alipay.sofa.ark.container.service.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ArkClientTest extends BaseTest {

    // bizName=biz-demo, bizVersion=1.0.0
    private URL bizUrl1;
    // bizName=biz-demo, bizVersion=2.0.0
    private URL bizUrl2;
    // bizName=biz-demo, bizVersion=3.0.0
    private URL bizUrl3;

    @Before
    public void before() {
        super.before();
        // bizName=biz-demo, bizVersion=1.0.0
        bizUrl1 = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=2.0.0
        bizUrl2 = this.getClass().getClassLoader().getResource("sample-ark-2.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=3.0.0
        bizUrl3 = this.getClass().getClassLoader().getResource("sample-ark-3.0.0-ark-biz.jar");
    }

    @Test
    public void testInstallBiz() throws Throwable {
        ClientResponse response = ArkClient.checkBiz();
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(0, response.getBizInfos().size());

        // test install
        File bizFile = ArkClient.createBizSaveFile("biz-demo", "1.0.0");
        FileUtils.copyInputStreamToFile(bizUrl1.openStream(), bizFile);
        response = ArkClient.installBiz(bizFile);

        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        BizInfo bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.ACTIVATED, bizInfo.getBizState());

        // test install biz with same bizName and bizVersion
        // test install
        File bizFile1 = ArkClient.createBizSaveFile("biz-demo", "1.0.0");
        FileUtils.copyInputStreamToFile(bizUrl1.openStream(), bizFile1);
        response = ArkClient.installBiz(bizFile1);
        Assert.assertEquals(ResponseCode.REPEAT_BIZ, response.getCode());

        // test install biz with same bizName and different bizVersion
        //        response = ArkClient.installBiz(new File(bizUrl2.getFile()));
        File bizFile2 = ArkClient.createBizSaveFile("biz-demo", "2.0.0");
        FileUtils.copyInputStreamToFile(bizUrl2.openStream(), bizFile2);
        response = ArkClient.installBiz(bizFile2);
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.DEACTIVATED, bizInfo.getBizState());
    }

    @Test
    public void testBizArguments() throws Throwable {
        EventAdminService eventAdminService = arkServiceContainer
            .getService(EventAdminService.class);
        List<String> topicList = new ArrayList<>();
        EventHandler eventHandler = new EventHandler<ArkEvent>() {
            @Override
            public void handleEvent(ArkEvent event) {
                topicList.add(event.getTopic());
            }

            @Override
            public int getPriority() {
                return 0;
            }
        };
        eventAdminService.register(eventHandler);

        File bizFile3 = ArkClient.createBizSaveFile("biz-demo", "3.0.0");
        FileUtils.copyInputStreamToFile(bizUrl3.openStream(), bizFile3);
        ArkClient.installBiz(bizFile3);
        //        ArkClient.installBiz(new File(bizUrl3.getFile()));
        ArkClient.uninstallBiz("biz-demo", "3.0.0");

        File bizFile33 = ArkClient.createBizSaveFile("biz-demo", "3.0.0");
        FileUtils.copyInputStreamToFile(bizUrl3.openStream(), bizFile33);
        ArkClient.installBiz(bizFile33, new String[] { "demo" });
        ArkClient.uninstallBiz("biz-demo", "3.0.0");
        Assert.assertEquals("BEFORE-INVOKE-BIZ-START", topicList.get(0));
        Assert.assertEquals("No arguments", topicList.get(1));
        Assert.assertEquals("AFTER-INVOKE-BIZ-START", topicList.get(2));
        // new event
        Assert.assertEquals("BEFORE-RECYCLE-BIZ", topicList.get(4));
        Assert.assertEquals("demo", topicList.get(7));
    }

    @Test
    public void testCheckBiz() throws Throwable {
        testInstallBiz();
        // test check all biz
        ClientResponse response = ArkClient.checkBiz();
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(2, response.getBizInfos().size());

        // test check specified bizName
        response = ArkClient.checkBiz("biz-demo");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(2, response.getBizInfos().size());

        // test check specified bizName and version
        response = ArkClient.checkBiz("biz-demo", "2.0.0");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(1, response.getBizInfos().size());
        response = ArkClient.checkBiz("biz-demo", "3.0.0");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(0, response.getBizInfos().size());
    }

    @Test
    public void testUninstallBiz() throws Throwable {
        testCheckBiz();
        // test uninstall biz
        ClientResponse response = ArkClient.uninstallBiz("biz-demo", "1.0.0");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());

        // test check all biz
        response = ArkClient.checkBiz();
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(1, response.getBizInfos().size());
    }

    public void testSwitchBiz() throws Throwable {
        testUninstallBiz();
        // test switch biz
        ClientResponse response = ArkClient.installBiz(new File(bizUrl1.getFile()));
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        BizInfo bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.ACTIVATED, bizInfo.getBizState());

        response = ArkClient.checkBiz("biz-demo", "2.0.0");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());
        Assert.assertEquals(1, response.getBizInfos().size());
        bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.DEACTIVATED, bizInfo.getBizState());

        response = ArkClient.switchBiz("biz-demo", "2.0.0");
        Assert.assertEquals(ResponseCode.SUCCESS, response.getCode());

        response = ArkClient.switchBiz("biz-demo", "3.0.0");
        Assert.assertEquals(ResponseCode.NOT_FOUND_BIZ, response.getCode());

        response = ArkClient.checkBiz("biz-demo", "2.0.0");
        bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.ACTIVATED, bizInfo.getBizState());
        response = ArkClient.checkBiz("biz-demo", "1.0.0");
        bizInfo = response.getBizInfos().iterator().next();
        Assert.assertEquals(BizState.DEACTIVATED, bizInfo.getBizState());

        // Uninstall biz
        ArkClient.uninstallBiz("biz-demo", "1.0.0");
        ArkClient.uninstallBiz("biz-demo", "2.0.0");
    }

}