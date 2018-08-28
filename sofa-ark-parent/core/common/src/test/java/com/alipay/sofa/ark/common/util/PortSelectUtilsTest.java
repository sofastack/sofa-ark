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
package com.alipay.sofa.ark.common.util;

import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author qilong.zql
 * @since 0.5.0
 */
public class PortSelectUtilsTest {

    @Test
    public void selectMinimumPort() {
        int port = PortSelectUtils.selectAvailablePort(1050, 100);
        AssertUtils.isTrue(port == PortSelectUtils.MIN_PORT_NUMBER, "Select Error Port.");
    }

    @Test
    public void selectUnusedPort() {
        int port = PortSelectUtils.selectAvailablePort(1234, 100);
        AssertUtils.isTrue(port == 1234, "Select Error Port.");
    }

    @Test
    public void selectUsedPort() {
        try (ServerSocket ss = new ServerSocket(1234)) {
            int port = PortSelectUtils.selectAvailablePort(1234, 100);
            AssertUtils.isTrue(port == 1235, "Select Error Port.");
        } catch (IOException ex) {
            // ignore
        }
    }

    @Test
    public void selectSinglePort() {
        try (ServerSocket ss = new ServerSocket(1234)) {
            int port = PortSelectUtils.selectAvailablePort(1234, 1);
            AssertUtils.isTrue(port == -1, "Select Error Port.");
        } catch (IOException ex) {
            // ignore
        }
    }

    @Test
    public void selectMaximumPort() {
        int port = PortSelectUtils.selectAvailablePort(65555, 10);
        AssertUtils.isTrue(port == -1, "Select Error Port.");
    }

}