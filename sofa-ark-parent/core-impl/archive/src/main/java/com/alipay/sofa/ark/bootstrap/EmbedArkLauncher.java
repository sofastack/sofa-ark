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

import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.argument.CommandArgument;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class EmbedArkLauncher extends AbstractLauncher {
    private static final String     SOFA_ARK_MAIN                     = "com.alipay.sofa.ark.container.EmbedArkContainer";
    private static final String     DEFAULT_BIZ_CLASS_LOADER_HOOK_DIR = "com.alipay.sofa.ark.container.service.classloader.MasterBizClassLoaderHookAll";
    private final ExecutableArchive executableArchive;

    public EmbedArkLauncher() {
        try {
            this.executableArchive = createArchive();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public EmbedArkLauncher(ExecutableArchive executableArchive) {
        this.executableArchive = executableArchive;
    }

    public Object launch(String[] args) throws Exception {
        ClassLoader classLoader = createContainerClassLoader(getContainerArchive());
        List<String> attachArgs = new ArrayList<>();
        attachArgs.add(String.format("%s%s=%s", CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK,
            CommandArgument.CLASSPATH_ARGUMENT_KEY, getClasspath()));
        attachArgs.add(String.format("%s%s=%s", CommandArgument.ARK_BIZ_ARGUMENTS_MARK,
            CommandArgument.ENTRY_CLASS_NAME_ARGUMENT_KEY, SOFA_ARK_MAIN));
        attachArgs.add(String.format("%s%s=%s", CommandArgument.ARK_BIZ_ARGUMENTS_MARK,
            CommandArgument.ENTRY_METHOD_NAME_ARGUMENT_KEY, "main"));
        return launch(attachArgs.toArray(new String[attachArgs.size()]), getMainClass(),
            classLoader);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(Constants.EMBED_ENABLE, "true");
        getOrSetDefault(MASTER_BIZ, new File("").getAbsolutePath());
        getOrSetDefault(BIZ_CLASS_LOADER_HOOK_DIR, DEFAULT_BIZ_CLASS_LOADER_HOOK_DIR);
        new EmbedArkLauncher().launch(args);
    }

    private static void getOrSetDefault(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    @Override
    protected ExecutableArchive getExecutableArchive() throws Exception {
        return this.executableArchive;
    }

    @Override
    protected String getMainClass() throws Exception {
        return SOFA_ARK_MAIN;
    }

    protected ExecutableArchive createArchive() throws Exception {
        return new ClasspathLauncher.ClassPathArchive(SOFA_ARK_MAIN, "main", getUrls());
    }

    protected URL[] getUrls() {
        return ((URLClassLoader) this.getClass().getClassLoader()).getURLs();
    }

    private String getClasspath() {
        URL[] urls = getUrls();
        StringBuilder sb = new StringBuilder();
        for (URL url : urls) {
            sb.append(url.toExternalForm()).append(CommandArgument.CLASSPATH_SPLIT);
        }
        return sb.toString();
    }

    /**
     * Create a classloader for the specified URLs.
     *
     * @param urls   the URLs
     * @param parent the parent
     * @return the classloader load ark container
     */
    protected ClassLoader createContainerClassLoader(URL[] urls, ClassLoader parent) {
        return new ContainerClassLoader(urls, parent, this.getClass().getClassLoader());
    }
}
