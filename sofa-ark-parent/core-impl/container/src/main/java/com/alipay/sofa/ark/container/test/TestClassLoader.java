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

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.exception.ArkLoaderException;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class TestClassLoader extends BizClassLoader {

    private final ClassLoader delegateClassLoader;

    private static String[]   packageForTest = {
            // Junit
            "org.junit", "junit", "org.hamcrest",
            // TestNG
            "org.testng", "com.beust.jcommander", "bsh",
            // mockito
            "org.mockito",
            // Ark
            "com.alipay.sofa.ark.support.common",
            // tomcat
            "org.apache.catalina", "org.apache.coyote", "org.apache.juli", "org.apache.naming",
            "org.apache.tomcat", "org.apache.el", "javax" };

    private List<String>      delegateClassToAppClassLoader;

    private List<String>      delegateClassToTestClassLoader;

    public TestClassLoader(String bizIdentity, URL[] urls, ClassLoader delegate) {
        super(bizIdentity, urls, true);
        delegateClassLoader = delegate;
        // since version 1.1.0, we support load extension from ark biz, we should register biz now.
        BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
        Biz testBiz = createTestBiz(bizIdentity);
        bizManagerService.registerBiz(testBiz);
        ((BizModel) testBiz).setBizState(BizState.ACTIVATED);
        super.setBizModel((BizModel) testBiz);
    }

    @Override
    protected Class<?> loadClassInternal(String name, boolean resolve) throws ArkLoaderException {
        if (isDelegateToAppClassLoader(ClassUtils.getPackageName(name))) {
            try {
                return delegateClassLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw new ArkLoaderException(String.format(
                    "[TestClass Loader] %s : can not load class: %s", getBizIdentity(), name));
            }
        } else {
            return super.loadClassInternal(name, resolve);
        }
    }

    private boolean isDelegateToAppClassLoader(String name) {
        if (delegateClassToAppClassLoader == null) {
            String classes = EnvironmentUtils.getProperty(
                Constants.FORCE_DELEGATE_TO_APP_CLASSLOADER, Constants.EMPTY_STR);
            delegateClassToAppClassLoader = Arrays.asList(classes.split(Constants.COMMA_SPLIT));
        }
        if (delegateClassToTestClassLoader == null) {
            String classes = EnvironmentUtils.getProperty(
                Constants.FORCE_DELEGATE_TO_TEST_CLASSLOADER, Constants.EMPTY_STR);
            delegateClassToTestClassLoader = Arrays.asList(classes.split(Constants.COMMA_SPLIT));
        }

        for (String pkg : delegateClassToAppClassLoader) {
            if (!StringUtils.isEmpty(pkg) && name.startsWith(pkg)) {
                return true;
            }
        }

        for (String pkg : delegateClassToTestClassLoader) {
            if (!StringUtils.isEmpty(pkg) && name.startsWith(pkg)) {
                return false;
            }
        }

        for (String pkg : packageForTest) {
            if (name.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }

    private Biz createTestBiz(String bizIdentity) {
        String[] bizNameAndVersion = bizIdentity.split(":");
        if (bizNameAndVersion.length != 2) {
            throw new ArkRuntimeException("error bizIdentity format.");
        }
        BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
        Biz testBiz = new BizModel().setBizName(bizNameAndVersion[0])
            .setBizVersion(bizNameAndVersion[1]).setClassLoader(this).setDenyImportPackages("")
            .setDenyImportClasses("").setDenyImportResources("").setBizState(BizState.RESOLVED);
        bizManagerService.registerBiz(testBiz);
        return testBiz;
    }
}