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

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.common.thread.ThreadPoolManager;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.PortSelectUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.session.handler.TelnetProtocolHandler;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.TelnetServerService;
import com.google.inject.Singleton;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * {@link TelnetServerService}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class StandardTelnetServerImpl implements TelnetServerService {

    private static final ArkLogger LOGGER                  = ArkLoggerFactory.getDefaultLogger();

    private Selector               selector                = null;

    private ServerSocketChannel    acceptorSvr             = null;

    private String                 host                    = null;

    private int                    port                    = -1;

    private AtomicBoolean          shutdown                = new AtomicBoolean(false);

    private boolean                enableTelnetServer      = EnvironmentUtils.getProperty(
                                                               TELNET_SERVER_ENABLE, "true")
                                                               .equalsIgnoreCase("true");

    private final int              WORKER_THREAD_POOL_SIZE = 1;
    private final int              SELECT_TIME_GAP         = 1000;

    public StandardTelnetServerImpl() {
        if (enableTelnetServer) {
            String telnetValue = EnvironmentUtils.getProperty(TELNET_PORT_ATTRIBUTE);
            try {
                CommonThreadPool workerPool = new CommonThreadPool()
                    .setCorePoolSize(WORKER_THREAD_POOL_SIZE).setDaemon(true)
                    .setThreadPoolName(Constants.TELNET_SERVER_WORKER_THREAD_POOL_NAME);
                ThreadPoolManager.registerThreadPool(
                    Constants.TELNET_SERVER_WORKER_THREAD_POOL_NAME, workerPool);
                if (!StringUtils.isEmpty(telnetValue)) {
                    parseHostAndPort(telnetValue);
                } else {
                    port = PortSelectUtils.selectAvailablePort(DEFAULT_TELNET_PORT,
                        DEFAULT_SELECT_PORT_SIZE);
                }
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("Invalid host/port in %s", telnetValue), e);
                throw new ArkException(e);
            }
        }
    }

    @Override
    public void run() {
        AssertUtils.isTrue(port > 0, "Telnet port should be positive integer.");
        try {
            selector = Selector.open();
            acceptorSvr = ServerSocketChannel.open();
            if (host == null) {
                acceptorSvr.socket().bind(new InetSocketAddress(port));
            } else {
                acceptorSvr.socket().bind(new InetSocketAddress(InetAddress.getByName(host), port));
            }
            acceptorSvr.configureBlocking(false);
            acceptorSvr.register(selector, SelectionKey.OP_ACCEPT);
            LOGGER.info("Listening on port: "
                        + Integer.toString(acceptorSvr.socket().getLocalPort()));

            Runnable action = new Runnable() {
                @Override
                public void run() {
                    while (!shutdown.get()) {
                        try {
                            selector.select(SELECT_TIME_GAP);
                            Set<SelectionKey> selectionKeys = selector.selectedKeys();
                            Iterator<SelectionKey> it = selectionKeys.iterator();
                            while (it.hasNext()) {
                                SelectionKey key = it.next();
                                it.remove();
                                try {
                                    handlerSelectionKey(key);
                                } catch (Throwable t) {
                                    if (key != null) {
                                        key.cancel();
                                        if (key.channel() != null) {
                                            key.channel().close();
                                        }
                                    }
                                    LOGGER.error("An error occurs in telnet session.", t);
                                }
                            }
                        } catch (Throwable t) {
                            if (!shutdown.get()) {
                                LOGGER.error("An error occurs in telnet server.", t);
                            }
                        }
                    }
                }
            };

            ThreadPoolManager.getThreadPool(Constants.TELNET_SERVER_WORKER_THREAD_POOL_NAME)
                .getExecutor().execute(action);
        } catch (IOException e) {
            LOGGER.error("Unable to open telnet.", e);
            throw new ArkException(e);
        }
    }

    private void handlerSelectionKey(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            return;
        } else if (key.isAcceptable()) {
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ, new TelnetProtocolHandler(sc));
            sc.write(ByteBuffer.wrap(TelnetProtocolHandler.NEGOTIATION_MESSAGE));
        } else if (key.isReadable()) {
            TelnetProtocolHandler telnetProtocolHandler = (TelnetProtocolHandler) key.attachment();
            telnetProtocolHandler.handle();
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            try {
                acceptorSvr.close();
                selector.close();
            } catch (Throwable t) {
                LOGGER.error("An error occurs when shutdown telnet server.", t);
                throw new ArkException(t);
            }
        }
    }

    private void parseHostAndPort(String telnetValue) {
        int index = telnetValue.lastIndexOf(STRING_COLON);
        if (index > -1) {
            host = telnetValue.substring(0, index);
        }

        port = Integer.parseInt(telnetValue.substring(index + 1));
    }

    @Override
    public void init() throws ArkException {
        if (enableTelnetServer) {
            run();
        } else {
            LOGGER.warn("Telnet server is disabled.");
        }
    }

    @Override
    public void dispose() throws ArkException {
        if (enableTelnetServer) {
            shutdown();
        }
    }

    @Override
    public int getPriority() {
        return HIGHEST_PRECEDENCE;
    }
}