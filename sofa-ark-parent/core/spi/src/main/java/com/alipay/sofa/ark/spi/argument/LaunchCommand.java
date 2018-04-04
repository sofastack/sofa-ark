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
package com.alipay.sofa.ark.spi.argument;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.argument.CommandArgument.*;

/**
 * command argument parsed as a launchCommand
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class LaunchCommand {

    private URL      executableArkBizJar;

    private URL[]    classpath;

    private String   entryClassName;

    private String   entryMethodName;

    private String   entryMethodDescriptor;

    private String[] launchArgs;

    private Boolean  isTestMode;

    public boolean isExecutedByCommandLine() {
        return executableArkBizJar != null;
    }

    public URL getExecutableArkBizJar() {
        return executableArkBizJar;
    }

    public LaunchCommand setExecutableArkBizJar(URL executableArkBizJar) {
        this.executableArkBizJar = executableArkBizJar;
        return this;
    }

    public URL[] getClasspath() {
        return classpath;
    }

    public LaunchCommand setClasspath(URL[] classpath) {
        this.classpath = classpath;
        return this;
    }

    public String getEntryMethodName() {
        return entryMethodName;
    }

    public LaunchCommand setEntryMethodName(String entryMethodName) {
        this.entryMethodName = entryMethodName;
        return this;
    }

    public String getEntryClassName() {
        return entryClassName;
    }

    public LaunchCommand setEntryClassName(String entryClassName) {
        this.entryClassName = entryClassName;
        return this;
    }

    public String[] getLaunchArgs() {
        return launchArgs;
    }

    public LaunchCommand setLaunchArgs(String[] launchArgs) {
        this.launchArgs = launchArgs;
        return this;
    }

    public String getEntryMethodDescriptor() {
        return entryMethodDescriptor;
    }

    public LaunchCommand setEntryMethodDescriptor(String entryMethodDescriptor) {
        this.entryMethodDescriptor = entryMethodDescriptor;
        return this;
    }

    public Boolean isTestMode() {
        return isTestMode == null ? false : isTestMode;
    }

    public LaunchCommand setTestMode(Boolean testMode) {
        isTestMode = testMode;
        return this;
    }

    public static LaunchCommand parse(String arkCommand, String[] args)
                                                                       throws MalformedURLException {
        LaunchCommand launchCommand = new LaunchCommand();

        String arkJarPrefix = String.format("%s%s=", ARK_CONTAINER_ARGUMENTS_MARK,
            FAT_JAR_ARGUMENT_KEY);
        String arkClasspathPrefix = String.format("%s%s=", ARK_CONTAINER_ARGUMENTS_MARK,
            CLASSPATH_ARGUMENT_KEY);
        String entryClassNamePrefix = String.format("%s%s=", ARK_BIZ_ARGUMENTS_MARK,
            ENTRY_CLASS_NAME_ARGUMENT_KEY);
        String entryMethodNamePrefix = String.format("%s%s=", ARK_BIZ_ARGUMENTS_MARK,
            ENTRY_METHOD_NAME_ARGUMENT_KEY);
        String entryMethodDescriptorPrefix = String.format("%s%s=", ARK_BIZ_ARGUMENTS_MARK,
            ENTRY_METHOD_DESCRIPTION_ARGUMENT_KEY);
        String testRunMode = String.format("%s%s=%s", ARK_BIZ_ARGUMENTS_MARK, BIZ_RUN_MODE,
            TEST_RUN_MODE);

        String[] arkArgs = arkCommand.split(CommandArgument.KEY_VALUE_PAIR_SPLIT);
        for (String arg : arkArgs) {
            arg = arg.trim();

            if (arg.startsWith(arkJarPrefix)) {
                String fatJarUrl = arg.substring(arkJarPrefix.length());
                launchCommand.setExecutableArkBizJar(new URL(fatJarUrl));
            }

            if (arg.startsWith(entryClassNamePrefix)) {
                String entryClassName = arg.substring(entryClassNamePrefix.length());
                launchCommand.setEntryClassName(entryClassName);
            }

            if (arg.startsWith(entryMethodNamePrefix)) {
                String entryMethodName = arg.substring(entryMethodNamePrefix.length());
                launchCommand.setEntryMethodName(entryMethodName);
            }

            if (arg.startsWith(entryMethodDescriptorPrefix)) {
                String entryMethodDescriptor = arg.substring(entryMethodDescriptorPrefix.length());
                launchCommand.setEntryMethodDescriptor(entryMethodDescriptor);
            }

            if (arg.equals(testRunMode)) {
                launchCommand.setTestMode(true);
            }

            if (arg.startsWith(arkClasspathPrefix)) {
                String classpath = arg.substring(arkClasspathPrefix.length());
                List<URL> urlList = new ArrayList<>();
                for (String url : classpath.split(CLASSPATH_SPLIT)) {
                    if (url.isEmpty()) {
                        continue;
                    }
                    urlList.add(new URL(url));
                }
                launchCommand.setClasspath(urlList.toArray(new URL[urlList.size()]));
            }

        }

        launchCommand.setLaunchArgs(args);

        return launchCommand;
    }

    public static String toString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
        }
        return sb.toString();
    }
}