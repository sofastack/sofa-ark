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

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.registry.PluginServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.common.log.Constants;
import mockit.Mock;
import mockit.MockUp;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BaseTest {
    protected ArkServiceContainer arkServiceContainer = new ArkServiceContainer();

    @Before
    public void before() {
        arkServiceContainer.start();
    }

    @After
    public void after() {
        arkServiceContainer.stop();
    }

    @BeforeClass
    public static void beforeClass() {
        System.setProperty(Constants.LOG_ENV_SUFFIX, ArkLoggerFactory.SOFA_ARK_LOGGER_SPACE
                                                     + ":dev");
    }

    static {
        // fix cobertura bug
        new PluginServiceProvider(new PluginModel());
    }

    static {
        new MockUp<ManagementFactory>() {
            @Mock
            public RuntimeMXBean getRuntimeMXBean() {
                return new MockUp<RuntimeMXBean>() {
                    @Mock
                    List<String> getInputArguments() {
                        List<String> mockArguments = new ArrayList<>();
                        String filePath = this.getClass().getClassLoader()
                            .getResource("SampleClass.class").getPath();
                        String workingPath = new File(filePath).getParent();
                        mockArguments.add(String.format("javaaget:%s", workingPath));
                        mockArguments.add(String.format("-javaagent:%s", workingPath));
                        mockArguments.add(String.format("-javaagent:%s=xx", workingPath));
                        return mockArguments;
                    }
                }.getMockInstance();
            }
        };
    }
}