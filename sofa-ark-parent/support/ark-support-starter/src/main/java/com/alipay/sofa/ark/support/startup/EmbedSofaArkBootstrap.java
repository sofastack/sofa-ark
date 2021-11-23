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

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.bootstrap.ClasspathLauncher;
import com.alipay.sofa.ark.loader.EmbedClassPathArchive;
import com.alipay.sofa.ark.spi.argument.CommandArgument;
import com.alipay.sofa.ark.support.common.DelegateToMasterBizClassLoaderHook;
import org.springframework.core.env.Environment;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.sofa.ark.spi.constant.Constants;

/**
 * Launch an embed ark container
 *
 * @author bingjie.lbj
 */
public class EmbedSofaArkBootstrap {
    private static AtomicBoolean started = new AtomicBoolean(false);

    public static void launch(Environment environment) {
        if (started.compareAndSet(false, true)) {
            EntryMethod entryMethod = new EntryMethod(Thread.currentThread());

            getOrSetDefault(
                Constants.MASTER_BIZ,
                environment.getProperty(Constants.MASTER_BIZ,
                    environment.getProperty("spring.application.name")));
            getOrSetDefault(Constants.BIZ_CLASS_LOADER_HOOK_DIR,
                environment.getProperty(Constants.BIZ_CLASS_LOADER_HOOK_DIR));
            getOrSetDefault(Constants.PLUGIN_EXPORT_CLASS_ENABLE,
                environment.getProperty(Constants.PLUGIN_EXPORT_CLASS_ENABLE, "false"));
            getOrSetDefault(Constants.BIZ_CLASS_LOADER_HOOK_DIR,
                DelegateToMasterBizClassLoaderHook.class.getName());
            try {
                URL[] urls = getURLClassPath();
                ClasspathLauncher launcher = new ClasspathLauncher(new EmbedClassPathArchive(
                    entryMethod.getDeclaringClassName(), entryMethod.getMethod().getName(), urls));
                launcher.launch(new String[] {}, getClasspath(urls), entryMethod.getMethod());
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void getOrSetDefault(String key, String defaultValue) {
        String value = ArkConfigs.getStringValue(key);
        if (value == null && defaultValue != null) {
            ArkConfigs.setSystemProperty(key, defaultValue);
        }
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