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
package com.alipay.sofa.ark.container.command;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * AbstractCommandProvider, provide common features.
 *
 * @author joe
 * @version 2018.07.20 11:30
 */
public abstract class AbstractCommandProvider implements CommandProvider {

    @Override
    public boolean validate(String command) {
        if (StringUtils.isEmpty(command)) {
            return false;
        }

        String[] strs = process(command);
        command = strs[0];
        return command.equals(getCommandPre());
    }

    @Override
    public String handleCommand(String command) {
        String[] commands = process(command);
        if (commands.length < 2) {
            return getHelp(command);
        }

        return handleCommandLine(commands);
    }

    @Override
    public String getHelp(String commandMarker) {
        if (getCommandPre().equals(commandMarker)) {
            return getHelp();
        } else {
            return null;
        }
    }

    /**
     * deal commandLine(split by blank character)
     * @param commandLine commandLine
     * @return Every valid character after processing
     */
    protected String[] process(String commandLine) {
        return commandLine.trim().split(Constants.SPACE_SPLIT);
    }

    /**
     * the command prefix that the provider can handle
     * @return pre
     */
    protected abstract String getCommandPre();

    /**
     * deal command array
     * @param commands command array
     * @return result
     */
    protected abstract String handleCommandLine(String[] commands);
}
