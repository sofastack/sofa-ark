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
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class BizFactoryServiceTest extends BaseTest {

    private PluginFactoryService pluginFactoryService;

    private PluginManagerService pluginManagerService;

    private BizFactoryService    bizFactoryService;

    private BizManagerService    bizManagerService;

    @Override
    public void before() {
        super.before();
        pluginManagerService = arkServiceContainer.getService(PluginManagerService.class);
        pluginFactoryService = arkServiceContainer.getService(PluginFactoryService.class);
        bizFactoryService = arkServiceContainer.getService(BizFactoryService.class);
        bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
    }

    @Test
    public void test() throws Throwable {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL samplePlugin = cl.getResource("sample-plugin.jar");
        Plugin plugin = pluginFactoryService.createPlugin(new File(samplePlugin.getFile()));
        pluginManagerService.registerPlugin(plugin);

        URL sampleBiz = cl.getResource("sample-biz.jar");
        Biz biz = bizFactoryService.createBiz(new File(sampleBiz.getFile()));
        bizManagerService.registerBiz(biz);
        Assert.assertNotNull(biz);
        Assert.assertNotNull(biz.getBizClassLoader().getResource(Constants.ARK_PLUGIN_MARK_ENTRY));

        ArkConfigs.putStringValue(Constants.MASTER_BIZ, "master-biz");
        Biz masterBiz = bizFactoryService.createEmbedMasterBiz(cl);
        Assert.assertNotNull(masterBiz);
        Assert.assertNotNull(masterBiz.getBizClassLoader().getResource(
            "com/alipay/sofa/ark/container/service/biz/"));
    }

    @Test
    public void testCreateBiz() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL sampleBiz = cl.getResource("sample-biz.jar");
        BizOperation bizOperation = new BizOperation();
        String mockVersion = "mock version";
        bizOperation.setBizVersion(mockVersion);
        Biz biz = bizFactoryService.createBiz(bizOperation, new File(sampleBiz.getFile()));
        Assert.assertEquals(biz.getBizVersion(), mockVersion);
    }

    @Test
    public void testPackageInfo() throws Throwable {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-ark-plugin.jar");
        Plugin plugin = pluginFactoryService.createPlugin(new File(samplePlugin.getFile()));
        ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
        pluginManagerService.registerPlugin(plugin);
        Class mdc = pluginClassLoader.loadClass("org.slf4j.MDC");
        Assert.assertTrue(mdc.getClassLoader().equals(pluginClassLoader));
        Assert.assertNotNull(mdc.getPackage().getImplementationVersion());
    }

}