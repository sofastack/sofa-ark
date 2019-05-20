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
package com.alipay.sofa.ark.container.session.handler;

import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.common.thread.ThreadPoolManager;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

import java.util.List;

/**
 * Collect implementation of {@link com.alipay.sofa.ark.spi.service.session.CommandProvider}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class ArkCommandHandler {

    private RegistryService registryService;

    static {
        init();
    }

    private static void init() {
        CommonThreadPool commandPool = new CommonThreadPool().setAllowCoreThreadTimeOut(true)
            .setThreadPoolName(Constants.TELNET_COMMAND_THREAD_POOL_NAME).setDaemon(true);
        ThreadPoolManager
            .registerThreadPool(Constants.TELNET_COMMAND_THREAD_POOL_NAME, commandPool);
    }

    public ArkCommandHandler() {
        registryService = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
    }

    public String handleCommand(String cmdLine) {
        if (StringUtils.isEmpty(cmdLine)) {
            return StringUtils.EMPTY_STRING;
        }
        List<ServiceReference<CommandProvider>> commandProviders = registryService
            .referenceServices(CommandProvider.class, null);
        for (ServiceReference<CommandProvider> commandService : commandProviders) {
            CommandProvider commandProvider = commandService.getService();
            if (commandProvider.validate(cmdLine)) {
                return commandProvider.handleCommand(cmdLine);
            }
        }
        return helpMessage(commandProviders);
    }

    public String helpMessage(List<ServiceReference<CommandProvider>> commandProviders) {
        StringBuilder sb = new StringBuilder();
        for (ServiceReference<CommandProvider> commandService : commandProviders) {
            CommandProvider commandProvider = commandService.getService();
            sb.append(commandProvider.getHelp());
        }
        return sb.toString();
    }

    public String promptMessage() {
        return Constants.TELNET_SESSION_PROMPT;
    }

    public String responseMessage(String cmd) {
        String commandResult = handleCommand(cmd);
        commandResult = commandResult.replace("\n", Constants.TELNET_STRING_END);
        if (StringUtils.isEmpty(commandResult)) {
            commandResult = Constants.TELNET_STRING_END;
        } else if (!commandResult.endsWith(Constants.TELNET_STRING_END)) {
            commandResult = commandResult + Constants.TELNET_STRING_END
                            + Constants.TELNET_STRING_END;
        } else if (!commandResult.endsWith(Constants.TELNET_STRING_END
            .concat(Constants.TELNET_STRING_END))) {
            commandResult = commandResult + Constants.TELNET_STRING_END;
        }
        commandResult = commandResult + promptMessage();
        return commandResult;
    }
}