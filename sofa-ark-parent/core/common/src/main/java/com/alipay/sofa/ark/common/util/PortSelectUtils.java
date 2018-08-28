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

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author qilong.zql
 * @author khotyn
 * @since 0.5.0
 */
public class PortSelectUtils {

    /**
     * The minimum candidate port number of IPv4
     */
    public static final int MIN_PORT_NUMBER = 1100;

    /**
     * The maximum candidate port number of IPv4
     */
    public static final int MAX_PORT_NUMBER = 65535;

    /**
     * Select appropriate port among specify interval
     *
     * @param defaultPort specify the starting port
     * @param maxLength specify the size of interval
     * @return return available port
     */
    public synchronized static int selectAvailablePort(int defaultPort, int maxLength) {
        for (int i = defaultPort; i < defaultPort + maxLength; i++) {
            try {
                if (available(i)) {
                    return i;
                }
            } catch (IllegalArgumentException e) {
                // Ignore and continue
            }
        }

        return -1;
    }

    private static boolean available(int port) {
        if (port < MIN_PORT_NUMBER || port > MAX_PORT_NUMBER) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            // Do nothing
        }
        return false;
    }

}