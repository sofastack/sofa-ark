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
package com.alipay.sofa.ark.support;

import java.io.IOException;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.pipeline.RegisterServiceStage;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.extension.ExtensionLoaderService;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import com.alipay.sofa.ark.support.common.DelegateToMasterBizClassLoaderHook;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class DefaultClassLoaderHookTest {

    private BizManagerService         bizManagerService;

    private final ArkServiceContainer arkServiceContainer = new ArkServiceContainer(new String[] {});

    @Before
    public void before() {

        arkServiceContainer.start();
        arkServiceContainer.getService(RegisterServiceStage.class).process(null);
        ArkServiceLoader.setExtensionLoaderService(arkServiceContainer
            .getService(ExtensionLoaderService.class));

        ArkConfigs.setSystemProperty(Constants.BIZ_CLASS_LOADER_HOOK_DIR,
            DelegateToMasterBizClassLoaderHook.class.getName());

        bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
    }

    @After
    public void after() {
        arkServiceContainer.stop();
    }

    public static BizModel createTestBizModel(String bizName, String bizVersion, BizState bizState,
                                              URL[] urls) {
        BizModel bizModel = new BizModel().setBizState(bizState);
        bizModel.setBizName(bizName).setBizVersion(bizVersion);
        BizClassLoader bizClassLoader = new BizClassLoader(bizModel.getIdentity(), urls);
        bizClassLoader.setBizModel(bizModel);
        bizModel.setClassPath(urls).setClassLoader(bizClassLoader);
        return bizModel;
    }

    @Test
    public void testLoadClassFromClassLoaderHook() throws Exception {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL pluginUrl1 = this.getClass().getClassLoader().getResource("sample-ark-plugin-0.5.0.jar");
        URL pluginUrl2 = this.getClass().getClassLoader().getResource("sample-biz-0.3.0.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED,
                new URL[] { bizUrl });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel.setDeclaredLibraries("sample-ark-plugin");

        List<URL> masterUrls = new ArrayList<>();
        Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources("");
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            masterUrls.add(url);
        }
        masterUrls.add(pluginUrl1);
        masterUrls.add(pluginUrl2);

        BizModel masterBizModel = createTestBizModel("master biz", "1.0.0", BizState.RESOLVED,
                 masterUrls.toArray(new URL[0]));
        masterBizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportResources(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);
        bizManagerService.registerBiz(masterBizModel);

        ArkClient.setMasterBiz(masterBizModel);

        // case 1: find class from plugin but not set provided in biz model
        Assert.assertThrows(ArkLoaderException.class, () -> bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication"));

        // case 2: find class from plugin in classpath
        Assert.assertEquals(masterBizModel.getBizClassLoader(), bizModel.getBizClassLoader().loadClass(DelegateArkContainer.class.getName()).getClassLoader());

        // case 3: find class from plugin in jar
        Assert.assertEquals(masterBizModel.getBizClassLoader(), bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.common.SampleClassExported").getClassLoader());

        // case 4: find resource from plugin but not set provided in biz model
        Assert.assertNull(bizModel.getBizClassLoader().getResource("META-INF/spring/service.xml"));

        // case 5: find resource from plugin in classpath
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar"));

        // case 6: find resource from plugin in jar
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource("Sample_Resource_Exported"));


        // case 7: find resources from plugin but not set provided in biz model
        Assert.assertFalse(bizModel.getBizClassLoader().getResources("META-INF/spring/service.xml").hasMoreElements());

        // case 8: find resources from plugin in classpath
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("sample-ark-1.0.0-ark-biz.jar").hasMoreElements());

        // case 9: find resources from plugin in jar
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("Sample_Resource_Exported").hasMoreElements());
    }

    @Test
    public void getAllResources() throws IOException {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL resourceUrl = this.getClass().getClassLoader()
            .getResource("sample-ark-plugin-0.5.0.jar");

        URL[] bizUrls = new URL[] { bizUrl, resourceUrl };
        BizModel declaredBiz = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, bizUrls);
        declaredBiz.setDeclaredLibraries("sample-ark-plugin");

        BizModel notDeclaredBiz = createTestBizModel("biz B", "1.0.0", BizState.RESOLVED, bizUrls);
        declaredBiz.setDeclaredLibraries("");

        List<URL> masterUrls = new ArrayList<>();
        Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources("");
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            masterUrls.add(url);
        }
        masterUrls.add(resourceUrl);

        BizModel masterBizModel = createTestBizModel("master biz", "1.0.0", BizState.RESOLVED,
            masterUrls.toArray(new URL[0]));
        masterBizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportResources(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(declaredBiz);
        bizManagerService.registerBiz(notDeclaredBiz);
        bizManagerService.registerBiz(masterBizModel);

        ArkClient.setMasterBiz(masterBizModel);

        Assert.assertTrue(masterBizModel.getBizClassLoader()
            .getResources("Sample_Resource_Exported").hasMoreElements());
        // declaredMode: get all resources from biz and master biz
        Enumeration<URL> resourcesFromBizAndMasterBiz = declaredBiz.getBizClassLoader()
            .getResources("Sample_Resource_Exported");
        Assert.assertNotNull(resourcesFromBizAndMasterBiz.nextElement());
        Assert.assertNotNull(resourcesFromBizAndMasterBiz.nextElement());

        // declaredMode: get all resources from biz and master biz
        Enumeration<URL> resourcesOnlyFromBiz = notDeclaredBiz.getBizClassLoader().getResources(
            "Sample_Resource_Exported");
        Assert.assertNotNull(resourcesOnlyFromBiz.nextElement());
        Assert.assertFalse(resourcesOnlyFromBiz.hasMoreElements());
    }
}
