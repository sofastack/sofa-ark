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

import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

import java.util.Set;

/**
 * plugins command
 *
 * @author joe
 * @version 0.5.0
 */
public class PluginCommand implements ListCommand, InfoCommand {
    public static String         UNIQUEID  = PluginCommand.class.getName();
    private static final String  TAB       = "    ";
    private static final String  TWO_TAB   = "        ";
    private static final String  NO_EXIST  = String.format("%sno plugin exist", TAB);
    private static final String  SEPARATOR = " : ";
    private PluginManagerService service   = ArkServiceContainerHolder.getContainer().getService(
                                               PluginManagerService.class);

    @Override
    public String getHelp() {
        return "plugin list : list ark container all plugins name.\nplugin info [plugin-name] : list plugin info.";
    }

    @Override
    public String getInfo(String id) {
        Plugin plugin = service.getPluginByName(id);
        return plugin == null ? String.format("plugin %s not found", id) : getPrint(plugin);
    }

    @Override
    public String getListInfo() {
        return String.format("plugins : \n%s", getPrint(service.getAllPluginNames(), TAB));
    }

    @Override
    public String getName() {
        return "plugin";
    }

    private String getPrint(Plugin plugin) {
        StringBuilder sb = new StringBuilder();
        sb.append(plugin.getPluginName()).append(SEPARATOR).append("\n");
        sb.append(TAB).append(Constants.GROUP_ID_ATTRIBUTE).append(SEPARATOR)
            .append(plugin.getGroupId()).append("\n");
        sb.append(TAB).append(Constants.ARTIFACT_ID_ATTRIBUTE).append(SEPARATOR)
            .append(plugin.getArtifactId()).append("\n");
        sb.append(TAB).append(Constants.PLUGIN_VERSION_ATTRIBUTE).append(SEPARATOR)
            .append(plugin.getVersion()).append("\n");
        sb.append(TAB).append(Constants.PRIORITY_ATTRIBUTE).append(SEPARATOR)
            .append(plugin.getPriority()).append("\n");
        sb.append(TAB).append(Constants.ACTIVATOR_ATTRIBUTE).append(SEPARATOR)
            .append(plugin.getPluginActivator()).append("\n");
        sb.append(TAB).append(Constants.EXPORT_PACKAGES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getExportPackages(), TWO_TAB));
        sb.append(TAB).append(Constants.EXPORT_CLASSES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getExportClasses(), TWO_TAB));
        sb.append(TAB).append(Constants.IMPORT_PACKAGES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getImportPackages(), TWO_TAB));
        sb.append(TAB).append(Constants.IMPORT_CLASSES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getImportClasses(), TWO_TAB));
        sb.append(TAB).append(Constants.IMPORT_RESOURCES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getImportResources(), TWO_TAB));
        sb.append(TAB).append(Constants.EXPORT_RESOURCES_ATTRIBUTE).append(SEPARATOR).append("\n");
        sb.append(getPrint(plugin.getExportResources(), TWO_TAB));
        sb.append(TAB).append("export-index : ").append("\n");
        sb.append(getPrint(plugin.getExportIndex(), TWO_TAB));
        return sb.toString();
    }

    private String getPrint(Set<String> set, String tab) {
        if (set.isEmpty()) {
            return NO_EXIST;
        }
        StringBuilder sb = new StringBuilder();
        for (String str : set) {
            sb.append(tab).append(str).append("\n");
        }
        return sb.toString();
    }
}
