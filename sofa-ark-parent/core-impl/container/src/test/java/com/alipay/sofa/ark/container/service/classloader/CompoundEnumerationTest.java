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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
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
import java.util.NoSuchElementException;

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

    @Test(expected = NoSuchElementException.class)
    public void test() throws IOException {
        URL bizURL = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL pluginURL = this.getClass().getClassLoader().getResource("sample-plugin.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] {
                bizURL, pluginURL });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizManagerService.registerBiz(bizModel);
        CompoundEnumeration<URL> e = (CompoundEnumeration<URL>) bizModel.getBizClassLoader()
            .getResources(Constants.ARK_PLUGIN_MARK_ENTRY);

        Assert.assertTrue(e.hasMoreElements());
        URL url = e.nextElement();
        Assert.assertNotNull(url);
        e.nextElement();
    }
}
