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
package com.alipay.sofa.ark.spi.service.session;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A telnet session service provides the input and output. The input will
 * be used by ark to read in session commands. The output will be used to
 * print the results of session commands.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface TelnetSession {

    /**
     * Start to handle a session
     */
    void start();

    /**
     * Returns the input for this telnet session. This input will be used to
     * read input commands from the user of the session.
     *
     * @return this input for this telnet session.
     */
    InputStream getInput();

    /**
     * Returns this output for this telnet session. This output will be used
     * to write the results of input commands.
     *
     * @return the output for this telnet session.
     */
    OutputStream getOutput();

    /**
     * Called to free resources associated with this telnet session. For example,
     * closing the streams associated with the input and output for this session.
     */
    void close();

    /**
     * Check whether the telnet session is alive or not.
     *
     * @return  alive state
     */
    boolean isAlive();

}