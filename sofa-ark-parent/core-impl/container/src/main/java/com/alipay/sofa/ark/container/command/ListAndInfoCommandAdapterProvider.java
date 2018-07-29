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
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

import java.util.List;

/**
 * list and info command adapter
 *
 * @author joe
 * @version 0.5.0
 */
public class ListAndInfoCommandAdapterProvider implements CommandProvider {
    public static String    UNIQUEID = ListAndInfoCommandAdapterProvider.class.getName();
    private RegistryService registryService;

    public ListAndInfoCommandAdapterProvider() {
        registryService = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
    }

    @Override
    public boolean validate(String commandLine) {
        if (StringUtils.isEmpty(commandLine)) {
            return false;
        }

        String[] strs = commandLine.trim().split(Constants.SPACE_SPLIT);
        if (strs.length < 2) {
            return false;
        }

        String name = strs[0];
        String command = strs[1];

        Class<? extends Command> clazz = null;
        if (ListCommand.COMMAND.equals(command)) {
            if (strs.length != 2) {
                return false;
            }
            clazz = ListCommand.class;
        } else if (InfoCommand.COMMAND.equals(command)) {
            if (strs.length != 3) {
                return false;
            }
            clazz = InfoCommand.class;
        }
        return clazz != null && match(registryService.referenceServices(clazz), name) != null;
    }

    @Override
    public String handleCommand(String commandLine) {
        String[] commands = commandLine.trim().split(Constants.SPACE_SPLIT);
        String name = commands[0];
        String command = commands[1];

        if (ListCommand.COMMAND.equals(command)) {
            ListCommand listCommand = match(registryService.referenceServices(ListCommand.class),
                name);
            if (listCommand != null) {
                return listCommand.getListInfo();
            }
        } else if (InfoCommand.COMMAND.equals(command)) {
            InfoCommand infoCommand = match(registryService.referenceServices(InfoCommand.class),
                name);
            if (infoCommand != null) {
                return infoCommand.getInfo(commands[2]);
            }
        }
        return getHelp();
    }

    @Override
    public String getHelp(String commandMarker) {
        ListCommand listCommand = match(registryService.referenceServices(ListCommand.class),
            commandMarker);
        if (listCommand != null) {
            return listCommand.getHelp();
        }
        InfoCommand infoCommand = match(registryService.referenceServices(InfoCommand.class),
            commandMarker);
        if (infoCommand != null) {
            return infoCommand.getHelp();
        }
        return StringUtils.EMPTY_STRING;
    }

    @Override
    public String getHelp() {
        List<ServiceReference<ListCommand>> listCommandRefs = registryService
            .referenceServices(ListCommand.class);
        List<ServiceReference<InfoCommand>> infoCommandRefs = registryService
            .referenceServices(InfoCommand.class);

        StringBuilder sb = new StringBuilder();
        for (ServiceReference<ListCommand> listCommandRef : listCommandRefs) {
            sb.append(String.format("%s\n", listCommandRef.getService().getHelp()));
        }

        for (ServiceReference<InfoCommand> infoCommandRef : infoCommandRefs) {
            sb.append(String.format("%s\n", infoCommandRef.getService().getHelp()));
        }

        return sb.toString();
    }

    private <C extends Command> C match(List<ServiceReference<C>> commands, String name) {
        for (ServiceReference<C> command : commands) {
            if (name.equals(command.getService().getName())) {
                return command.getService();
            }
        }
        return null;
    }
}
