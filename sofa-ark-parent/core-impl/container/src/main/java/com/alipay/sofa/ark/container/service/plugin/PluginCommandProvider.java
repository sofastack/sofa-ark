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
package com.alipay.sofa.ark.container.service.plugin;

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class PluginCommandProvider implements CommandProvider {

    @ArkInject
    private PluginManagerService pluginManagerService;

    @Override
    public String getHelp() {
        return HELP_MESSAGE;
    }

    @Override
    public String handleCommand(String command) {
        return new PluginCommand(command).process();
    }

    @Override
    public boolean validate(String command) {
        return new PluginCommand(command).isValidate();
    }

    private static final String HELP_MESSAGE = "Plugin Command Tips:\n"
                                               + "  USAGE: plugin [option...] [pluginName...]\n"
                                               + "  SAMPLE: plugin -m plugin-A plugin-B\n"
                                               + "  -h  Shows the help message.\n"
                                               + "  -a  Shows all plugin name.\n"
                                               + "  -m  Shows the meta info of specified pluginName.\n"
                                               + "  -s  Shows the service info of specified pluginName.\n"
                                               + "  -d  Shows the detail info of specified pluginName.\n";

    class PluginCommand {
        private boolean        isValidate;
        private Set<Character> options    = new HashSet<>();
        private Set<String>    parameters = new HashSet<>();

        PluginCommand(String command) {
            if (StringUtils.isEmpty(command)) {
                isValidate = false;
                return;
            }

            String[] syntax = command.trim().split(Constants.SPACE_SPLIT);
            if (!"plugin".equals(syntax[0])) {
                isValidate = false;
                return;
            }

            int pluginNameIndex = syntax.length;
            // fetch all options and allow repetition
            for (int i = 1; i < syntax.length; ++i) {
                if (!syntax[i].startsWith("-")) {
                    pluginNameIndex = i;
                    break;
                }
                if (syntax[i].startsWith("-") && syntax[i].length() == 1) {
                    isValidate = false;
                    return;
                }
                for (int j = 1; j < syntax[i].length(); ++j) {
                    options.add(syntax[i].charAt(j));
                }
            }

            // only four option can be allowed.
            for (Character option : options) {
                switch (option) {
                    case 'h':
                    case 'a':
                    case 'm':
                    case 's':
                    case 'd':
                        continue;
                    default:
                        isValidate = false;
                        return;
                }
            }

            // '-h' or '-a' option can not be combined with other option, such as '-m'
            if ((options.contains('h') || options.contains('a')) && options.size() > 1) {
                isValidate = false;
                return;
            }

            // take the rest option as pluginName parameters
            while (pluginNameIndex < syntax.length) {
                parameters.add(syntax[pluginNameIndex++]);
            }

            // '-h' or '-a' option need not pluginName parameter
            if ((options.contains('h') || options.contains('a')) && parameters.size() > 0) {
                isValidate = false;
                return;
            }

            // no parameter is needed when no options
            if (options.isEmpty()) {
                isValidate = false;
                return;
            }

            // if option is not 'h' or 'a', parameter should not be empty
            if (!(options.contains('h') || options.contains('a')) && !options.isEmpty()
                && parameters.isEmpty()) {
                isValidate = false;
                return;
            }

            isValidate = true;
        }

        boolean isValidate() {
            return isValidate;
        }

        String process() {
            if (!isValidate) {
                return "Error command format. Pls type 'plugin -h' to get help message\n";
            }
            StringBuilder sb = new StringBuilder(512);
            // print plugin command help message
            if (options.contains('h')) {
                sb.append(getHelp());
            } else if (options.contains('a')) {
                return pluginList();
            } else {
                Set<String> candidates = pluginManagerService.getAllPluginNames();
                boolean matched = false;
                for (String pattern : parameters) {
                    for (String candidate : candidates) {
                        if (Pattern.matches(pattern, candidate)) {
                            matched = true;
                            sb.append(pluginInfo(candidate));
                        }
                    }
                }
                if (!matched) {
                    sb.append("no matched plugin candidates.").append("\n");
                }
            }
            return sb.toString();
        }

        String pluginList() {
            Set<String> pluginNames = pluginManagerService.getAllPluginNames();
            StringBuilder sb = new StringBuilder(128);
            if (pluginNames.isEmpty()) {
                sb.append("no plugins.").append("\n");
            } else {
                for (String pluginName : pluginNames) {
                    sb.append(pluginName).append("\n");
                }
            }
            sb.append("plugin count = ").append(pluginNames.size()).append("\n");
            return sb.toString();
        }

        String pluginInfo(String pluginName) {
            Plugin plugin = pluginManagerService.getPluginByName(pluginName);
            StringBuilder sb = new StringBuilder(256);
            // print plugin meta info
            if (options.contains('m')) {
                sb.append("PluginName:       ").append(pluginName).append("\n");
                sb.append("Version:          ").append(plugin.getVersion()).append("\n");
                sb.append("Priority:         ").append(plugin.getPriority()).append("\n");
                sb.append("Activator:        ").append(plugin.getPluginActivator()).append("\n");
                sb.append("Export Packages:  ")
                    .append(StringUtils.setToStr(plugin.getExportPackages(), ",", "\\"))
                    .append("\n");
                sb.append("Import Packages:  ")
                    .append(StringUtils.setToStr(plugin.getImportPackages(), ",", "\\"))
                    .append("\n");
                sb.append("Export Classes:   ")
                    .append(StringUtils.setToStr(plugin.getExportClasses(), ",", "\\"))
                    .append("\n");
                sb.append("Import Classes:   ")
                    .append(StringUtils.setToStr(plugin.getImportClasses(), ",", "\\"))
                    .append("\n");
                sb.append("Export Resources: ")
                    .append(StringUtils.setToStr(plugin.getExportResources(), ",", "\\"))
                    .append("\n");
                sb.append("Import Resources: ")
                    .append(StringUtils.setToStr(plugin.getImportResources(), ",", "\\"))
                    .append("\n");
            }
            // print plugin service info
            if (options.contains('s')) {
                // TODO
            }
            // print plugin detail info
            if (options.contains('d')) {
                sb.append("GroupId:     ").append(plugin.getGroupId()).append("\n");
                sb.append("ArtifactId:  ").append(plugin.getArtifactId()).append("\n");
                sb.append("Version:     ").append(plugin.getVersion()).append("\n");
                sb.append("URL:         ").append(plugin.getPluginURL()).append("\n");
                sb.append("ClassLoader: ").append(plugin.getPluginClassLoader()).append("\n");
                sb.append("ClassPath:   ").append(join(plugin.getClassPath(), ",")).append("\n");
            }
            sb.append("\n");
            return sb.toString();
        }

        String join(URL[] urls, String separator) {
            Set<String> set = new HashSet<>();
            if (urls != null) {
                for (URL url : urls) {
                    set.add(url.getPath());
                }
            }
            return StringUtils.setToStr(set, separator, "\\");
        }
    }
}