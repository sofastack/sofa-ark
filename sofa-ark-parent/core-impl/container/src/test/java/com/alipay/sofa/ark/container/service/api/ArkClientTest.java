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
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.replay.Replay;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.event.EventHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.alipay.sofa.ark.api.ArkClient.checkBiz;
import static com.alipay.sofa.ark.api.ArkClient.checkOperation;
import static com.alipay.sofa.ark.api.ArkClient.createBizSaveFile;
import static com.alipay.sofa.ark.api.ArkClient.getArguments;
import static com.alipay.sofa.ark.api.ArkClient.getBizFactoryService;
import static com.alipay.sofa.ark.api.ArkClient.getBizManagerService;
import static com.alipay.sofa.ark.api.ArkClient.getPluginManagerService;
import static com.alipay.sofa.ark.api.ArkClient.installBiz;
import static com.alipay.sofa.ark.api.ArkClient.installOperation;
import static com.alipay.sofa.ark.api.ArkClient.invocationReplay;
import static com.alipay.sofa.ark.api.ArkClient.setBizFactoryService;
import static com.alipay.sofa.ark.api.ArkClient.setBizManagerService;
import static com.alipay.sofa.ark.api.ArkClient.switchOperation;
import static com.alipay.sofa.ark.api.ArkClient.uninstallBiz;
import static com.alipay.sofa.ark.api.ArkClient.uninstallOperation;
import static com.alipay.sofa.ark.api.ResponseCode.REPEAT_BIZ;
import static com.alipay.sofa.ark.api.ResponseCode.SUCCESS;
import static com.alipay.sofa.ark.common.util.FileUtils.copyInputStreamToFile;
import static com.alipay.sofa.ark.spi.constant.Constants.ACTIVATE_NEW_MODULE;
import static com.alipay.sofa.ark.spi.constant.Constants.AUTO_UNINSTALL_WHEN_FAILED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_BIZ_URL;
import static com.alipay.sofa.ark.spi.constant.Constants.EMBED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.ACTIVATE_MULTI_BIZ_VERSION_ENABLE;
import static com.alipay.sofa.ark.spi.model.BizOperation.OperationType.CHECK;
import static com.alipay.sofa.ark.spi.model.BizOperation.OperationType.INSTALL;
import static com.alipay.sofa.ark.spi.model.BizOperation.OperationType.SWITCH;
import static com.alipay.sofa.ark.spi.model.BizOperation.OperationType.UNINSTALL;
import static com.alipay.sofa.ark.spi.model.BizState.ACTIVATED;
import static com.alipay.sofa.ark.spi.model.BizState.DEACTIVATED;
import static com.alipay.sofa.ark.spi.model.BizState.RESOLVED;
import static java.lang.System.setProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    // bizName=biz-demo, bizVersion=4.0.0
    private URL bizUrl4;
    // bizName=biz-demo, bizVersion=5.0.0
    private URL bizUrl5;

    @Before
    public void before() {
        super.before();
        // bizName=biz-demo, bizVersion=1.0.0
        bizUrl1 = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=2.0.0
        bizUrl2 = this.getClass().getClassLoader().getResource("sample-ark-2.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=3.0.0
        bizUrl3 = this.getClass().getClassLoader().getResource("sample-ark-3.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=4.0.0
        bizUrl4 = this.getClass().getClassLoader().getResource("sample-ark-4.0.0-ark-biz.jar");
        // bizName=biz-demo, bizVersion=5.0.0
        bizUrl5 = this.getClass().getClassLoader().getResource("sample-ark-5.0.0-ark-biz.jar");
    }

    @Test
    public void testCreateBizSaveFile() {
        File bizFile = createBizSaveFile("test-biz-demo", "1.0.0", "suffix");
        assertTrue(bizFile.getName().contains("test-biz-demo-1.0.0-suffix"));
    }

    @Test
    public void testInstallBiz() throws Throwable {

        ClientResponse response = checkBiz();
        assertEquals(SUCCESS, response.getCode());
        assertEquals(0, response.getBizInfos().size());

        // test install
        File bizFile = createBizSaveFile("biz-demo", "1.0.0");
        copyInputStreamToFile(bizUrl1.openStream(), bizFile);
        response = installBiz(bizFile);

        assertEquals(SUCCESS, response.getCode());
        BizInfo bizInfo = response.getBizInfos().iterator().next();
        assertEquals(ACTIVATED, bizInfo.getBizState());

        // test install biz with same bizName and bizVersion
        // test install
        File bizFile1 = createBizSaveFile("biz-demo", "1.0.0");
        copyInputStreamToFile(bizUrl1.openStream(), bizFile1);
        response = installBiz(bizFile1);
        assertEquals(REPEAT_BIZ, response.getCode());

        // test install biz with same bizName and different bizVersion
        //        response = ArkClient.installBiz(new File(bizUrl2.getFile()));
        File bizFile2 = createBizSaveFile("biz-demo", "2.0.0");
        copyInputStreamToFile(bizUrl2.openStream(), bizFile2);
        response = installBiz(bizFile2);
        assertEquals(SUCCESS, response.getCode());
        bizInfo = response.getBizInfos().iterator().next();
        assertEquals(DEACTIVATED, bizInfo.getBizState());

        // test install biz with same bizName and different bizVersion and active latest
        setProperty(ACTIVATE_NEW_MODULE, "true");
        setProperty(EMBED_ENABLE, "true");
        File bizFile3 = createBizSaveFile("biz-demo", "3.0.0");
        copyInputStreamToFile(bizUrl3.openStream(), bizFile3);
        response = installBiz(bizFile3);
        setProperty(ACTIVATE_NEW_MODULE, "");
        setProperty(EMBED_ENABLE, "");
        assertEquals(SUCCESS, response.getCode());
        bizInfo = response.getBizInfos().iterator().next();
        assertEquals(ACTIVATED, bizInfo.getBizState());

        // test install biz with same bizName and different bizVersion and keep old module state
        setProperty(ACTIVATE_MULTI_BIZ_VERSION_ENABLE, "true");
        File bizFile4 = createBizSaveFile("biz-demo", "4.0.0");
        copyInputStreamToFile(bizUrl4.openStream(), bizFile4);
        response = installBiz(bizFile4);
        assertEquals(SUCCESS, response.getCode());
        BizManagerService bizManagerService = arkServiceContainer
            .getService(BizManagerService.class);
        assertSame(bizManagerService.getBiz("biz-demo", "3.0.0").getBizState(), ACTIVATED);
        setProperty(ACTIVATE_MULTI_BIZ_VERSION_ENABLE, "");
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

        File bizFile3 = createBizSaveFile("biz-demo", "3.0.0");
        copyInputStreamToFile(bizUrl3.openStream(), bizFile3);
        installBiz(bizFile3);
        //        ArkClient.installBiz(new File(bizUrl3.getFile()));
        uninstallBiz("biz-demo", "3.0.0");

        File bizFile33 = createBizSaveFile("biz-demo", "3.0.0");
        copyInputStreamToFile(bizUrl3.openStream(), bizFile33);
        installBiz(bizFile33, new String[] { "demo" });
        uninstallBiz("biz-demo", "3.0.0");
        assertEquals("BEFORE-INVOKE-BIZ-START", topicList.get(0));
        assertEquals("No arguments", topicList.get(1));
        assertEquals("AFTER-INVOKE-BIZ-START", topicList.get(2));
        // new event
        assertEquals("BEFORE-RECYCLE-BIZ", topicList.get(4));
        assertEquals("demo", topicList.get(7));
    }

    @Test
    public void testCheckBiz() throws Throwable {

        testInstallBiz();
        // test check all biz
        ClientResponse response = checkBiz();
        assertEquals(SUCCESS, response.getCode());
        assertEquals(4, response.getBizInfos().size());

        // test check specified bizName
        response = checkBiz("biz-demo");
        assertEquals(SUCCESS, response.getCode());
        assertEquals(4, response.getBizInfos().size());

        // test check specified bizName and version
        response = checkBiz("biz-demo", "2.0.0");
        assertEquals(SUCCESS, response.getCode());
        assertEquals(1, response.getBizInfos().size());
        response = checkBiz("biz-demo", "3.0.0");
        assertEquals(SUCCESS, response.getCode());
        assertEquals(1, response.getBizInfos().size());

        response = checkBiz("biz-demo", "4.0.0");
        assertEquals(SUCCESS, response.getCode());
        assertEquals(1, response.getBizInfos().size());

        response = checkBiz("biz-demo", "5.0.0");
        assertEquals(SUCCESS, response.getCode());
        assertEquals(0, response.getBizInfos().size());
    }

    @Test
    public void testUninstallBiz() throws Throwable {

        testCheckBiz();
        // test uninstall biz
        ClientResponse response = uninstallBiz("biz-demo", "1.0.0");
        assertEquals(SUCCESS, response.getCode());

        // test check all biz
        response = checkBiz();
        assertEquals(SUCCESS, response.getCode());
        assertEquals(3, response.getBizInfos().size());
    }

    @Test
    public void testUninstallBizWhenIncludeLib() throws Throwable {

        testCheckBiz();
        // test uninstall biz
        ClientResponse response = uninstallBiz("biz-demo", "3.0.0");
        assertEquals(SUCCESS, response.getCode());

        // test check all biz
        response = checkBiz();
        assertEquals(SUCCESS, response.getCode());
        assertEquals(3, response.getBizInfos().size());
    }

    @Test
    public void testInstallBizWithThrowable() throws Throwable {

        File bizFile = createBizSaveFile("biz-demo", "1.0.0");
        copyInputStreamToFile(bizUrl1.openStream(), bizFile);
        BizFactoryService bizFactoryService = getBizFactoryService();
        BizFactoryService bizFactoryServiceMock = mock(BizFactoryService.class);
        setBizFactoryService(bizFactoryServiceMock);
        Biz biz = mock(Biz.class);
        doThrow(new IllegalArgumentException()).when(biz).start(any());
        when(bizFactoryServiceMock.createBiz((File) any())).thenReturn(biz);

        try {
            installBiz(bizFile, null);
            assertTrue(false);
        } catch (Throwable e) {
            setBizFactoryService(bizFactoryService);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        assertNotNull(getBizManagerService());
        assertNotNull(getBizFactoryService());
        assertNotNull(getPluginManagerService());
        assertNotNull(getArguments());
    }

    @Test
    public void testInstallOperation() throws Throwable {

        BizOperation bizOperation = new BizOperation();
        bizOperation.setOperationType(INSTALL);
        bizOperation.getParameters().put(CONFIG_BIZ_URL, bizUrl1.toString());
        bizOperation.setBizName("biz-demo");
        bizOperation.setBizVersion("1.0.0");

        ClientResponse response = installOperation(bizOperation, new String[] {});
        assertEquals(SUCCESS, response.getCode());
    }

    @Test
    public void testInstallOperationWithDynamicMainClass() throws Throwable {

        // the biz module will start with dynamic mainClass specified in env parameters, which is org.example.Main2
        BizOperation bizOperation = new BizOperation();
        bizOperation.setOperationType(INSTALL);
        bizOperation.getParameters().put(CONFIG_BIZ_URL, bizUrl5.toString());
        bizOperation.setBizName("biz-demo");
        bizOperation.setBizVersion("5.0.0");

        Map<String, String> envs = Collections.singletonMap(Constants.BIZ_MAIN_CLASS,
            "org.example.Main2");

        ClientResponse response2 = installOperation(bizOperation, new String[] {}, envs);
        assertEquals(SUCCESS, response2.getCode());
        assertEquals("org.example.Main2", (new ArrayList<>(response2.getBizInfos())).get(0)
            .getMainClass());

        // but in fact, the biz module was packaged with mainClass as org.example.Main1
        URL url = new URL(bizOperation.getParameters().get(Constants.CONFIG_BIZ_URL));
        File file = ArkClient.createBizSaveFile(bizOperation.getBizName(),
            bizOperation.getBizVersion());
        try (InputStream inputStream = url.openStream()) {
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
        JarFile bizFile = new JarFile(file);
        JarFileArchive jarFileArchive = new JarFileArchive(bizFile);
        BizArchive bizArchive = new JarBizArchive(jarFileArchive);
        assertEquals("org.example.Main1",
            bizArchive.getManifest().getMainAttributes().getValue(Constants.MAIN_CLASS_ATTRIBUTE));
        assertEquals("org.example.Main1",
            bizArchive.getManifest().getMainAttributes().getValue(Constants.START_CLASS_ATTRIBUTE));
    }

    @Test
    public void testInstallBizFailed() throws Throwable {
        File bizFile = createBizSaveFile("biz-install-failed-demo", "1.0.0");
        copyInputStreamToFile(bizUrl1.openStream(), bizFile);
        BizFactoryService bizFactoryService = getBizFactoryService();
        BizFactoryService bizFactoryServiceMock = mock(BizFactoryService.class);
        BizManagerService bizManagerService = getBizManagerService();
        BizManagerServiceImpl bizManagerServiceMock = new BizManagerServiceImpl();

        Biz biz = mock(Biz.class);
        when(biz.getIdentity()).thenReturn("biz-install-failed-demo:1.0.0");
        when(biz.getBizState()).thenReturn(RESOLVED);
        when(biz.getBizName()).thenReturn("biz-install-failed-demo");
        when(biz.getBizVersion()).thenReturn("1.0.0");
        doThrow(new IllegalArgumentException()).when(biz).start(any(), any());
        when(bizFactoryServiceMock.createBiz((File) any())).thenReturn(biz);

        // case1: not set AUTO_UNINSTALL_ENABLE
        try {
            setBizFactoryService(bizFactoryServiceMock);
            setBizManagerService(bizManagerServiceMock);
            doThrow(new Exception()).when(biz).stop();

            installBiz(bizFile, null);
            fail();
        } catch (Throwable e) {
            assertFalse(bizManagerServiceMock.getBiz("biz-install-failed-demo").isEmpty());
        } finally {
            setBizFactoryService(bizFactoryService);
            setBizManagerService(bizManagerService);
        }

        // case2: set AUTO_UNINSTALL_ENABLE=false
        try {
            ArkConfigs.putStringValue(AUTO_UNINSTALL_WHEN_FAILED_ENABLE, "false");
            setBizFactoryService(bizFactoryServiceMock);
            setBizManagerService(bizManagerServiceMock);

            installBiz(bizFile, null);
            fail();
        } catch (Throwable e) {
            assertFalse(bizManagerServiceMock.getBiz("biz-install-failed-demo").isEmpty());
            setBizFactoryService(bizFactoryService);
            setBizManagerService(bizManagerService);
        } finally {
            ArkConfigs.putStringValue(AUTO_UNINSTALL_WHEN_FAILED_ENABLE, "true");
        }
    }

    @Test
    public void testUninstallOperation() throws Throwable {

        BizOperation bizOperation = new BizOperation();
        bizOperation.setOperationType(INSTALL);
        bizOperation.getParameters().put(CONFIG_BIZ_URL, bizUrl1.toString());
        bizOperation.setBizName("biz-demo");
        bizOperation.setBizVersion("1.0.0");
        installOperation(bizOperation, new String[] {});

        bizOperation.setOperationType(UNINSTALL);
        ClientResponse response = uninstallOperation(bizOperation);
        assertEquals(SUCCESS, response.getCode());
    }

    @Test
    public void testSwitchOperation() throws Throwable {

        BizOperation bizOperation = new BizOperation();
        bizOperation.setOperationType(INSTALL);
        bizOperation.getParameters().put(CONFIG_BIZ_URL, bizUrl1.toString());
        bizOperation.setBizName("biz-demo");
        bizOperation.setBizVersion("1.0.0");
        installOperation(bizOperation, new String[] {});

        bizOperation.setOperationType(SWITCH);
        ClientResponse response = switchOperation(bizOperation);
        assertEquals(SUCCESS, response.getCode());
    }

    @Test
    public void testCheckOperation() throws Throwable {

        BizOperation bizOperation = new BizOperation();
        bizOperation.setOperationType(INSTALL);
        bizOperation.getParameters().put(CONFIG_BIZ_URL, bizUrl1.toString());
        bizOperation.setBizName("biz-demo");
        bizOperation.setBizVersion("1.0.0");
        installOperation(bizOperation, new String[] {});

        bizOperation.setOperationType(CHECK);
        ClientResponse response = checkOperation(bizOperation);
        assertEquals(SUCCESS, response.getCode());

        bizOperation.setBizVersion("2.0.0");
        response = checkOperation(bizOperation);
        assertEquals(SUCCESS, response.getCode());
    }

    @Test
    public void testInvocationReplay() throws Throwable {
        assertEquals("1", invocationReplay("1.0.0", new Replay() {
            @Override
            public Object invoke() {
                return "1";
            }
        }));
    }
}
