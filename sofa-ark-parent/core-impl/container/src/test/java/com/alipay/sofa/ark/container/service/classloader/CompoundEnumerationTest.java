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
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class CompoundEnumerationTest extends BaseTest {
    private PluginFactoryService pluginFactoryService;

    private PluginManagerService pluginManagerService;

    private BizFactoryService    bizFactoryService;

    private BizManagerService    bizManagerService;

    @Before
    public void before() {
        super.before();
        pluginManagerService = arkServiceContainer.getService(PluginManagerService.class);
        pluginFactoryService = arkServiceContainer.getService(PluginFactoryService.class);
        bizFactoryService = arkServiceContainer.getService(BizFactoryService.class);
        bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
    }

    @Test
    public void test() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL samplePlugin = cl.getResource("sample-plugin.jar");
        Plugin plugin = pluginFactoryService.createPlugin(new File(samplePlugin.getFile()));
        pluginManagerService.registerPlugin(plugin);

        URL sampleBiz = cl.getResource("sample-biz.jar");
        Biz biz = bizFactoryService.createBiz(new File(sampleBiz.getFile()));
        bizManagerService.registerBiz(biz);
        CompoundEnumeration<URL> e = (CompoundEnumeration<URL>) biz.getBizClassLoader()
            .getResources(Constants.ARK_PLUGIN_MARK_ENTRY);

        Assert.assertTrue(e.hasMoreElements());
        URL url = e.nextElement();
        Assert.assertNotNull(url);
    }
}
