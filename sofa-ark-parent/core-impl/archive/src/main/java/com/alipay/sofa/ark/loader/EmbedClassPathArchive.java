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
package com.alipay.sofa.ark.loader;

import com.alipay.sofa.ark.bootstrap.ClasspathLauncher;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ContainerArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A embed classpath archive base on an application fat jar
 *
 * @author bingjie.lbj
 */
public class EmbedClassPathArchive extends ClasspathLauncher.ClassPathArchive {

    public EmbedClassPathArchive(String className, String method, URL[] urls) throws IOException {
        super(className, method, urls);
    }

    @Override
    public ContainerArchive getContainerArchive() throws Exception {
        List<URL> urlList = filterUrls(Constants.ARK_CONTAINER_MARK_ENTRY);

        if (urlList.isEmpty()) {
            return null;
        }

        if (urlList.size() > 1) {
            throw new ArkRuntimeException("Duplicate Container Jar File Found.");
        }

        return new JarContainerArchive(getUrlJarFileArchive(urlList.get(0)));
    }

    @Override
    public List<BizArchive> getBizArchives() {
        return new ArrayList<>();
    }

    @Override
    public List<PluginArchive> getPluginArchives() throws Exception {
        List<URL> urlList = filterUrls(Constants.ARK_PLUGIN_MARK_ENTRY);

        List<PluginArchive> pluginArchives = new ArrayList<>();
        for (URL url : urlList) {
            pluginArchives.add(new JarPluginArchive(getUrlJarFileArchive(url)));
        }
        return pluginArchives;
    }

    protected JarFileArchive getUrlJarFileArchive(URL url) throws IOException {
        String file = url.getFile();
        if (file.contains(FILE_IN_JAR)) {
            int pos = file.indexOf("!/");
            File fatJarFile = new File(file.substring(0, pos));
            String nestedJar = file.substring(file.lastIndexOf("/") + 1);
            JarFileArchive fatJarFileArchive = new JarFileArchive(fatJarFile);
            List<Archive> matched = fatJarFileArchive.getNestedArchives(entry -> {
                if (entry.getName().contains(nestedJar)) {
                    return true;
                }
                return false;
            });
            return (JarFileArchive) matched.get(0);
        } else {
            return new JarFileArchive(new File(file));
        }
    }
}
