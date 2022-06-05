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
package com.alipay.sofa.ark.dynamic.launcher;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContextManager;
import com.alipay.sofa.ark.dynamic.common.execption.AppLauncherException;
import com.alipay.sofa.ark.dynamic.loader.TestJarLanucher;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The type App launcher.
 *
 * @author hanyue
 * @version : AppLauncher.java, v 0.1 2022年05月26日 下午11:35 hanyue Exp $
 */
public class AppLauncher {
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final ArkLogger     LOGGER  = ArkLoggerFactory.getDefaultLogger();

    /**
     * Start ark container and master biz.
     *
     * @param sofaArkTestContextManager the sofa ark test context manager
     */
    public static void startArkContainerAndMasterBiz(SofaArkTestContextManager sofaArkTestContextManager) {
        if (STARTED.compareAndSet(false, true)) {
            StopWatch stopWatch = new StopWatch(AppLauncher.class.getSimpleName());
            try {
                SofaArkTestContext serverlessTestContext = sofaArkTestContextManager
                    .getSofaArkTestContext();

                File file = JarUtils.getMasterBizFatJar();
                File unpackFile = new File(file.getAbsolutePath() + "-unpack");
                if (!unpackFile.exists()) {
                    unpackFile = FileUtils.unzip(file, file.getAbsolutePath() + "-unpack");
                }

                stopWatch.start("beforeInstallMaster");
                serverlessTestContext.setAttribute(SofaArkTestConstants.MASTER_FAT_JAR, unpackFile);
                sofaArkTestContextManager.beforeInstallMaster();
                stopWatch.stop();

                try {
                    stopWatch.start("startArkContainerAndMasterBiz");
                    TestJarLanucher testJarLanucher = new TestJarLanucher(new ExplodedArchive(
                        unpackFile), unpackFile);
                    testJarLanucher.run(new String[] {});
                    stopWatch.stop();
                } finally {
                    System.out.println(stopWatch.prettyPrint());
                    LOGGER.info(stopWatch.prettyPrint());

                    ClassLoaderUtils.pushContextClassLoader(AppLauncher.class.getClassLoader());
                }
            } catch (Throwable ex) {
                throw new AppLauncherException(ex);
            }
        }
    }
}