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

            // BizJar
            File bizJarFile = JarUtils.getBizFatJar();
            Attributes mainAttributes = new JarFile(bizJarFile).getManifest().getMainAttributes();
            String arkBizName = mainAttributes.getValue(Constants.ARK_BIZ_NAME);
            String arkBizVersion = mainAttributes.getValue(Constants.ARK_BIZ_VERSION);

            // Once again access
            ArkClientInvoker arkClientProxy = new DefaultArkClientInvoker();
            Object existBiz = arkClientProxy.getBiz(arkBizName, arkBizVersion);
            if (existBiz != null) {
                ClassLoader bizClassLoader = arkClientProxy.getBizClassLoader(existBiz);
                return bizClassLoader.loadClass(sofaArkTestContext.getTestClass().getName()).newInstance();
            }

            File unpackFile = new File(bizJarFile.getAbsolutePath() + "-unpack");
            if (!unpackFile.exists()) {
                unpackFile = FileUtils.unzip(bizJarFile, bizJarFile.getAbsolutePath() + "-unpack");
            }
            sofaArkTestContextManager.getSofaArkTestContext().setAttribute(
                    SofaArkTestConstants.BIZ_FAT_JAR, unpackFile);
            sofaArkTestContextManager.beforeInstallBiz();

            // install current module
            stopWatch.start("startBizAndInjectTestClasss");
            ArkResponse reponse = arkClientProxy.installBiz(bizJarFile);
            if (!Objects.equals(reponse.responseCode.name(), ResponseCode.SUCCESS.name())) {
                throw new RuntimeException(reponse.errMsg);
            }

            existBiz = arkClientProxy.getBiz(arkBizName, arkBizVersion);
            ClassLoader bizClassLoader = arkClientProxy.getBizClassLoader(existBiz);
            stopWatch.stop();

            // inject testClass
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
        // Gets All urls
        URL[] urls = ((URLClassLoader) BizLauncher.class.getClassLoader()).getURLs();

        // Gets the test directory of the current class
        URL testTarget = testClass.getClass().getResource(File.separator);

        // MastBizClassLoader add testTarget
        for (URL url : urls) {
            // skip testTarget
            if (Objects.equals(testTarget, url)) {
                continue;
            }
            ReflectionUtils.addURL(getMasterClassLoader(), url);
        }

        Object newInstance = refreshBizInstance(testClass, bizClassLoader, testTarget);
        refreshMasterInstance(testClass, getMasterClassLoader(), testTarget);
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
        new TestClassInvoker() {
            @Override
            public void broadcast() {
                Transloader.DEFAULT.wrap(getSofaArkTestContextManager(newInstance)).invoke(
                        new InvocationMethodDescription("afterInstallBiz"));
            }
        }.broadcast();

        return newInstance;
    }

    /**
     * Because Plugin export，Inject testClass to TestClassloader after refreshBizInstance
     *
     * @param testClass
     * @param testClassLoader
     * @param testTarget
     * @return
     * @throws Throwable
     */
    private static Object refreshMasterInstance(Class<?> testClass, ClassLoader testClassLoader,
                                                URL testTarget) throws Throwable {
        // TestClassLoader add testTarget
        ReflectionUtils.addURL(testClassLoader, testTarget);

        // Refresh testClass
        Class<?> newTestClass = testClassLoader.loadClass(testClass.getName());

        // instance
        Object newInstance = newTestClass.newInstance();

        // Broadcast by SofaArkTestExecutionListener
        new TestClassInvoker() {
            @Override
            public void broadcast() {
                Transloader.DEFAULT.wrap(getSofaArkTestContextManager(newInstance)).invoke(
                        new InvocationMethodDescription("afterInstallMaster"));
            }
        }.broadcast();

        return newInstance;
    }

    private static ClassLoader getMasterClassLoader() {
        return MasterBizClassloaderHolder.getClassLoader();
    }

    @FunctionalInterface
    interface TestClassInvoker {
        default Object getSofaArkTestContextManager(Object newInstance) {
            return TRANSLOADER.wrap(newInstance).invoke(
                    new InvocationMethodDescription("getSofaArkTestContextManager"));
        }

        void broadcast();
    }

    interface ArkClientInvoker {
        ArkResponse installBiz(File file);

        Object getBiz(String arkName, String arkVersion);

        ClassLoader getBizClassLoader(Object biz);
    }

    static class DefaultArkClientInvoker implements ArkClientInvoker {

        private final ClassWrapper arkClientWrapper;

        DefaultArkClientInvoker() throws ClassNotFoundException {
            this.arkClientWrapper = TRANSLOADER.wrap(getMasterClassLoader().loadClass(ArkClient.class.getName()));
        }

        @Override
        public ArkResponse installBiz(File file) {
            Object clientResponse = arkClientWrapper.invoke(new InvocationMethodDescription(
                    "installBiz", new Object[] {file}));

            ResponseCode responseCode = (ResponseCode) TRANSLOADER.wrap(clientResponse)
                    .invokeCastable(new InvocationMethodDescription("getCode"));
            String errMsg = null;
            if (!Objects.equals(responseCode.name(), ResponseCode.SUCCESS.name())) {
                errMsg = (String) TRANSLOADER.wrap(clientResponse).invoke(
                        new InvocationMethodDescription("getMessage"));
            }

            return new ArkResponse(responseCode, errMsg);
        }

        @Override
        public Object getBiz(String arkName, String arkVersion) {
            Object bizManagerService = arkClientWrapper.invoke(new InvocationFieldDescription("bizManagerService"));
            Object existBiz = TRANSLOADER.wrap(bizManagerService).invoke(
                    new InvocationMethodDescription("getBiz",
                            new Object[] {arkName, arkVersion}));
            return existBiz;
        }

        @Override
        public ClassLoader getBizClassLoader(Object biz) {
            return (ClassLoader) TRANSLOADER.wrap(biz).invoke(
                    new InvocationMethodDescription("getBizClassLoader"));
        }
    }

    static class ArkResponse {
        private final ResponseCode responseCode;
        private final String       errMsg;

        ArkResponse(ResponseCode responseCode, String errMsg) {
            this.responseCode = responseCode;
            this.errMsg = errMsg;
        }
    }
}