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
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.archive.ContainerArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.argument.CommandArgument;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class AbstractLauncher {

    /**
     * Launch the ark container. This method is the initial entry point when execute an fat jar.
     * @throws Exception if the ark container fails to launch.
     */
    public void launch(String[] args) throws Exception {
        JarFile.registerUrlProtocolHandler();

        ClassLoader classLoader = createContainerClassLoader(getContainerArchive());

        List<String> attachArgs = new ArrayList<>();
        attachArgs
            .add(String.format("%s%s=%s", CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK,
                CommandArgument.FAT_JAR_ARGUMENT_KEY, getExecutableArchive().getUrl()
                    .toExternalForm()));
        attachArgs.addAll(Arrays.asList(args));

        launch(attachArgs.toArray(new String[attachArgs.size()]), getMainClass(), classLoader);
    }

    /**
     * Launch the ark container. This method is the initial entry point when execute in IDE.
     * @throws Exception if the ark container fails to launch.
     */
    public void launch(String[] args, String classpath, Method method) throws Exception {
        JarFile.registerUrlProtocolHandler();

        ClassLoader classLoader = createContainerClassLoader(getContainerArchive());

        List<String> attachArgs = new ArrayList<>();
        attachArgs.add(String.format("%s%s=%s %s %s%s=%s %s %s%s=%s %s %s%s=%s",
            CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK, CommandArgument.CLASSPATH_ARGUMENT_KEY,
            classpath, CommandArgument.KEY_VALUE_PAIR_SPLIT,
            CommandArgument.ARK_BIZ_ARGUMENTS_MARK, CommandArgument.ENTRY_CLASS_NAME_ARGUMENT_KEY,
            method.getDeclaringClass().getName(), CommandArgument.KEY_VALUE_PAIR_SPLIT,
            CommandArgument.ARK_BIZ_ARGUMENTS_MARK, CommandArgument.ENTRY_METHOD_NAME_ARGUMENT_KEY,
            method.getName(), CommandArgument.KEY_VALUE_PAIR_SPLIT,
            CommandArgument.ARK_BIZ_ARGUMENTS_MARK,
            CommandArgument.ENTRY_METHOD_DESCRIPTION_ARGUMENT_KEY, method.toGenericString()));
        attachArgs.addAll(Arrays.asList(args));

        launch(attachArgs.toArray(new String[attachArgs.size()]), getMainClass(), classLoader);
    }

    /**
     * Launch the ark container in {@literal TEST} run mode. Only container and plugin
     * would startup.
     *
     * @param classpath classpath of ark-biz
     * @return Object {@literal com.alipay.sofa.ark.container.ArkContainer}
     * @throws Exception
     */
    public Object launch(String classpath) throws Exception {
        JarFile.registerUrlProtocolHandler();

        ClassLoader classLoader = createContainerClassLoader(getContainerArchive());

        List<String> attachArgs = new ArrayList<>();
        attachArgs.add(String.format("%s%s=%s %s %s%s=%s",
            CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK, CommandArgument.CLASSPATH_ARGUMENT_KEY,
            classpath, CommandArgument.KEY_VALUE_PAIR_SPLIT,
            CommandArgument.ARK_BIZ_ARGUMENTS_MARK, CommandArgument.BIZ_RUN_MODE,
            CommandArgument.TEST_RUN_MODE));

        return launch(attachArgs.toArray(new String[attachArgs.size()]), getMainClass(),
            classLoader);
    }

    protected Object launch(String[] args, String mainClass, ClassLoader classLoader)
                                                                                     throws Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return createMainMethodRunner(mainClass, args).run();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    protected MainMethodRunner createMainMethodRunner(String mainClass, String[] args) {
        return new MainMethodRunner(mainClass, args);
    }

    /**
     * Returns the executable archive
     * @return the executable archive
     * @throws Exception
     */
    protected abstract ExecutableArchive getExecutableArchive() throws Exception;

    /**
     * Returns the container archive archive
     * @return the container archive archive
     * @throws Exception
     */
    protected ContainerArchive getContainerArchive() throws Exception {
        return getExecutableArchive().getContainerArchive();
    }

    /**
     * Create a classloader for the ark container archive
     * @param containerArchive the ark container archive
     * @return the classloader load ark container
     * @throws Exception
     */
    protected ClassLoader createContainerClassLoader(ContainerArchive containerArchive)
                                                                                       throws Exception {
        return createContainerClassLoader(containerArchive.getUrls(), null);
    }

    /**
     * Create a classloader for the specified URLs.
     * @param urls the URLs
     * @param parent the parent
     * @return the classloader load ark container
     */
    protected ClassLoader createContainerClassLoader(URL[] urls, ClassLoader parent) {
        return new ContainerClassLoader(urls, parent);
    }

    /**
     * Returns the main class that should be launched.
     *
     * @return the name of the main class
     * @throws Exception if the main class cannot be obtained.
     */
    protected abstract String getMainClass() throws Exception;
}