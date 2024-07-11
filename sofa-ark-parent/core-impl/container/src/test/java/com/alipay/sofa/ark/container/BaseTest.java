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
package com.alipay.sofa.ark.container;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.pipeline.RegisterServiceStage;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.extension.ExtensionLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.MockedStatic;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.common.util.FileUtils.file;
import static com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader.setExtensionLoaderService;
import static org.mockito.Mockito.*;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BaseTest {

    private URL                     jarURL              = ArkContainerTest.class.getClassLoader()
                                                            .getResource("test.jar");
    protected ArkServiceContainer   arkServiceContainer = new ArkServiceContainer(new String[] {});
    protected ArkContainer          arkContainer;
    MockedStatic<ManagementFactory> managementFactoryMockedStatic;
    static {
        // fix cobertura bug
        new PluginServiceProvider(new PluginModel());
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

    public static BizModel createTestBizModel(String bizName, String bizVersion, BizState bizState,
                                              ClassLoader classLoader) {
        BizModel bizModel = new BizModel().setBizState(bizState);
        bizModel.setBizName(bizName).setBizVersion(bizVersion);
        bizModel.setClassLoader(classLoader);
        return bizModel;
    }

    @Before
    public void before() {

        List<String> mockArguments = new ArrayList<>();
        String filePath = this.getClass().getClassLoader().getResource("SampleClass.class").getPath();
        String workingPath = file(filePath).getParent();
        mockArguments.add(String.format("javaaget:%s", workingPath));
        mockArguments.add(String.format("-javaagent:%s", workingPath));
        mockArguments.add(String.format("-javaagent:%s=xx", workingPath));

        RuntimeMXBean runtimeMXBean = mock(RuntimeMXBean.class);
        when(runtimeMXBean.getInputArguments()).thenReturn(mockArguments);
        when(runtimeMXBean.getName()).thenReturn("");

        managementFactoryMockedStatic = mockStatic(ManagementFactory.class);
        managementFactoryMockedStatic.when(ManagementFactory::getRuntimeMXBean).thenReturn(runtimeMXBean);

        arkServiceContainer.start();
        arkServiceContainer.getService(RegisterServiceStage.class).process(null);
        setExtensionLoaderService(arkServiceContainer.getService(ExtensionLoaderService.class));
    }

    @After
    public void after() {

        arkServiceContainer.stop();
        if (arkContainer != null) {
            arkContainer.stop();
        }
        managementFactoryMockedStatic.close();
    }

    @BeforeClass
    public static void beforeClass() {
    }

    protected void registerMockPlugin() {
        if (arkContainer == null) {
            String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
            arkContainer = (ArkContainer) ArkContainer.main(args);
        }
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        Plugin plugin = new PluginModel().setPluginName("mock")
            .setPluginClassLoader(this.getClass().getClassLoader()).setImportClasses("")
            .setImportPackages("").setImportResources("");
        pluginManagerService.registerPlugin(plugin);
    }

    protected void registerMockBiz() {
        if (arkContainer == null) {
            String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
            arkContainer = (ArkContainer) ArkContainer.main(args);
        }
        BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
        Biz biz = new BizModel().setBizName("mock").setBizVersion("1.0")
            .setClassLoader(this.getClass().getClassLoader()).setDenyImportPackages("")
            .setDenyImportClasses("").setDenyImportResources("").setBizState(BizState.RESOLVED);
        bizManagerService.registerBiz(biz);
        ((BizModel) biz).setBizState(BizState.ACTIVATED);
    }
}
