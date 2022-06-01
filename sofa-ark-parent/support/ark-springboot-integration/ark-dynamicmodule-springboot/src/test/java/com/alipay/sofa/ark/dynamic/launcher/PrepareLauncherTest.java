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

import com.alipay.sofa.ark.dynamic.BaseTest;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static com.alipay.sofa.ark.dynamic.launcher.PrepareLauncher.TARGET_BIZ_PACKAGE_SH;
import static com.alipay.sofa.ark.dynamic.launcher.PrepareLauncher.TARGET_MASTER_PACKAGE_SH;
import static com.alipay.sofa.ark.dynamic.launcher.PrepareLauncher.checkAndCopySh;
import static com.alipay.sofa.ark.dynamic.launcher.PrepareLauncher.checkBizFatJar;
import static com.alipay.sofa.ark.dynamic.launcher.PrepareLauncher.checkMasterFatJAr;

/**
 * @author hanyue
 * @version : PrepareLauncherTest.java, v 0.1 2022年05月31日 3:46 PM hanyue Exp $
 */
public class PrepareLauncherTest extends BaseTest {

    @Test
    public void testCheckAndCopySh() throws IOException {
        checkAndCopySh();

        Assert.assertTrue(new File(TARGET_MASTER_PACKAGE_SH).exists());
        Assert.assertTrue(new File(TARGET_BIZ_PACKAGE_SH).exists());
    }

    @Test
    public void testCheckMasterFatJAr() throws Exception {
        checkAndCopySh();
        checkMasterFatJAr();

        File masterBizFatJar = JarUtils.getMasterBizFatJar();
        Assert.assertTrue(masterBizFatJar.exists());
    }

    @Test
    public void testCheckBizFatJar() throws Exception {
        checkAndCopySh();
        checkBizFatJar();

        File bizFatJar = JarUtils.getBizFatJar();
        Assert.assertTrue(bizFatJar.exists());
    }
}