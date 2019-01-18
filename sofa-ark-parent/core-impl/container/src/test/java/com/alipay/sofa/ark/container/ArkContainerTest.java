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

import com.alipay.sofa.ark.bootstrap.ArkLauncher;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.DEFAULT_PROFILE;

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
            new File((jarURL.getFile())))));
        arkContainer.start();
        arkContainer.stop();
        Assert.assertFalse(arkContainer.isRunning());
    }

    @Test
    public void testTelnetServerDisable() throws ArkRuntimeException {
        System.setProperty(Constants.TELNET_SERVER_ENABLE, "false");
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        ArkContainer arkContainer = (ArkContainer) ArkContainer.main(args);
        boolean enable = true;
        try (Socket socket = new Socket(InetAddress.getLocalHost(), Constants.DEFAULT_TELNET_PORT)) {
        } catch (IOException ex) {
            enable = false;
        } finally {
            arkContainer.stop();
            System.getProperties().remove(Constants.TELNET_SERVER_ENABLE);
        }
        Assert.assertFalse(enable);
    }

    @Test
    public void testTelnetServerEnable() throws ArkRuntimeException {
        System.setProperty(Constants.TELNET_SERVER_ENABLE, "true");
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        ArkContainer arkContainer = (ArkContainer) ArkContainer.main(args);
        boolean enable = true;
        try (Socket socket = new Socket(InetAddress.getLocalHost(), Constants.DEFAULT_TELNET_PORT)) {
        } catch (IOException ex) {
            enable = false;
        } finally {
            arkContainer.stop();
            System.getProperties().remove(Constants.TELNET_SERVER_ENABLE);
        }
        Assert.assertTrue(enable);
    }
}