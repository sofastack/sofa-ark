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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.ReflectionUtils;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContextManager;
import com.alipay.sofa.ark.dynamic.common.execption.BizLauncherException;
import com.alipay.sofa.ark.dynamic.support.testng.AbstractTestNGSofaArkContextTests;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import org.springframework.util.StopWatch;

import java.io.File;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author hanyue
 * @version : BizLauncher.java, v 0.1 2022年05月26日 下午11:36 hanyue Exp $
 */
public class BizLauncher {
    public static final String               START_METHOD = "startBizAndInjectTestClasss";
    private static final ArkLogger           LOGGER       = ArkLoggerFactory.getDefaultLogger();

    private static SofaArkTestContextManager sofaArkTestContextManager;

    private Object startBizAndInjectTestClasss(Class<?> testClass) {
        StopWatch stopWatch = new StopWatch(BizLauncher.class.getSimpleName());
        try {
            sofaArkTestContextManager = getSofaArkTestContextManager(testClass);

            File bizJarFile = JarUtils.getBizFatJar();

            Attributes mainAttributes = new JarFile(bizJarFile).getManifest().getMainAttributes();
            String arkBizName = mainAttributes.getValue(Constants.ARK_BIZ_NAME);
            String arkBizVersion = mainAttributes.getValue(Constants.ARK_BIZ_VERSION);
            Biz existBiz = ArkClient.getBizManagerService().getBiz(arkBizName, arkBizVersion);
            if (existBiz != null) {
                return getTestInstanceFromBizClassLoader(testClass, existBiz.getBizClassLoader());
            }

            File unpackFile = new File(bizJarFile.getAbsolutePath() + "-unpack");
            if (!unpackFile.exists()) {
                unpackFile = FileUtils.unzip(bizJarFile, bizJarFile.getAbsolutePath() + "-unpack");
            }

            stopWatch.start("beforeInstallBiz");
            sofaArkTestContextManager.getSofaArkTestContext().setAttribute(
                SofaArkTestConstants.BIZ_FAT_JAR, unpackFile);
            sofaArkTestContextManager.beforeInstallBiz();
            stopWatch.stop();

            stopWatch.start("startBizAndInjectTestClasss");
            Biz biz = ArkClient.getBizFactoryService().createBiz(bizJarFile);

            ClientResponse clientResponse = ArkClient.installBiz(biz, new String[] {});
            if (clientResponse.getCode() != ResponseCode.SUCCESS) {
                throw new RuntimeException(clientResponse.getMessage());
            }
            Biz bizInstalled = ArkClient.getBizManagerService().getBizByIdentity(biz.getIdentity());
            Object testInstance = getTestInstanceFromBizClassLoader(testClass,
                bizInstalled.getBizClassLoader());
            stopWatch.stop();

            ((AbstractTestNGSofaArkContextTests) testInstance).getSofaArkTestContextManager()
                .afterInstallBiz();

            return testInstance;
        } catch (Throwable ex) {
            throw new BizLauncherException(ex);
        } finally {
            System.out.println(stopWatch.prettyPrint());
            LOGGER.info(stopWatch.prettyPrint());
        }
    }

    private SofaArkTestContextManager getSofaArkTestContextManager(Class<?> testClass)
                                                                                      throws Throwable {
        if (sofaArkTestContextManager == null) {
            Object testInstance = getTestInstanceFromTestClassLoader(testClass);
            sofaArkTestContextManager = ((AbstractTestNGSofaArkContextTests) testInstance)
                .getSofaArkTestContextManager();
            sofaArkTestContextManager.getSofaArkTestContext().updateState(testInstance, null, null);
        }
        return sofaArkTestContextManager;
    }

    private Object getTestInstanceFromTestClassLoader(Class<?> testestClass) throws Throwable {
        return testestClass.newInstance();
    }

    private Object getTestInstanceFromBizClassLoader(Class<?> testClass, ClassLoader bizClassLoader)
                                                                                                    throws Throwable {
        // Gets the test directory of the current class
        URL testTarget = testClass.getClass().getResource(File.separator);

        // BizClassLoader add testTarget
        ReflectionUtils.addURL(bizClassLoader, testTarget);

        // Refresh testClass
        Class<?> newTestClass = bizClassLoader.loadClass(testClass.getName());

        return newTestClass.newInstance();
    }
}