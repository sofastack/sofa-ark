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

import com.alipay.sofa.ark.common.thread.ThreadPoolManager;
import com.alipay.sofa.ark.container.session.handler.TelnetProtocolHandler;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.TelnetSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * {@link TelnetSession}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class StandardTelnetSession implements TelnetSession {

    private final Socket        socket;

    private final InputStream   in;

    private final OutputStream  out;

    private volatile boolean    alive;
    /**
     * https://www.ietf.org/rfc/rfc857.txt
     * https://www.ietf.org/rfc/rfc858.txt
     * https://www.ietf.org/rfc/rfc1073.txt
     * https://www.ietf.org/rfc/rfc1091.txt
     */
    private static final byte[] NEGOTIATION_MESSAGE = new byte[] { (byte) 255, (byte) 251,
            (byte) 1, (byte) 255, (byte) 251, (byte) 3, (byte) 255, (byte) 253, (byte) 31,
            (byte) 255, (byte) 253, (byte) 24      };

    public StandardTelnetSession(Socket socket) throws IOException {
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        alive = true;
        negotiate();
    }

    @Override
    public synchronized void start() {
        TelnetProtocolHandler telnetProtocolHandler = new TelnetProtocolHandler(this);
        ThreadPoolManager.getThreadPool(Constants.TELNET_SERVER_THREAD_POOL_NAME).getExecutor()
            .execute(telnetProtocolHandler);
    }

    @Override
    public InputStream getInput() {
        return in;
    }

    @Override
    public OutputStream getOutput() {
        return out;
    }

    @Override
    public synchronized void close() {
        try {
            if (isAlive()) {
                alive = false;
                socket.close();
            }
        } catch (IOException ex) {
            // ignore
        }
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    /**
     * When a connection is built, the server will negotiate some default message with the client.
     *
     * @throws IOException
     */
    private void negotiate() throws IOException {
        out.write(NEGOTIATION_MESSAGE);
        out.flush();
    }
}