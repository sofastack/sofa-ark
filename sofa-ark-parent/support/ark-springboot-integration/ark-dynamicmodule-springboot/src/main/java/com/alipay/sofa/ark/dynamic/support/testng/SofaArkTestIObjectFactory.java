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
package com.alipay.sofa.ark.dynamic.support.testng;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.dynamic.common.execption.InstantiateException;
import com.alipay.sofa.ark.dynamic.launcher.Launcher;
import org.testng.internal.ObjectFactoryImpl;

import java.lang.reflect.Constructor;

/**
 * Test runner params:
 * -objectfactory com.alipay.sofa.ark.dynamic.testng.SofaArkTestIObjectFactory
 *
 * @author hanyue
 * @version : SofaArkTestIObjectFactory.java, v 0.1 2022年05月07日 下午4:41 hanyue Exp $
 */
public class SofaArkTestIObjectFactory extends ObjectFactoryImpl {

    @Override
    public synchronized Object newInstance(Constructor constructor, Object... params) {
        if (params.length > 0) {
            throw new IllegalArgumentException("Test class can only have no-parameter constructor.");
        }
        Object testInstance = super.newInstance(constructor, params);
        if (!supports(testInstance)) {
            return testInstance;
        }

        try {
            Launcher launcher = new Launcher();
            return launcher.run(testInstance);
        } catch (Throwable ex) {
            throw new InstantiateException(ex);
        }
    }

    private boolean supports(Object instance) {
        return instance instanceof AbstractTestNGSofaArkContextTests && ArkConfigs.isEmbedEnable();
    }
}
