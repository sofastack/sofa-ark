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
package com.alipay.sofa.ark.container.service.biz;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.thread.ThreadPoolManager;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class BizCommandProvider implements CommandProvider {

    @ArkInject
    private BizManagerService bizManagerService;

    @Override
    public String getHelp() {
        return HELP_MESSAGE;
    }

    @Override
    public String handleCommand(String command) {
        return new BizCommand(command).process();
    }

    @Override
    public boolean validate(String command) {
        return new BizCommand(command).isValidate();
    }

    static final String HELP_MESSAGE = "Biz Command Tips:\n"
                                       + "  USAGE: biz [option...] [arguments...]\n"
                                       + "  SAMPLE: biz -m bizIdentityA bizIdentityB.\n"
                                       + "  -h  Shows the help message.\n"
                                       + "  -a  Shows all biz.\n"
                                       + "  -m  Shows the meta info of specified bizIdentity.\n"
                                       + "  -s  Shows the service info of specified bizIdentity.\n"
                                       + "  -d  Shows the detail info of specified bizIdentity.\n"
                                       + "  -i  Install biz of specified bizIdentity or bizUrl.\n"
                                       + "  -u  Uninstall biz of specified bizIdentity.\n"
                                       + "  -o  Switch biz of specified bizIdentity.\n";

    class BizCommand {
        private boolean        isValidate;
        private Set<Character> options    = new HashSet<>();
        private Set<String>    parameters = new HashSet<>();

        BizCommand(String command) {
            if (StringUtils.isEmpty(command)) {
                isValidate = false;
                return;
            }

            String[] syntax = command.trim().split(Constants.SPACE_SPLIT);
            if (!"biz".equals(syntax[0])) {
                isValidate = false;
                return;
            }

            int argumentIndex = syntax.length;
            // fetch all options and allow repetition
            for (int i = 1; i < syntax.length; ++i) {
                if (!syntax[i].startsWith("-")) {
                    argumentIndex = i;
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

            // only the following option can be allowed.
            for (Character option : options) {
                switch (option) {
                    case 'h':
                    case 'a':
                    case 'm':
                    case 's':
                    case 'd':
                    case 'i':
                    case 'u':
                    case 'o':
                        continue;
                    default:
                        isValidate = false;
                        return;
                }
            }

            // check whether options is empty
            if (options.isEmpty()) {
                isValidate = false;
                return;
            }

            // '-h' or '-a' option can not be combined with other option, such as '-m'
            if (options.contains('h') || options.contains('a') || options.contains('i')
                || options.contains('u') || options.contains('o')) {
                if (options.size() > 1) {
                    isValidate = false;
                    return;
                }
            }

            // take the rest option as parameters
            while (argumentIndex < syntax.length) {
                parameters.add(syntax[argumentIndex++]);
            }

            // '-h' or '-a' option need not any parameter
            if ((options.contains('h') || options.contains('a')) && parameters.size() > 0) {
                isValidate = false;
                return;
            }

            // if option is not 'h' or 'a', parameter should not be empty
            if (!(options.contains('h') || options.contains('a')) && parameters.isEmpty()) {
                isValidate = false;
                return;
            }

            // if option is 'i' or 'u' or 'o', parameter count should be only one.
            if (options.contains('i') || options.contains('u') || options.contains('o')) {
                if (parameters.size() > 1) {
                    isValidate = false;
                    return;
                }
            }

            isValidate = true;
        }

        boolean isValidate() {
            return isValidate;
        }

        String process() {
            if (!isValidate) {
                return "Error command format. Pls type 'biz -h' to get help message\n";
            }
            StringBuilder sb = new StringBuilder(512);

            if (options.contains('h')) {
                return HELP_MESSAGE;
            } else if (options.contains('a')) {
                return bizList();
            } else if (options.contains('i')) {
                return installBiz();
            } else if (options.contains('u')) {
                return uninstallBiz();
            } else if (options.contains('o')) {
                return switchBiz();
            } else {
                Set<String> candidates = bizManagerService.getAllBizIdentities();
                boolean matched = false;
                for (String pattern : parameters) {
                    for (String candidate : candidates) {
                        if (Pattern.matches(pattern, candidate)) {
                            matched = true;
                            sb.append(bizInfo(candidate));
                        }
                    }
                }
                if (!matched) {
                    sb.append("no matched biz candidates.").append("\n");
                }
            }
            return sb.toString();
        }

        String bizList() {
            List<Biz> bizList = bizManagerService.getBizInOrder();
            StringBuilder sb = new StringBuilder(128);
            for (Biz biz : bizList) {
                sb.append(biz.getIdentity()).append(Constants.STRING_COLON)
                    .append(biz.getBizState()).append("\n");
            }
            sb.append("biz count = ").append(bizList.size()).append("\n");
            return sb.toString();
        }

        String installBiz() {

            if (EnvironmentUtils.isOpenSecurity()) {
                return "Cannot execute install command in security mode.\n";
            }

            if (!isReadyInstall()) {
                return "Exists some biz whose state is neither 'activated' nor 'deactivated'.\n";
            }
            ThreadPoolManager.getThreadPool(Constants.TELNET_COMMAND_THREAD_POOL_NAME)
                .getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        BizOperation bizOperation = new BizOperation()
                            .setOperationType(BizOperation.OperationType.INSTALL);
                        String param = parameters.toArray(new String[] {})[0];
                        try {
                            URL url = new URL(param);
                            bizOperation.putParameter(Constants.CONFIG_BIZ_URL, param);
                        } catch (Throwable t) {
                            String[] nameAndVersion = param.split(Constants.STRING_COLON);
                            if (nameAndVersion.length != 2) {
                                ArkLoggerFactory.getDefaultLogger().error(
                                    "Invalid telnet biz install command {}", param);
                                return;
                            }
                            bizOperation.setBizName(nameAndVersion[0]).setBizVersion(
                                nameAndVersion[1]);
                        }
                        try {
                            ArkClient.installOperation(bizOperation);
                        } catch (Throwable throwable) {
                            ArkLoggerFactory.getDefaultLogger().error(
                                "Fail to process telnet install command: " + param, throwable);
                        }
                    }
                });
            return "Start to process install command now, pls wait and check.";
        }

        String uninstallBiz() {

            if (EnvironmentUtils.isOpenSecurity()) {
                return "Cannot execute uninstall command in security mode.\n";
            }

            ThreadPoolManager.getThreadPool(Constants.TELNET_COMMAND_THREAD_POOL_NAME)
                .getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        String param = parameters.toArray(new String[] {})[0];
                        String[] nameAndVersion = param.split(Constants.STRING_COLON);
                        if (nameAndVersion.length != 2) {
                            ArkLoggerFactory.getDefaultLogger().error(
                                "Invalid telnet biz uninstall command {}", param);
                            return;
                        }
                        try {
                            ArkClient.uninstallBiz(nameAndVersion[0], nameAndVersion[1]);
                        } catch (Throwable throwable) {
                            ArkLoggerFactory.getDefaultLogger().error(
                                "Fail to process telnet uninstall command: " + param, throwable);
                        }
                    }
                });
            return "Start to process uninstall command now, pls wait and check.";
        }

        String switchBiz() {
            ThreadPoolManager.getThreadPool(Constants.TELNET_COMMAND_THREAD_POOL_NAME)
                .getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        String param = parameters.toArray(new String[] {})[0];
                        String[] nameAndVersion = param.split(Constants.STRING_COLON);
                        if (nameAndVersion.length != 2) {
                            ArkLoggerFactory.getDefaultLogger().error(
                                "Invalid telnet biz switch command {}", param);
                            return;
                        }
                        try {
                            ArkClient.switchBiz(nameAndVersion[0], nameAndVersion[1]);
                        } catch (Throwable throwable) {
                            ArkLoggerFactory.getDefaultLogger().error(
                                "Fail to process telnet switch command: " + param, throwable);
                        }
                    }
                });
            return "Start to process switch command now, pls wait and check.";
        }

        String bizInfo(String bizIdentity) {
            Biz biz = bizManagerService.getBizByIdentity(bizIdentity);
            if (biz == null) {
                return "Invalid bizIdentity: " + bizIdentity + "\n";
            }
            StringBuilder sb = new StringBuilder(256);
            // print biz meta info
            if (options.contains('m')) {
                sb.append("BizName:                  ").append(biz.getBizName()).append("\n");
                sb.append("Version:                  ").append(biz.getBizVersion()).append("\n");
                sb.append("Priority:                 ").append(biz.getPriority()).append("\n");
                sb.append("MainClass:                ").append(biz.getMainClass()).append("\n");
                sb.append("WebContextPath:           ").append(biz.getWebContextPath())
                    .append("\n");
                sb.append("Deny Import Packages:     ")
                    .append(StringUtils.setToStr(biz.getDenyImportPackages(), ",", "\\"))
                    .append("\n");
                sb.append("Deny Import Classes:      ")
                    .append(StringUtils.setToStr(biz.getDenyImportClasses(), ",", "\\"))
                    .append("\n");
                sb.append("Deny Import Resources:    ")
                    .append(StringUtils.setToStr(biz.getDenyImportResources(), ",", "\\"))
                    .append("\n");
            }

            // print biz service info
            if (options.contains('s')) {
                // TODO
            }

            // print biz detail info
            if (options.contains('d')) {
                sb.append("ClassLoader: ").append(biz.getBizClassLoader()).append("\n");
                sb.append("ClassPath:   ").append(join(biz.getClassPath(), ",")).append("\n");
            }
            sb.append("\n");
            return sb.toString();
        }

        public boolean isReadyInstall() {
            for (Biz biz : bizManagerService.getBizInOrder()) {
                if (biz.getBizState() != BizState.ACTIVATED
                    && biz.getBizState() != BizState.DEACTIVATED) {
                    return false;
                }
            }
            return true;
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
