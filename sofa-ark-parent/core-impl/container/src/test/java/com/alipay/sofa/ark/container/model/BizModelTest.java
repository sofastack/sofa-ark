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
package com.alipay.sofa.ark.container.model;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.event.EventAdminServiceImpl;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.model.BizInfo.BizStateRecord;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.AUTO_UNINSTALL_WHEN_FAILED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.REMOVE_BIZ_INSTANCE_AFTER_STOP_FAILED;
import static org.apache.commons.io.FileUtils.touch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BizModelTest {

    @Test
    public void testDoCheckDeclared() throws MalformedURLException {

        BizModel bizModel = new BizModel();
        assertEquals(new HashSet(), bizModel.setAttribute("a", "b").setAttributes(new HashMap<>())
            .getInjectExportPackages());
        assertEquals(new HashSet(), bizModel.getInjectPluginDependencies());
        bizModel.setCustomBizName("abc");
        assertNotNull(bizModel.getAttributes());
        assertNull(bizModel.getBizTempWorkDir());
        bizModel.toString();

        bizModel.setPluginClassPath(new URL[] { new URL("file://b/a.jar!/") });
        assertTrue(bizModel.doCheckDeclared("file://b/a.jar!/b.jar"));
        assertTrue(bizModel.doCheckDeclared(this.getClass().getClassLoader()
            .getResource("test.jar").getPath()));
    }

    @Test
    public void testBizStateChanged() {
        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz1");
        bizModel.setBizVersion("0.0.1-SNAPSHOT");
        List<BizStateRecord> changeLogs = bizModel.getBizStateRecords();

        assertEquals(0, changeLogs.size());

        // create Biz
        bizModel.setBizState(BizState.RESOLVED);
        bizModel.setClassLoader(this.getClass().getClassLoader());
        assertEquals(1, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));

        // activate Biz
        bizModel.setBizState(BizState.ACTIVATED);
        assertEquals(2, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));

        // deactivate Biz
        bizModel.setBizState(BizState.DEACTIVATED);
        assertEquals(3, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));
        assertTrue(bizModel.toString().contains("-> deactivated"));

        bizModel.setBizState(BizState.UNRESOLVED);
        assertEquals(4, changeLogs.size());
        assertTrue(bizModel.toString().contains("-> resolved"));
        assertTrue(bizModel.toString().contains("-> activated"));
        assertTrue(bizModel.toString().contains("-> deactivated"));
        assertTrue(bizModel.toString().contains("-> unresolved"));
    }

    @Test
    public void testRecycleBizTempWorkDir() throws Throwable {
        assertFalse(BizModel.recycleBizTempWorkDir(null));

        File fileJar = new File("/tmp/" + System.currentTimeMillis() + ".jar");
        touch(fileJar);

        assertTrue(BizModel.recycleBizTempWorkDir(fileJar));
        assertFalse(fileJar.exists());

        File fileDir = new File("/tmp/" + System.currentTimeMillis() + "-test");
        fileDir.mkdir();
        File fileSubFile = new File(fileDir.getAbsolutePath() + "/subfile.jar");
        touch(fileSubFile);

        assertTrue(BizModel.recycleBizTempWorkDir(fileDir));
        assertFalse(fileDir.exists());
        assertFalse(fileSubFile.exists());
    }

    @Test
    public void testStopFailedWithClean() {
        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz1");
        bizModel.setBizVersion("0.0.1-SNAPSHOT");
        bizModel.setBizState(BizState.ACTIVATED);

        try (MockedStatic<ArkServiceContainerHolder> mockedStatic = Mockito.mockStatic(ArkServiceContainerHolder.class)) {
            EventAdminService eventAdminService = mock(EventAdminServiceImpl.class);
            BizManagerService bizManagerService = mock(BizManagerService.class);

            ArkServiceContainer arkServiceContainer = mock(ArkServiceContainer.class);
            doThrow(new RuntimeException()).when(eventAdminService).sendEvent(any(BeforeBizStopEvent.class));
            when(arkServiceContainer.getService(EventAdminService.class)).thenReturn(eventAdminService);
            when(arkServiceContainer.getService(BizManagerService.class)).thenReturn(bizManagerService);
            mockedStatic.when(ArkServiceContainerHolder::getContainer).thenReturn(arkServiceContainer);
            try {
                bizModel.stop();
            } catch (RuntimeException e) {
            }
            assertEquals(BizState.STOPPED, bizModel.getBizState());
        }

        bizModel.setBizState(BizState.ACTIVATED);
        try (MockedStatic<ArkServiceContainerHolder> mockedStatic = Mockito.mockStatic(ArkServiceContainerHolder.class)) {
            ArkConfigs.putStringValue(REMOVE_BIZ_INSTANCE_AFTER_STOP_FAILED, "false");
            EventAdminService eventAdminService = mock(EventAdminServiceImpl.class);
            BizManagerService bizManagerService = mock(BizManagerService.class);

            ArkServiceContainer arkServiceContainer = mock(ArkServiceContainer.class);
            doThrow(new RuntimeException()).when(eventAdminService).sendEvent(any(BeforeBizStopEvent.class));
            when(arkServiceContainer.getService(EventAdminService.class)).thenReturn(eventAdminService);
            when(arkServiceContainer.getService(BizManagerService.class)).thenReturn(bizManagerService);
            mockedStatic.when(ArkServiceContainerHolder::getContainer).thenReturn(arkServiceContainer);
            try {
                bizModel.stop();
            } catch (RuntimeException e) {
            }
            assertEquals(BizState.BROKEN, bizModel.getBizState());
        } finally {
            ArkConfigs.putStringValue(REMOVE_BIZ_INSTANCE_AFTER_STOP_FAILED, "true");
        }
    }

    @Test
    public void testStopSucceedWithClean() {
        BizModel bizModel = new BizModel();
        bizModel.setBizName("biz1");
        bizModel.setBizVersion("0.0.1-SNAPSHOT");
        bizModel.setBizState(BizState.ACTIVATED);

        try (MockedStatic<ArkServiceContainerHolder> mockedStatic = Mockito.mockStatic(ArkServiceContainerHolder.class)) {
            EventAdminService eventAdminService = mock(EventAdminService.class);
            BizManagerService bizManagerService = mock(BizManagerService.class);

            ArkServiceContainer arkServiceContainer = mock(ArkServiceContainer.class);
            when(arkServiceContainer.getService(EventAdminService.class)).thenReturn(eventAdminService);
            when(arkServiceContainer.getService(BizManagerService.class)).thenReturn(bizManagerService);
            mockedStatic.when(ArkServiceContainerHolder::getContainer).thenReturn(arkServiceContainer);
            bizModel.stop();

            assertEquals(BizState.STOPPED, bizModel.getBizState());
        }

        bizModel.setBizState(BizState.ACTIVATED);
        try (MockedStatic<ArkServiceContainerHolder> mockedStatic = Mockito.mockStatic(ArkServiceContainerHolder.class)) {
            ArkConfigs.putStringValue(REMOVE_BIZ_INSTANCE_AFTER_STOP_FAILED, "false");
            EventAdminService eventAdminService = mock(EventAdminService.class);
            BizManagerService bizManagerService = mock(BizManagerService.class);

            ArkServiceContainer arkServiceContainer = mock(ArkServiceContainer.class);
            when(arkServiceContainer.getService(EventAdminService.class)).thenReturn(eventAdminService);
            when(arkServiceContainer.getService(BizManagerService.class)).thenReturn(bizManagerService);
            mockedStatic.when(ArkServiceContainerHolder::getContainer).thenReturn(arkServiceContainer);
            bizModel.stop();

            assertEquals(BizState.STOPPED, bizModel.getBizState());
        } finally {
            ArkConfigs.putStringValue(AUTO_UNINSTALL_WHEN_FAILED_ENABLE, "true");
        }
    }
}
