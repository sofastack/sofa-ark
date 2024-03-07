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
package com.alipay.sofa.ark.container.service.retrieval;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yanzhu
 * @since 2.2.4
 */
public class InfoQueryCommandProvider implements CommandProvider {

    @Override
    public String getHelp() {
        return HELP_MESSAGE;
    }

    @Override
    public String handleCommand(String command) {
        return new ContainerQueryInfoCommand(command).process();
    }

    @Override
    public boolean validate(String command) {
        return new ContainerQueryInfoCommand(command).isValidate();
    }

    private static final String HELP_MESSAGE = "Query Class Info Or Other Info Command Tips:\n"
                                               + "  USAGE: plugin [option...] [className/beanName...]\n"
                                               + "  SAMPLE: ck -c com.sofa.ark.HelloWorld\n"
                                               + "  -h  Shows the help message.\n"
                                               + "  -c  Shows the class info.\n";

    class ContainerQueryInfoCommand {
        private boolean               isValidate;
        private Set<Character>        options         = new HashSet<>();
        private Set<String>           parameters      = new HashSet<>();
        private final Instrumentation instrumentation = ByteBuddyAgent.install();

        ContainerQueryInfoCommand(String command) {
            if (StringUtils.isEmpty(command)) {
                isValidate = false;
                return;
            }

            String[] syntax = command.trim().split(Constants.SPACE_SPLIT);
            if (!"ck".equals(syntax[0])) {
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
                    case 'c':
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

            // '-h' or '-c' option can not be combined with other option
            if (options.contains('h') || options.contains('c')) {
                if (options.size() > 1) {
                    isValidate = false;
                    return;
                }
            }

            // take the rest option as parameters
            while (argumentIndex < syntax.length) {
                parameters.add(syntax[argumentIndex++]);
            }

            // '-h' option need not any parameter
            if (options.contains('h') && parameters.size() > 0) {
                isValidate = false;
                return;
            }

            // if option is not 'h' or 'a', parameter should not be empty
            if (!options.contains('h') && parameters.isEmpty()) {
                isValidate = false;
                return;
            }

            // if option is 'c', parameter count should be only one.
            if (options.contains('c')) {
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
                return "Error command format. Pls type 'ck -h' to get help message\n";
            }

            if (options.contains('h')) {
                return HELP_MESSAGE;
            } else if (options.contains('c')) {
                return queryClass();
            } else {
                return HELP_MESSAGE;
            }
        }

        String queryClass() {
            if (EnvironmentUtils.isOpenSecurity()) {
                return "Cannot execute 'ck -c' command in security mode.\n";
            }
            String param = parameters.toArray(new String[] {})[0];
            Set<Class<?>> matches = new HashSet<Class<?>>();
            for (Class<?> c : instrumentation.getAllLoadedClasses()) {
                if (c == null) {
                    continue;
                }
                if (param.equals(c.getName())) {
                    matches.add(c);
                }
            }
            if (matches.isEmpty()) {
                return "Can not find class : " + param;
            }
            return createClassInfo(matches).toString();
        }

        StringBuilder createClassInfo(Set<Class<?>> classSet) {
            StringBuilder sb = new StringBuilder();
            for (Class<?> clazz : classSet) {
                ClassLoader classLoader = clazz.getClassLoader();
                if (null != ArkClient.getMasterBiz()
                    && classLoader == ArkClient.getMasterBiz().getBizClassLoader()) {
                    String classInfo = ClassInfoMethod.createClassInfo(clazz, ArkClient
                        .getMasterBiz().getIdentity());
                    sb.append(classInfo).append("\n");
                } else if (null != ArkClient.getMasterBiz()
                           && classLoader instanceof BizClassLoader) {
                    String classInfo = ClassInfoMethod.createClassInfo(clazz,
                        ((BizClassLoader) classLoader).getBizIdentity());
                    sb.append(classInfo).append("\n");
                } else {
                    String classInfo = ClassInfoMethod.createClassInfo(clazz,
                        classLoader.toString());
                    sb.append(classInfo).append("\n");
                }
            }
            return sb;
        }
    }
}
