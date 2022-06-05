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
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.ReflectionUtils;
import com.alipay.sofa.ark.dynamic.common.MasterBizClassloaderHolder;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContextManager;
import com.alipay.sofa.ark.dynamic.common.execption.BizLauncherException;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.transloader.ClassWrapper;
import com.alipay.sofa.ark.transloader.Transloader;
import com.alipay.sofa.ark.transloader.invoke.InvocationFieldDescription;
import com.alipay.sofa.ark.transloader.invoke.InvocationMethodDescription;
import org.springframework.util.StopWatch;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * The type Biz launcher.
 *
 * @author hanyue
 * @version : BizLauncher.java, v 0.1 2022年05月26日 下午11:36 hanyue Exp $
 */
public class BizLauncher {
    private static final ArkLogger   LOGGER      = ArkLoggerFactory.getDefaultLogger();
    private static final Transloader TRANSLOADER = Transloader.DEFAULT;

    /**
     * Start biz and inject test classs object.
     *
     * @param sofaArkTestContextManager the sofa ark test context manager
     * @return the object
     */
    public static Object startBizAndInjectTestClasss(SofaArkTestContextManager sofaArkTestContextManager) {
        StopWatch stopWatch = new StopWatch(BizLauncher.class.getSimpleName());
        try {
            SofaArkTestContext sofaArkTestContext = sofaArkTestContextManager
                    .getSofaArkTestContext();
            ClassLoader classLoader = MasterBizClassloaderHolder.getClassLoader();
            ClassWrapper arkClientWrapper = TRANSLOADER.wrap(classLoader.loadClass(ArkClient.class
                    .getName()));

            File bizJarFile = JarUtils.getBizFatJar();

            Attributes mainAttributes = new JarFile(bizJarFile).getManifest().getMainAttributes();
            String arkBizName = mainAttributes.getValue(Constants.ARK_BIZ_NAME);
            String arkBizVersion = mainAttributes.getValue(Constants.ARK_BIZ_VERSION);

            Object bizManagerService = arkClientWrapper.invoke(new InvocationFieldDescription(
                    "bizManagerService"));

            Object existBiz = TRANSLOADER.wrap(bizManagerService).invoke(
                    new InvocationMethodDescription("getBiz",
                            new Object[] {arkBizName, arkBizVersion}));
            if (existBiz != null) {
                ClassLoader bizClassLoader = (ClassLoader) TRANSLOADER.wrap(existBiz).invoke(
                        new InvocationMethodDescription("getBizClassLoader"));
                return bizClassLoader.loadClass(sofaArkTestContext.getTestClass().getName()).newInstance();
            }

            File unpackFile = new File(bizJarFile.getAbsolutePath() + "-unpack");
            if (!unpackFile.exists()) {
                unpackFile = FileUtils.unzip(bizJarFile, bizJarFile.getAbsolutePath() + "-unpack");
            }

            sofaArkTestContextManager.getSofaArkTestContext().setAttribute(
                    SofaArkTestConstants.BIZ_FAT_JAR, unpackFile);
            sofaArkTestContextManager.beforeInstallBiz();

            stopWatch.start("startBizAndInjectTestClasss");
            Object clientResponse = arkClientWrapper.invoke(new InvocationMethodDescription(
                    "installBiz", new Object[] {bizJarFile}));

            ResponseCode responseCode = (ResponseCode) TRANSLOADER.wrap(clientResponse)
                    .invokeCastable(new InvocationMethodDescription("getCode"));
            if (!Objects.equals(responseCode.name(), ResponseCode.SUCCESS.name())) {
                throw new RuntimeException((String) TRANSLOADER.wrap(clientResponse).invoke(
                        new InvocationMethodDescription("getMessage")));
            }

            existBiz = TRANSLOADER.wrap(bizManagerService).invoke(
                    new InvocationMethodDescription("getBiz",
                            new Object[] {arkBizName, arkBizVersion}));
            ClassLoader bizClassLoader = (ClassLoader) TRANSLOADER.wrap(existBiz).invoke(
                    new InvocationMethodDescription("getBizClassLoader"));
            stopWatch.stop();

            return refreshInstance(sofaArkTestContext.getTestClass(), bizClassLoader);
        } catch (Throwable ex) {
            throw new BizLauncherException(ex);
        } finally {
            System.out.println(stopWatch.prettyPrint());
            LOGGER.info(stopWatch.prettyPrint());
        }
    }

    private static Object refreshInstance(Class<?> testClass, ClassLoader bizClassLoader)
            throws Throwable {
        ClassLoader testClassLoader = MasterBizClassloaderHolder.getClassLoader();

        // Gets All urls
        URL[] urls = ((URLClassLoader) BizLauncher.class.getClassLoader()).getURLs();

        // Gets the test directory of the current class
        URL testTarget = testClass.getClass().getResource(File.separator);

        // MastBizClassLoader add testTarget
        for (URL url : urls) {
            if (Objects.equals(testTarget, url)) {
                continue;
            }
            ReflectionUtils.addURL(testClassLoader, url);
        }

        Object newInstance = refreshBizInstance(testClass, bizClassLoader, testTarget);
        refreshMasterInstance(testClass, testClassLoader, testTarget);

        return newInstance;
    }

    private static Object refreshBizInstance(Class<?> testClass, ClassLoader bizClassLoader,
                                             URL testTarget) throws Throwable {

        // BizClassLoader add testTarget
        ReflectionUtils.addURL(bizClassLoader, testTarget);

        // Refresh testClass
        Class<?> newTestClass = bizClassLoader.loadClass(testClass.getName());

        // instance
        Object newInstance = newTestClass.newInstance();

        // Broadcast by SofaArkTestExecutionListener
        Object getSofaArkTestContextManager = TRANSLOADER.wrap(newInstance).invoke(
                new InvocationMethodDescription("getSofaArkTestContextManager"));
        Transloader.DEFAULT.wrap(getSofaArkTestContextManager).invoke(
                new InvocationMethodDescription("afterInstallBiz"));

        return newInstance;
    }

    private static Object refreshMasterInstance(Class<?> testClass, ClassLoader testClassLoader,
                                                URL testTarget) throws Throwable {
        // TestClassLoader add testTarget
        ReflectionUtils.addURL(testClassLoader, testTarget);

        // Refresh testClass
        Class<?> newTestClass = testClassLoader.loadClass(testClass.getName());

        // instance
        Object newInstance = newTestClass.newInstance();

        // Broadcast by SofaArkTestExecutionListener
        Object getSofaArkTestContextManager = TRANSLOADER.wrap(newInstance).invoke(
                new InvocationMethodDescription("getSofaArkTestContextManager"));
        Transloader.DEFAULT.wrap(getSofaArkTestContextManager).invoke(
                new InvocationMethodDescription("afterInstallMaster"));

        return newInstance;
    }
}