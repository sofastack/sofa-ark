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

/**
 * Answer a string (may be as many lines as you like) with help
 * texts that explain the command.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface CommandProvider {
    /**
     * Get Command Help Message
     * @return
     */
    String getHelp();

    /**
     * Get Specified Command Help Message
     * @param commandMarker Specified Command
     * @return
     */
    String getHelp(String commandMarker);

    /**
     * Validate whether command is valid
     * @param command
     * @return
     */
    boolean validate(String command);

    /**
     * Handler Specified Command
     * @param command
     * @return
     */
    String handleCommand(String command);
}