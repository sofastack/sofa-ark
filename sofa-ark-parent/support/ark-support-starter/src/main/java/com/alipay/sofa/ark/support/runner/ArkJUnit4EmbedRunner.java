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
package com.alipay.sofa.ark.support.runner;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import static com.alipay.sofa.ark.spi.constant.Constants.EMBED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

/**
 * used for test ark biz like koupleless, which run ark plugin in embed mode
 * please refer {@link com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService#createEmbedPlugin(com.alipay.sofa.ark.spi.archive.PluginArchive, java.lang.ClassLoader)}
 *
 * @author lvjing2
 * @since 2.2.10
 */
public class ArkJUnit4EmbedRunner extends BlockJUnit4ClassRunner {

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public ArkJUnit4EmbedRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        try {
            if (!DelegateArkContainer.isStarted()) {
                System.setProperty(EMBED_ENABLE, "true");
                System.setProperty(MASTER_BIZ, "test master biz");
                DelegateArkContainer.launch(testClass);
                System.clearProperty(EMBED_ENABLE);
                System.clearProperty(MASTER_BIZ);
            }
            ClassLoader testClassLoader = DelegateArkContainer.getTestClassLoader();
            TestClass testKlazz = super.createTestClass(testClassLoader.loadClass(testClass
                .getName()));
            ClassLoaderUtils.pushContextClassLoader(ClassLoader.getSystemClassLoader());
            return testKlazz;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run(RunNotifier notifier) {
        notifier.addListener(JUnitExecutionListener.getRunListener());
        super.run(notifier);
    }
}
