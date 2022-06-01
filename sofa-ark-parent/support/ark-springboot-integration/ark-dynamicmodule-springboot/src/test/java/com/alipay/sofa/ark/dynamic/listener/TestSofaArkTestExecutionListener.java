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

import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import org.springframework.context.ApplicationContext;
import org.testng.Assert;

import java.io.File;

/**
 * @author hanyue
 * @version : TestSofaArkTestExecutionListener.java, v 0.1 2022年05月31日 3:15 PM hanyue Exp $
 */
public class TestSofaArkTestExecutionListener implements SofaArkTestExecutionListener {

    @Override
    public void beforeInstallMaster(SofaArkTestContext testContext, ClassLoader appClassLoader) {
        Object attribute = testContext.getAttribute(SofaArkTestConstants.MASTER_FAT_JAR);
        Assert.assertTrue(attribute instanceof File);
        File file = (File) attribute;
        Assert.assertTrue(file.exists());

        ApplicationContext applicationContext = testContext.getApplicationContext();
        Assert.assertNull(applicationContext);

        Assert.assertEquals(getClass().getClassLoader(), appClassLoader);
    }

    @Override
    public void beforeInstallBiz(SofaArkTestContext testContext, ClassLoader masterClassLoader) {
        Object attribute = testContext.getAttribute(SofaArkTestConstants.BIZ_FAT_JAR);
        Assert.assertTrue(attribute instanceof File);
        File fIle = (File) attribute;
        Assert.assertTrue(fIle.exists());

        ApplicationContext applicationContext = testContext.getApplicationContext();
        Object sofaArkTestBean = applicationContext.getBean("sofaArkTestBean");
        Assert.assertNotNull(sofaArkTestBean);

        Assert.assertEquals(getClass().getClassLoader(), masterClassLoader);
    }

    @Override
    public void afterInstallBiz(SofaArkTestContext testContext, ClassLoader bizClassLoader) {
        ApplicationContext applicationContext = testContext.getApplicationContext();
        Object sofaArkTestBean = applicationContext.getBean("sofaArkTestBean");
        Assert.assertNotNull(sofaArkTestBean);
    }
}