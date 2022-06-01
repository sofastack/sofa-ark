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

/**
 * @author hanyue
 * @version : SofaArkTestExecutionListener.java, v 0.1 2022年05月24日 下午9:20 hanyue Exp $
 */
public interface SofaArkTestExecutionListener {

    /**
     * @param testContext    Include masterFatJar
     * @param appClassLoader sun.misc.Launcher.AppClassLoader
     * @throws Exception
     */
    default void beforeInstallMaster(SofaArkTestContext testContext, ClassLoader appClassLoader) throws Exception {
        // testContext.getAttribute(SofaArkTestConstants.MASTER_FAT_JAR)
    }

    /**
     * @param testContext       Include bizFatJar and matserApplicationContext
     * @param masterClassLoader com.alipay.sofa.ark.dynamic.loader.TestClassloader
     * @throws Exception
     */
    default void beforeInstallBiz(SofaArkTestContext testContext, ClassLoader masterClassLoader) throws Exception {
        // testContext.getAttribute(SofaArkTestConstants.BIZ_FAT_JAR)
    }

    /**
     * @param testContext    Include bizApplicationContext
     * @param bizClassLoader com.alipay.sofa.ark.container.service.classloader.BizClassLoader
     * @throws Exception
     */
    default void afterInstallBiz(SofaArkTestContext testContext, ClassLoader bizClassLoader) throws Exception {

    }
}