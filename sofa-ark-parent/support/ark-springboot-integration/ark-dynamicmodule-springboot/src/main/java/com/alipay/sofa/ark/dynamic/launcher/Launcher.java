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

import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContextManager;
import com.alipay.sofa.ark.dynamic.support.testng.AbstractTestNGSofaArkContextTests;

/**
 * The type Launcher.
 *
 * @author hanyue
 * @version : Launcher.java, v 0.1 2022年05月28日 上午6:05 hanyue Exp $
 */
public class Launcher {

    /**
     * Run object.
     *
     * @param testInstance the test instance
     * @return the object
     * @throws Throwable the throwable
     */
    public Object run(Object testInstance) throws Throwable {
        SofaArkTestContextManager serverlessTestContextManager = ((AbstractTestNGSofaArkContextTests) testInstance)
            .getSofaArkTestContextManager();
        serverlessTestContextManager.getSofaArkTestContext().updateState(testInstance, null, null);

        PrepareLauncher.check();
        AppLauncher.startArkContainerAndMasterBiz(serverlessTestContextManager);
        return BizLauncher.startBizAndInjectTestClasss(serverlessTestContextManager);
    }
}