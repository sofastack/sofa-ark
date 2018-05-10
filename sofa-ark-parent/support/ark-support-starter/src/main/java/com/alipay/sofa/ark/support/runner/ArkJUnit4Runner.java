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

import com.alipay.sofa.ark.common.util.ClassloaderUtils;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class ArkJUnit4Runner extends BlockJUnit4ClassRunner {

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code klass}
     *
     * @param klass
     * @throws InitializationError if the test class is malformed.
     */
    public ArkJUnit4Runner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        try {
            if (!DelegateArkContainer.isStarted()) {
                DelegateArkContainer.launch();
            }
            ClassLoader testClassLoader = DelegateArkContainer.getTestClassLoader();
            TestClass testKlazz = super.createTestClass(testClassLoader.loadClass(testClass
                .getName()));
            ClassloaderUtils.pushContextClassloader(ClassLoader.getSystemClassLoader());
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