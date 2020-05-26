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

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;

import java.net.URL;

/**
 * @author caojie.cj
 * @since 0.1.0
 */
public class NoneDelegateTestClassLoader extends BizClassLoader {
    public NoneDelegateTestClassLoader(String bizIdentity, URL[] urls) {
        super(bizIdentity, urls);
        // since version 1.1.0, we support load extension from ark biz, we should register biz now.
        BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);
        Biz testBiz = createTestBiz(bizIdentity);
        bizManagerService.registerBiz(testBiz);
        ((BizModel) testBiz).setBizState(BizState.ACTIVATED);
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
