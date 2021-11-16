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
package com.alipay.sofa.ark.support.startup;

import com.alipay.sofa.ark.bootstrap.ClasspathLauncher;
import com.alipay.sofa.ark.loader.EmbedClassPathArchive;
import com.alipay.sofa.ark.spi.argument.CommandArgument;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * Launch a embed ark container
 *
 * @author bingjie.lbj
 */
public class EmbedSofaArkBootstrap {
    private static final String  DEFAULT_BIZ_CLASS_LOADER_HOOK_DIR = "com.alipay.sofa.ark.container.service.classloader.MasterBizClassLoaderHookAll";
    private static AtomicBoolean started                           = new AtomicBoolean(false);

    public static void launch(Class mainClass, Environment environment) {
        if (started.compareAndSet(false, true)) {
            getOrSetDefault(MASTER_BIZ, environment.getProperty(MASTER_BIZ, environment.getProperty("spring.application.name")));
            getOrSetDefault(BIZ_CLASS_LOADER_HOOK_DIR, environment.getProperty(BIZ_CLASS_LOADER_HOOK_DIR));
            getOrSetDefault(PLUGIN_EXPORT_CLASS_ENABLE, environment.getProperty(PLUGIN_EXPORT_CLASS_ENABLE, "false"));
            getOrSetDefault(BIZ_CLASS_LOADER_HOOK_DIR, DEFAULT_BIZ_CLASS_LOADER_HOOK_DIR);
            try {
                URL[] urls = getURLClassPath();
                ClasspathLauncher launcher = new ClasspathLauncher(new EmbedClassPathArchive(
                    mainClass.getName(), "main", urls));
                launcher.launch(new String[] {}, getClasspath(urls), getMainMethod(mainClass));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Method getMainMethod(Class mainClass) throws NoSuchMethodException {
        Method entryMethod;
        try {
            entryMethod = mainClass.getMethod("main", String[].class);
        } catch (NoSuchMethodException ex) {
            entryMethod = mainClass.getDeclaredMethod("main", String[].class);
        }
        return entryMethod;
    }

    private static void getOrSetDefault(String key, String value) {
        if (System.getProperty(key) == null && value != null) {
            System.setProperty(key, value);
        }
    }

    public static void main(String[] args) {

    }

    private static String getClasspath(URL[] urls) {
        StringBuilder sb = new StringBuilder();
        for (URL url : urls) {
            sb.append(url.toExternalForm()).append(CommandArgument.CLASSPATH_SPLIT);
        }
        return sb.toString();
    }

    private static URL[] getURLClassPath() {
        ClassLoader classLoader = EmbedSofaArkBootstrap.class.getClassLoader();
        return ((URLClassLoader) classLoader).getURLs();
    }
}