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

    public ArkCommandHandler() {
        registryService = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
    }

    public String handleCommand(String cmdLine) {
        if (StringUtils.isEmpty(cmdLine)) {
            return StringUtils.EMPTY_STRING;
        }
        List<ServiceReference<CommandProvider>> commandProviders = registryService
            .referenceServices(CommandProvider.class);
        for (ServiceReference<CommandProvider> commandService : commandProviders) {
            CommandProvider commandProvider = commandService.getService();
            if (commandProvider.validate(cmdLine)) {
                return commandProvider.handleCommand(cmdLine);
            }
        }
        return helpMessage(commandProviders, cmdLine);
    }

    public String helpMessage(List<ServiceReference<CommandProvider>> commandProviders,
                              String cmdLine) {
        String[] phrases = cmdLine.trim().split(Constants.SPACE_SPLIT);
        StringBuilder sb = new StringBuilder();
        if (phrases.length > 1 && "help".equals(phrases[0])) {
            for (ServiceReference<CommandProvider> commandService : commandProviders) {
                CommandProvider commandProvider = commandService.getService();
                String response = commandProvider.getHelp(phrases[1]);
                if (response != null) {
                    sb.append(response);
                }
            }
        } else {
            for (ServiceReference<CommandProvider> commandService : commandProviders) {
                CommandProvider commandProvider = commandService.getService();
                sb.append(commandProvider.getHelp());
            }
        }
        return sb.toString();
    }
}