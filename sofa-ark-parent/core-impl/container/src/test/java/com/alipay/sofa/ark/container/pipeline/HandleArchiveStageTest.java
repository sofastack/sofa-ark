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
package com.alipay.sofa.ark.container.pipeline;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.loader.DirectoryBizArchive;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.api.ArkConfigs.getStringValue;
import static com.alipay.sofa.ark.api.ArkConfigs.setSystemProperty;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_SERVER_ADDRESS;
import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class HandleArchiveStageTest {

    private String               originalConfigServerAddress;

    private String               originalMasterBiz;

    private BizFactoryService    bizFactoryService    = mock(BizFactoryService.class);

    private BizManagerService    bizManagerService    = mock(BizManagerService.class);

    private PluginFactoryService pluginFactoryService = mock(PluginFactoryService.class);

    private PluginManagerService pluginManagerService = mock(PluginManagerService.class);

    private PipelineContext      pipelineContext      = mock(PipelineContext.class);

    private ExecutableArchive    executableArchive    = mock(ExecutableArchive.class);

    private HandleArchiveStage   handleArchiveStage   = new HandleArchiveStage();

    @Before
    public void before() throws Exception {

        originalConfigServerAddress = getStringValue(CONFIG_SERVER_ADDRESS);
        originalMasterBiz = getStringValue(MASTER_BIZ);
        when(pipelineContext.getExecutableArchive()).thenReturn(executableArchive);

        Field field = HandleArchiveStage.class.getDeclaredField("bizFactoryService");
        field.setAccessible(true);
        field.set(handleArchiveStage, bizFactoryService);

        field = HandleArchiveStage.class.getDeclaredField("bizManagerService");
        field.setAccessible(true);
        field.set(handleArchiveStage, bizManagerService);

        field = HandleArchiveStage.class.getDeclaredField("pluginFactoryService");
        field.setAccessible(true);
        field.set(handleArchiveStage, pluginFactoryService);

        field = HandleArchiveStage.class.getDeclaredField("pluginManagerService");
        field.setAccessible(true);
        field.set(handleArchiveStage, pluginManagerService);
    }

    @After
    public void after() {
        setSystemProperty(CONFIG_SERVER_ADDRESS,
            originalConfigServerAddress != null ? originalConfigServerAddress : "");
        setSystemProperty(MASTER_BIZ, originalMasterBiz != null ? originalMasterBiz : "'");
        clearInvocations(bizFactoryService, bizManagerService, pluginFactoryService,
            pluginManagerService);
    }

    @Test
    public void testProcess() throws Exception {

        Manifest manifest = mock(Manifest.class);
        BizArchive bizArchive1 = mock(DirectoryBizArchive.class);
        when(bizArchive1.getManifest()).thenReturn(manifest);
        BizArchive bizArchive2 = mock(JarBizArchive.class);
        when(bizArchive2.getManifest()).thenReturn(manifest);
        when(manifest.getMainAttributes()).thenReturn(mock(Attributes.class));
        BizArchive bizArchive3 = mock(BizArchive.class);
        when(bizArchive3.getManifest()).thenReturn(manifest);
        BizArchive bizArchive4 = mock(BizArchive.class);
        when(bizArchive4.getManifest()).thenReturn(manifest);
        when(executableArchive.getBizArchives()).thenReturn(
            asList(bizArchive1, bizArchive2, bizArchive3, bizArchive4));

        BizModel bizModel1 = new BizModel();
        bizModel1.setBizName("a");
        when(bizFactoryService.createBiz(bizArchive1)).thenReturn(bizModel1);
        when(bizFactoryService.createBiz(bizArchive2)).thenReturn(bizModel1);
        when(bizFactoryService.createBiz(bizArchive3)).thenReturn(bizModel1);
        BizModel bizModel2 = new BizModel();
        bizModel2.setBizName("b");
        when(bizFactoryService.createBiz(bizArchive4)).thenReturn(bizModel2);

        PluginArchive pluginArchive = mock(PluginArchive.class);
        when(executableArchive.getPluginArchives()).thenReturn(asList(pluginArchive));
        when(bizManagerService.getBizInOrder()).thenReturn(asList(bizModel1));

        Plugin plugin = mock(Plugin.class);
        when(pluginFactoryService.createPlugin(pluginArchive, null, new HashSet<>())).thenReturn(
            plugin);

        setSystemProperty(CONFIG_SERVER_ADDRESS, "localhost");
        setSystemProperty(MASTER_BIZ, "a");
        handleArchiveStage.process(pipelineContext);

        verify(bizFactoryService, times(4)).createBiz((BizArchive) any());
        verify(bizManagerService, times(3)).registerBiz(any());
        verify(bizManagerService, times(1)).getBizInOrder();
        verify(pluginFactoryService, times(1)).createPlugin(pluginArchive, null, new HashSet<>());
        verify(pluginManagerService, times(1)).registerPlugin(plugin);

        when(executableArchive.getBizArchives()).thenReturn(asList(bizArchive3));
        setSystemProperty(CONFIG_SERVER_ADDRESS, "");
        setSystemProperty(MASTER_BIZ, "");
        handleArchiveStage.process(pipelineContext);

        verify(bizFactoryService, times(5)).createBiz((BizArchive) any());
        verify(bizManagerService, times(4)).registerBiz(any());
        verify(bizManagerService, times(2)).getBizInOrder();
    }

    @Test
    public void testProcessStaticBizFromClasspath() throws Exception {

        BizArchive bizArchive = mock(BizArchive.class);
        when(executableArchive.getBizArchives()).thenReturn(asList(bizArchive));

        handleArchiveStage.processStaticBizFromClasspath(pipelineContext);
        verify(bizFactoryService, times(1)).createBiz((bizArchive));
        verify(bizManagerService, times(1)).registerBiz(null);
    }

    @Test
    public void testProcessEmbed() throws Exception {

        PluginArchive pluginArchive = mock(PluginArchive.class);
        when(executableArchive.getPluginArchives()).thenReturn(asList(pluginArchive));

        Plugin plugin = mock(Plugin.class);
        when(
            pluginFactoryService.createEmbedPlugin(pluginArchive, pipelineContext.getClass()
                .getClassLoader())).thenReturn(plugin);

        BizModel bizModel = new BizModel();
        bizModel.setBizName("a");
        when(bizFactoryService.createEmbedMasterBiz(pipelineContext.getClass().getClassLoader()))
            .thenReturn(bizModel);

        handleArchiveStage.processEmbed(pipelineContext);
        verify(pluginFactoryService, times(1)).createEmbedPlugin(pluginArchive,
            pipelineContext.getClass().getClassLoader());
        verify(pluginManagerService, times(1)).registerPlugin(plugin);
    }
}
