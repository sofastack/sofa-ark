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
package com.alipay.sofa.ark.container.session;

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.common.thread.ThreadPoolManager;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.PortSelectUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.TelnetServerService;
import com.google.inject.Singleton;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.DEFAULT_SELECT_PORT_SIZE;
import static com.alipay.sofa.ark.spi.constant.Constants.DEFAULT_TELNET_PORT;
import static com.alipay.sofa.ark.spi.constant.Constants.TELNET_PORT_ATTRIBUTE;
import static com.alipay.sofa.ark.spi.constant.Constants.TELNET_SERVER_ENABLE;

/**
 * {@link TelnetServerService}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class StandardTelnetServerImpl implements TelnetServerService {

    private static final int  WORKER_THREAD_POOL_SIZE = 2;

    private int               port                    = -1;

    private AtomicBoolean     shutdown                = new AtomicBoolean(false);

    private boolean           enableTelnetServer      = EnvironmentUtils.getProperty(
                                                          TELNET_SERVER_ENABLE, "true")
                                                          .equalsIgnoreCase("true");

    private NettyTelnetServer nettyTelnetServer;

    public StandardTelnetServerImpl() {
        if (enableTelnetServer) {
            String telnetPort = EnvironmentUtils.getProperty(TELNET_PORT_ATTRIBUTE);
            try {
                if (!StringUtils.isEmpty(telnetPort)) {
                    port = Integer.parseInt(telnetPort);
                } else {
                    port = PortSelectUtils.selectAvailablePort(DEFAULT_TELNET_PORT,
                        DEFAULT_SELECT_PORT_SIZE);
                }
            } catch (NumberFormatException e) {
                ArkLoggerFactory.getDefaultLogger().error(
                    String.format("Invalid port in %s", telnetPort), e);
                throw new ArkRuntimeException(e);
            }
        }
    }

    @Override
    public void run() {
        AssertUtils.isTrue(port > 0, "Telnet port should be positive integer.");
        try {
            ArkLoggerFactory.getDefaultLogger().info("Listening on port: " + port);
            CommonThreadPool workerPool = new CommonThreadPool()
                .setCorePoolSize(WORKER_THREAD_POOL_SIZE).setDaemon(true)
                .setThreadPoolName(Constants.TELNET_SERVER_WORKER_THREAD_POOL_NAME);
            ThreadPoolManager.registerThreadPool(Constants.TELNET_SERVER_WORKER_THREAD_POOL_NAME,
                workerPool);
            nettyTelnetServer = new NettyTelnetServer(port, workerPool.getExecutor());
            nettyTelnetServer.open();
        } catch (InterruptedException e) {
            ArkLoggerFactory.getDefaultLogger().error("Unable to open netty telnet server.", e);
            throw new ArkRuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            try {
                if (nettyTelnetServer != null) {
                    nettyTelnetServer.close();
                    nettyTelnetServer = null;
                }
            } catch (Throwable t) {
                ArkLoggerFactory.getDefaultLogger().error(
                    "An error occurs when shutdown telnet server.", t);
                throw new ArkRuntimeException(t);
            }
        }
    }

    @Override
    public void init() throws ArkRuntimeException {
        if (enableTelnetServer) {
            run();
        } else {
            ArkLoggerFactory.getDefaultLogger().warn("Telnet server is disabled.");
        }
    }

    @Override
    public void dispose() throws ArkRuntimeException {
        if (enableTelnetServer) {
            shutdown();
        }
    }

    @Override
    public int getPriority() {
        return HIGHEST_PRECEDENCE;
    }
}
