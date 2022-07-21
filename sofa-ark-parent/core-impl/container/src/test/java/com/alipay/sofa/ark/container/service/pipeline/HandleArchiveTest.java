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
package com.alipay.sofa.ark.container.service.pipeline;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.pipeline.HandleArchiveStage;
import com.alipay.sofa.ark.container.service.biz.BizFactoryServiceImpl;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.container.service.plugin.PluginFactoryServiceImpl;
import com.alipay.sofa.ark.container.service.plugin.PluginManagerServiceImpl;
import com.alipay.sofa.ark.loader.EmbedClassPathArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_INCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_INCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class HandleArchiveTest {

    @Test
    public void testIncludeExcludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_INCLUDE, "pluginA");
            System.setProperty(PLUGIN_ACTIVE_EXCLUDE, "pluginA,pluginB");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_EXCLUDE);
            System.clearProperty(PLUGIN_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testIncludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_INCLUDE, "pluginA");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testExcludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_EXCLUDE, "pluginA,pluginB");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testNoIncludeExcludeBiz() {
        HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
        PluginModel pluginModel = new PluginModel();
        pluginModel.setPluginName("pluginA");
        Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
        pluginModel.setPluginName("pluginB");
        Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
    }

    @Test
    public void testIncludeExcludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_INCLUDE, "bizA:1.0.0");
            System.setProperty(BIZ_ACTIVE_EXCLUDE, "bizA:1.0.0,BizB:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizC");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_INCLUDE);
            System.clearProperty(BIZ_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testIncludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_INCLUDE, "bizA:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("pluginB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("pluginC");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testExcludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_EXCLUDE, "bizA:1.0.0,bizB:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizC");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testNoIncludeExcludePlugin() {
        HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
        BizModel bizModel = new BizModel();
        bizModel.setBizName("bizA").setBizVersion("1.0.0");
        Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
        bizModel.setBizName("bizB");
        Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
    }

    @Test
    public void testStaticCombineRegisterBiz() throws Exception {
        //HandleArchiveStage内部属性有嵌套需要mock
        PluginManagerService pluginManagerService = Mockito.spy(new PluginManagerServiceImpl());
        PluginFactoryService pluginFactoryService = Mockito.spy(new PluginFactoryServiceImpl());
        BizManagerService bizManagerService = Mockito.spy(new BizManagerServiceImpl());
        BizFactoryServiceImpl bizFactoryService = Mockito.spy(new BizFactoryServiceImpl());
        System.setProperty("sofa.ark.embed.enable", "true");
        ArkConfigs.setSystemProperty(Constants.MASTER_BIZ, "test");

        BizModel bizModel = new BizModel();
        doReturn(bizModel).when(bizFactoryService).createBiz(any(BizArchive.class));
        doReturn(true).when(bizManagerService).registerBiz(any(BizModel.class));
        //创建handleArchiveStage
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL springbootFatJar = cl.getResource("static-combine-springboot-executable.jar");
        JarFileArchive jarFileArchive = new JarFileArchive(new File(springbootFatJar.getFile()));
        Iterator<org.springframework.boot.loader.archive.Archive> archives = jarFileArchive.getNestedArchives(
                (entry) -> entry.isDirectory() ? Objects.equals(entry.getName(), "BOOT-INF/classes/") : entry.getName()
                        .startsWith("BOOT-INF/lib/"), null);
        List<URL> urls = new ArrayList<>();
        while (archives.hasNext()) {
            urls.add(archives.next().getUrl());
        }
        EmbedClassPathArchive archive = new EmbedClassPathArchive("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication",
                "main",
                urls.toArray(new URL[] {}));
        PipelineContext pipelineContext = new PipelineContext();
        pipelineContext.setExecutableArchive(archive);
        HandleArchiveStage handleArchiveStage = new HandleArchiveStage();

        //注入
        Field bizFactoryServiceF = HandleArchiveStage.class.getDeclaredField("bizFactoryService");
        Field bizManagerServiceF = HandleArchiveStage.class.getDeclaredField("bizManagerService");
        Field pluginFactoryServiceF = HandleArchiveStage.class.getDeclaredField("pluginFactoryService");
        Field pluginManagerServiceF = HandleArchiveStage.class.getDeclaredField("pluginManagerService");
        org.springframework.util.ReflectionUtils.makeAccessible(bizFactoryServiceF);
        org.springframework.util.ReflectionUtils.makeAccessible(bizManagerServiceF);
        org.springframework.util.ReflectionUtils.makeAccessible(pluginFactoryServiceF);
        org.springframework.util.ReflectionUtils.makeAccessible(pluginManagerServiceF);
        org.springframework.util.ReflectionUtils.setField(bizFactoryServiceF, handleArchiveStage, bizFactoryService);
        org.springframework.util.ReflectionUtils.setField(bizManagerServiceF, handleArchiveStage, bizManagerService);
        org.springframework.util.ReflectionUtils.setField(pluginFactoryServiceF, handleArchiveStage, pluginFactoryService);
        org.springframework.util.ReflectionUtils.setField(pluginManagerServiceF, handleArchiveStage, pluginManagerService);
        //执行
        handleArchiveStage.process(pipelineContext);
        //验证是否检测到biz包并创建biz
        Mockito.verify(bizFactoryService, times(1)).createBiz(any(BizArchive.class));

    }
}