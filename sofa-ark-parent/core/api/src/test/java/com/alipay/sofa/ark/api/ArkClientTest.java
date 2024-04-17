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
package com.alipay.sofa.ark.api;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.apache.commons.io.FileUtils.touch;

public class ArkClientTest {

    @Test
    public void testRecycleBizTempWorkDir() throws Throwable {
        Assert.assertFalse(ArkClient.recycleBizTempWorkDir(null));

        File fileJar = new File("/tmp/" + System.currentTimeMillis() + ".jar");
        touch(fileJar);

        Assert.assertTrue(ArkClient.recycleBizTempWorkDir(fileJar));
        Assert.assertFalse(fileJar.exists());

        File fileDir = new File("/tmp/" + System.currentTimeMillis() + "-test");
        fileDir.mkdir();
        File fileSubFile = new File(fileDir.getAbsolutePath() + "/subfile.jar");
        touch(fileSubFile);

        Assert.assertTrue(ArkClient.recycleBizTempWorkDir(fileDir));
        Assert.assertFalse(fileDir.exists());
        Assert.assertFalse(fileSubFile.exists());
    }
}
