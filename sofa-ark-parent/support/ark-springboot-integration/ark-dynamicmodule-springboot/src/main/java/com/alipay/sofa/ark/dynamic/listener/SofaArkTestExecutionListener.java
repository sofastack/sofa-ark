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
package com.alipay.sofa.ark.dynamic.listener;

import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import org.springframework.core.Ordered;

/**
 * The interface Sofa ark test execution listener.
 *
 * @author hanyue
 * @version : SofaArkTestExecutionListener.java, v 0.1 2022年05月24日 下午9:20 hanyue Exp $
 */
public interface SofaArkTestExecutionListener extends Ordered {

    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Before install master.
     *
     * @param testContext    Include masterFatJar
     * @param appClassLoader sun.misc.Launcher.AppClassLoader
     * @throws Exception the exception
     */
    default void beforeInstallMaster(SofaArkTestContext testContext, ClassLoader appClassLoader) throws Exception {
        // testContext.getAttribute(SofaArkTestConstants.MASTER_FAT_JAR)
    }

    /**
     * After install master.
     *
     * @param testContext    Include matserApplicationContext
     * @param appClassLoader sun.misc.Launcher.AppClassLoader
     * @throws Exception
     */
    default void afterInstallMaster(SofaArkTestContext testContext, ClassLoader appClassLoader) throws Exception {

    }

    /**
     * Before install biz.
     *
     * @param testContext       Include bizFatJar
     * @param masterClassLoader com.alipay.sofa.ark.dynamic.loader.TestClassloader
     * @throws Exception the exception
     */
    default void beforeInstallBiz(SofaArkTestContext testContext, ClassLoader masterClassLoader) throws Exception {
        // testContext.getAttribute(SofaArkTestConstants.BIZ_FAT_JAR)
    }

    /**
     * After install biz.
     *
     * @param testContext    Include bizApplicationContext
     * @param bizClassLoader com.alipay.sofa.ark.container.service.classloader.BizClassLoader
     * @throws Exception the exception
     */
    default void afterInstallBiz(SofaArkTestContext testContext, ClassLoader bizClassLoader) throws Exception {

    }
}