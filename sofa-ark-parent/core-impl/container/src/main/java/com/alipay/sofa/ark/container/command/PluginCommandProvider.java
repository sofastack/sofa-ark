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

import java.util.Set;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * telnet plugin info provider
 *
 * @author joe
 * @version 0.5.0
 */
public class PluginCommandProvider implements CommandProvider {
    public static String         UNIQUEID    = "com.alipay.sofa.ark.container.command.PluginCommandProvider";
    private static String        COMMAND_PRE = "plugin";
    private PluginManagerService service     = ArkServiceContainerHolder.getContainer()
        .getService(PluginManagerService.class);

    @Override
    public boolean validate(String command) {
        if (StringUtils.isEmpty(command)) {
            return false;
        }

        String[] strs = process(command);
        if (command.length() < 2) {
            return false;
        }

        command = strs[1];

        if (!"list".equals(command) && !"info".equals(command)) {
            return false;
        } else if ("list".equals(command) && strs.length != 2) {
            return false;
        } else if ("info".equals(command) && strs.length != 3) {
            return false;
        }

        return true;
    }

    @Override
    public String handleCommand(String commandLine) {
        String[] commands = process(commandLine);
        String command = commands[1];
        StringBuilder sb = new StringBuilder();

        switch (command) {
            case "list":
                if (commands.length == 2) {
                    sb.append("plugins :\n").append(getPrint(service.getAllPluginNames(), "    "));
                    break;
                }
            case "info":
                if (commands.length == 3) {
                    Plugin plugin = service.getPluginByName(commands[2]);
                    if (plugin == null) {
                        sb.append(String.format("plugin %s not found", commands[2]));
                    } else {
                        sb.append(getPrint(plugin));
                    }
                    break;
                }
            default:
                sb.append("command error\n");
                sb.append(getHelp());
                break;
        }
        return sb.toString();
    }

    @Override
    public String getHelp(String commandMarker) {
        if (COMMAND_PRE.equals(commandMarker)) {
            return getHelp();
        } else {
            return StringUtils.EMPTY_STRING;
        }
    }

    @Override
    public String getHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("plugin list : list ark container all plugins name.\n");
        sb.append("plugin info [plugin-name] : list plugin info.\n");
        return sb.toString();
    }

    private String getPrint(Plugin plugin) {
        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getPluginName()).append(" :").append("\n");
        sb.append("    ").append("group-id : ").append(plugin.getPluginName()).append("\n");
        sb.append("    ").append("artifact-id :").append(plugin.getPluginName()).append("\n");
        sb.append("    ").append("version : ").append(plugin.getPluginName()).append("\n");
        sb.append("    ").append("priority : ").append(plugin.getPluginName()).append("\n");
        sb.append("    ").append("activator : ").append(plugin.getPluginName()).append("\n");
        sb.append("    ").append("export-packages : ").append("\n");
        sb.append(getPrint(plugin.getExportPackages(), "        "));
        sb.append("    ").append("export-classes : ").append("\n");
        sb.append(getPrint(plugin.getExportClasses(), "        "));
        sb.append("    ").append("import-packages : ").append("\n");
        sb.append(getPrint(plugin.getImportPackages(), "        "));
        sb.append("    ").append("import-classes : ").append("\n");
        sb.append(getPrint(plugin.getImportClasses(), "        "));
        sb.append("    ").append("import-resources : ").append("\n");
        sb.append(getPrint(plugin.getImportResources(), "        "));
        sb.append("    ").append("export-resources : ").append("\n");
        sb.append(getPrint(plugin.getExportResources(), "        "));
        sb.append("    ").append("export-index : ").append("\n");
        sb.append(getPrint(plugin.getExportIndex(), "        "));
        return sb.toString();
    }

    private String getPrint(Set<String> set, String tab) {
        if (set.isEmpty()) {
            return "    no plugin exist";
        }
        StringBuilder sb = new StringBuilder();
        for (String str : set) {
            sb.append(tab).append(str).append("\n");
        }
        return sb.toString();
    }

    /**
     * deal commandLine(split by blank character)
     * @param commandLine commandLine
     * @return Every valid character after processing
     */
    protected String[] process(String commandLine) {
        return commandLine.trim().split(Constants.SPACE_SPLIT);
    }
}
