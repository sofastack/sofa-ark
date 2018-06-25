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
package com.alipay.sofa.ark.container.test;

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.service.classloader.ClassloaderService;

import java.net.URL;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class TestHelper {

    private final static String MOCK_BIZ_IDENTITY = "Mock Biz:Mock Version";
    private ArkContainer        arkContainer;

    public TestHelper(Object object) {
        this.arkContainer = (ArkContainer) object;
    }

    public ClassLoader createTestClassLoader() {
        PipelineContext context = arkContainer.getPipelineContext();
        URL[] classpath = context.getLaunchCommand().getClasspath();
        ClassloaderService classloaderService = arkContainer.getArkServiceContainer().getService(
            ClassloaderService.class);
        return new TestClassLoader(MOCK_BIZ_IDENTITY, classpath,
            classloaderService.getSystemClassloader());
    }

    public boolean isStarted() {
        return arkContainer.isStarted();
    }

}