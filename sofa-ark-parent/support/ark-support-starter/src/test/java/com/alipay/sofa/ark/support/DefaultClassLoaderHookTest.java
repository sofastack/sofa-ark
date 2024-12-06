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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
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
        URL masterUrl1 = this.getClass().getClassLoader()
            .getResource("sample-ark-plugin-common-0.5.1.jar");
        URL masterUrl2 = this.getClass().getClassLoader().getResource("sofa-ark-sample-springboot-ark-0.3.0.jar");
        URL masterUrl3 = this.getClass().getClassLoader().getResource("aopalliance-1.0.jar");
        URL masterUrl4 = this.getClass().getClassLoader()
            .getResource("com.springsource.org.aopalliance-1.0.0.jar");

        BizModel bizModel = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED,
            new URL[] { bizUrl });
        bizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        bizModel.setDenyImportResources(StringUtils.EMPTY_STRING);
        bizModel
            .setDeclaredLibraries("sample-ark-plugin-common,com.springsource.org.aopalliance");

        List<URL> masterUrls = new ArrayList<>();
        Enumeration<URL> urlEnumeration = this.getClass().getClassLoader().getResources("");
        while (urlEnumeration.hasMoreElements()) {
            URL url = urlEnumeration.nextElement();
            masterUrls.add(url);
        }
        masterUrls.add(masterUrl1);
        masterUrls.add(masterUrl2);
        masterUrls.add(masterUrl3);
        masterUrls.add(masterUrl4);

        BizModel masterBizModel = createTestBizModel("master biz", "1.0.0", BizState.RESOLVED,
            masterUrls.toArray(new URL[0]));
        masterBizModel.setDenyImportClasses(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportPackages(StringUtils.EMPTY_STRING);
        masterBizModel.setDenyImportResources(StringUtils.EMPTY_STRING);

        bizManagerService.registerBiz(bizModel);
        bizManagerService.registerBiz(masterBizModel);

        ArkClient.setMasterBiz(masterBizModel);

        // case 1: find class from multiple libs in master classloader
        Class<?> adviceClazz = bizModel.getBizClassLoader().loadClass("org.aopalliance.aop.Advice");
        Assert.assertEquals(adviceClazz.getClassLoader(), masterBizModel.getBizClassLoader());

        // case 2: find class from master but not set provided in biz model
        Assert.assertThrows(ArkLoaderException.class, () -> bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.facade.SamplemasterService"));

        // case 3: find class from master in classpath
        Assert.assertEquals(masterBizModel.getBizClassLoader(), bizModel.getBizClassLoader().loadClass(DelegateArkContainer.class.getName()).getClassLoader());

        // case 4: find class from master in jar
        Assert.assertEquals(masterBizModel.getBizClassLoader(), bizModel.getBizClassLoader().loadClass("com.alipay.sofa.ark.sample.common.SampleClassExported").getClassLoader());

        // case 5: find resource from master but not set provided in biz model
        Assert.assertNull(bizModel.getBizClassLoader().getResource("org/slf4j/ILoggerFactory.class"));

        // case 6: find resource from master in classpath
        Assert.assertNotNull(bizModel.getBizClassLoader().getResource(
            "sample-ark-1.0.0-ark-biz.jar"));

        // case 7: find resource from master in multiple jar
        Assert.assertNull(bizModel.getBizClassLoader().getResource("Sample_Resource_Exported_A"));

        // case 8: find resources from master but not set provided in biz model
        Assert.assertFalse(bizModel.getBizClassLoader().getResources("org/slf4j/ILoggerFactory.class")
            .hasMoreElements());

        // case 9: find resources from master in classpath
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("sample-ark-1.0.0-ark-biz.jar")
            .hasMoreElements());

        // case 10: find resources from master in jar
        Assert.assertTrue(bizModel.getBizClassLoader().getResources("Sample_Resource_Exported")
            .hasMoreElements());
    }

    @Test
    public void getAllResources() throws IOException {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        URL resourceUrl = this.getClass().getClassLoader()
            .getResource("sample-ark-plugin-common-0.5.1.jar");

        URL[] bizUrls = new URL[] { bizUrl, resourceUrl };
        BizModel declaredBiz = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, bizUrls);
        declaredBiz.setDeclaredLibraries("sample-ark-plugin-common");

        BizModel notDeclaredBiz = createTestBizModel("biz B", "1.0.0", BizState.RESOLVED, bizUrls);
        notDeclaredBiz.setDeclaredLibraries("");

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

        // declaredMode: get all resources from biz and master biz
        Enumeration<URL> resourcesOnlyFromBiz = notDeclaredBiz.getBizClassLoader().getResources(
            "Sample_Resource_Exported");
        Assert.assertNotNull(resourcesOnlyFromBiz.nextElement());
        Assert.assertFalse(resourcesOnlyFromBiz.hasMoreElements());
    }

    @Test
    public void testPostFindCglibClass() throws ClassNotFoundException {
        URL bizUrl = this.getClass().getClassLoader().getResource("sample-ark-1.0.0-ark-biz.jar");
        BizModel testBiz = createTestBizModel("biz A", "1.0.0", BizState.RESOLVED, new URL[] { bizUrl });
        testBiz.setDenyImportClasses(StringUtils.EMPTY_STRING);
        testBiz.setDenyImportPackages(StringUtils.EMPTY_STRING);
        testBiz.setDenyImportResources(StringUtils.EMPTY_STRING);

        URL[] urLs = ClassLoaderUtils.getURLs(this.getClass().getClassLoader());
        BizModel masterBiz = createTestBizModel("master biz", "1.0.0", BizState.RESOLVED, urLs);
        masterBiz.setClassLoader(this.getClass().getClassLoader());

        bizManagerService.registerBiz(testBiz);
        bizManagerService.registerBiz(masterBiz);
        ArkClient.setMasterBiz(masterBiz);

        Assert.assertThrows(ArkLoaderException.class, () -> testBiz.getBizClassLoader().loadClass("xxxxCGLIB$$"));
    }
}
