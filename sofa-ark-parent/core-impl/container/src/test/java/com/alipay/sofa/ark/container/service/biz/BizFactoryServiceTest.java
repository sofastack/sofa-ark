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

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static com.alipay.sofa.ark.api.ArkConfigs.putStringValue;
import static com.alipay.sofa.ark.container.service.ArkServiceContainerHolder.getContainer;
import static com.alipay.sofa.ark.spi.constant.Constants.*;
import static com.alipay.sofa.ark.spi.constant.Constants.UNPACK_BIZ_WHEN_INSTALL;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertNotNull;

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
        bizManagerService = getContainer().getService(BizManagerService.class);
    }

    @Test
    public void test() throws Throwable {

        ClassLoader cl = currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-plugin.jar");
        Plugin plugin = pluginFactoryService.createPlugin(FileUtils.file(samplePlugin.getFile()));
        pluginManagerService.registerPlugin(plugin);

        URL sampleBiz = cl.getResource("sample-biz.jar");
        Biz biz = bizFactoryService.createBiz(FileUtils.file(sampleBiz.getFile()));
        bizManagerService.registerBiz(biz);
        assertNotNull(biz);
        assertNotNull(biz.getBizClassLoader().getResource(ARK_PLUGIN_MARK_ENTRY));

        putStringValue(MASTER_BIZ, "master-biz");
        Biz masterBiz = bizFactoryService.createEmbedMasterBiz(cl);
        assertNotNull(masterBiz);
        assertNotNull(masterBiz.getBizClassLoader().getResource(
            "com/alipay/sofa/ark/container/service/biz/"));
    }

    @Test
    public void testCreateBizWithoutBizOperation() throws IOException {
        String originalEmbed = System.getProperty(EMBED_ENABLE);
        String originalUnpack = System.getProperty(UNPACK_BIZ_WHEN_INSTALL);
        try {
            ClassLoader cl = currentThread().getContextClassLoader();
            URL sampleBiz = cl.getResource("sample-biz.jar");
            System.setProperty(EMBED_ENABLE, "true");
            System.setProperty(UNPACK_BIZ_WHEN_INSTALL, "false");
            Biz biz = bizFactoryService.createBiz(FileUtils.file(sampleBiz.getFile()));
            assertNotNull(biz);

        } finally {
            if (originalEmbed != null) {
                System.setProperty(EMBED_ENABLE, originalEmbed);
            } else {
                System.clearProperty(EMBED_ENABLE);
            }
            if (originalUnpack != null) {
                System.setProperty(UNPACK_BIZ_WHEN_INSTALL, originalUnpack);
            } else {
                System.clearProperty(UNPACK_BIZ_WHEN_INSTALL);
            }
        }
    }

    @Test
    public void testCreateBiz() throws IOException {
        ClassLoader cl = currentThread().getContextClassLoader();
        URL sampleBiz = cl.getResource("sample-biz.jar");
        BizOperation bizOperation = new BizOperation();
        String mockVersion = "mock version";
        bizOperation.setBizVersion(mockVersion);
        Biz biz = bizFactoryService.createBiz(bizOperation, FileUtils.file(sampleBiz.getFile()));
        Assert.assertEquals(biz.getBizVersion(), mockVersion);
    }

    @Test
    public void testPackageInfo() throws Throwable {
        ClassLoader cl = currentThread().getContextClassLoader();
        URL samplePlugin = cl.getResource("sample-ark-plugin-common-0.5.1.jar");
        Plugin plugin = pluginFactoryService.createPlugin(FileUtils.file(samplePlugin.getFile()));
        ClassLoader pluginClassLoader = plugin.getPluginClassLoader();
        pluginManagerService.registerPlugin(plugin);
        Class mdc = pluginClassLoader.loadClass("org.slf4j.MDC");
        Assert.assertTrue(mdc.getClassLoader().equals(pluginClassLoader));
        assertNotNull(mdc.getPackage().getImplementationVersion());
    }

}
