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
package com.alipay.sofa.ark.container;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkContainerTest extends BaseTest {

    private URL jarURL = ArkContainerTest.class.getClassLoader().getResource("test.jar");

    @Override
    public void before() {
        // no op
    }

    @Override
    public void after() {
        // no op
    }

    @Test
    public void testStart() throws ArkRuntimeException {
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        ArkContainer arkContainer = (ArkContainer) ArkContainer.main(args);
        Assert.assertTrue(arkContainer.isStarted());
        arkContainer.stop();
    }

    @Test
    public void testStop() throws Exception {
        ArkContainer arkContainer = new ArkContainer(new ExecutableArkBizJar(new JarFileArchive(
            FileUtils.file((jarURL.getFile())))));
        arkContainer.start();
        arkContainer.stop();
        Assert.assertFalse(arkContainer.isRunning());
    }

    @Test
    public void testArkServiceLoader() throws ArkRuntimeException {
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        ArkContainer arkContainer = (ArkContainer) ArkContainer.main(args);
        Assert.assertNotNull(ArkServiceLoader.getExtensionLoaderService());
        arkContainer.stop();
    }
}