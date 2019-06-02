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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.argument.CommandArgument.ARK_BIZ_ARGUMENTS_MARK;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.CLASSPATH_ARGUMENT_KEY;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.CLASSPATH_SPLIT;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.ENTRY_CLASS_NAME_ARGUMENT_KEY;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.ENTRY_METHOD_NAME_ARGUMENT_KEY;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.FAT_JAR_ARGUMENT_KEY;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.PROFILE;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.PROFILE_SPLIT;
import static com.alipay.sofa.ark.spi.argument.CommandArgument.VM_PROFILE;
import static com.alipay.sofa.ark.spi.constant.Constants.DEFAULT_PROFILE;

/**
 * command argument parsed as a launchCommand
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class LaunchCommand {

    private URL      executableArkBizJar;

    private URL[]    classpath;

    /**
     * the following two configs are mainly used by bootstrap ark biz at startup of IDE.
     */
    private String   entryClassName;
    private String   entryMethodName;

    private String[] launchArgs;

    private String[] profiles;

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

    public String[] getProfiles() {
        if (profiles != null) {
            return profiles;
        }
        String profileVMArgs = System.getProperty(VM_PROFILE);
        return profileVMArgs == null ? new String[] { DEFAULT_PROFILE } : profileVMArgs
            .split(PROFILE_SPLIT);
    }

    public LaunchCommand setProfiles(String[] profiles) {
        this.profiles = profiles;
        return this;
    }

    public static LaunchCommand parse(String[] args) throws MalformedURLException {
        LaunchCommand launchCommand = new LaunchCommand();

        String arkJarPrefix = String.format("%s%s=", ARK_CONTAINER_ARGUMENTS_MARK,
            FAT_JAR_ARGUMENT_KEY);
        String arkClasspathPrefix = String.format("%s%s=", ARK_CONTAINER_ARGUMENTS_MARK,
            CLASSPATH_ARGUMENT_KEY);
        String entryClassNamePrefix = String.format("%s%s=", ARK_BIZ_ARGUMENTS_MARK,
            ENTRY_CLASS_NAME_ARGUMENT_KEY);
        String entryMethodNamePrefix = String.format("%s%s=", ARK_BIZ_ARGUMENTS_MARK,
            ENTRY_METHOD_NAME_ARGUMENT_KEY);
        String arkConfigProfilePrefix = String.format("%s%s=", ARK_CONTAINER_ARGUMENTS_MARK,
            PROFILE);

        List<String> arguments = new ArrayList<>();
        for (String arg : args) {
            arg = arg.trim();
            if (arg.startsWith(arkJarPrefix)) {
                String fatJarUrl = arg.substring(arkJarPrefix.length());
                launchCommand.setExecutableArkBizJar(new URL(fatJarUrl));
            } else if (arg.startsWith(entryClassNamePrefix)) {
                String entryClassName = arg.substring(entryClassNamePrefix.length());
                launchCommand.setEntryClassName(entryClassName);
            } else if (arg.startsWith(entryMethodNamePrefix)) {
                String entryMethodName = arg.substring(entryMethodNamePrefix.length());
                launchCommand.setEntryMethodName(entryMethodName);
            } else if (arg.startsWith(arkClasspathPrefix)) {
                String classpath = arg.substring(arkClasspathPrefix.length());
                List<URL> urlList = new ArrayList<>();
                for (String url : classpath.split(CLASSPATH_SPLIT)) {
                    if (url.isEmpty()) {
                        continue;
                    }
                    urlList.add(new URL(url));
                }
                launchCommand.setClasspath(urlList.toArray(new URL[urlList.size()]));
            } else if (arg.startsWith(arkConfigProfilePrefix)) {
                String profile = arg.substring(arkConfigProfilePrefix.length());
                launchCommand.setProfiles(profile.split(PROFILE_SPLIT));
            } else {
                // -A and -B argument would not passed into biz main method.
                arguments.add(arg);
            }
        }
        return launchCommand.setLaunchArgs(arguments.toArray(new String[] {}));
    }

    public static String toString(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            sb.append(arg);
        }
        return sb.toString();
    }
}