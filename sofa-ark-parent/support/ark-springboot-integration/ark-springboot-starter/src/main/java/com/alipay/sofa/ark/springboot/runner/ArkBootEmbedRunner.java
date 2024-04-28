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
package com.alipay.sofa.ark.springboot.runner;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import com.alipay.sofa.ark.support.runner.JUnitExecutionListener;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;

import static com.alipay.sofa.ark.spi.constant.Constants.EMBED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

/**
 * Corresponding to {@literal org.springframework.test.context.junit4.SpringRunner}
 * used for test ark biz like koupleless, which run ark plugin in embed mode
 * please refer {@link com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService#createEmbedPlugin(com.alipay.sofa.ark.spi.archive.PluginArchive, java.lang.ClassLoader)}
 *
 * @author lvjing2
 * @since 2.2.10
 */
public class ArkBootEmbedRunner extends Runner implements Filterable, Sortable {

    private static final String SPRING_RUNNER = "org.springframework.test.context.junit4.SpringRunner";

    /**
     * {@see org.springframework.test.context.junit4.SpringRunner}
     */
    private Runner              runner;

    @SuppressWarnings("unchecked")
    public ArkBootEmbedRunner(Class<?> klazz) {
        if (!DelegateArkContainer.isStarted()) {
            System.setProperty(EMBED_ENABLE, "true");
            System.setProperty(MASTER_BIZ, "test master biz");
            DelegateArkContainer.launch(klazz);
            System.clearProperty(EMBED_ENABLE);
            System.clearProperty(MASTER_BIZ);
        }

        Class springRunnerClass = DelegateArkContainer.loadClass(SPRING_RUNNER);
        Class testClass = DelegateArkContainer.loadClass(klazz.getName());
        try {
            runner = (Runner) springRunnerClass.getConstructor(Class.class).newInstance(testClass);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Description getDescription() {
        return runner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        ClassLoader oldClassLoader = ClassLoaderUtils.pushContextClassLoader(DelegateArkContainer
            .getTestClassLoader());
        try {
            notifier.addListener(JUnitExecutionListener.getRunListener());
            runner.run(notifier);
        } finally {
            ClassLoaderUtils.popContextClassLoader(oldClassLoader);
        }
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        ((Filterable) runner).filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        ((Sortable) runner).sort(sorter);
    }
}
