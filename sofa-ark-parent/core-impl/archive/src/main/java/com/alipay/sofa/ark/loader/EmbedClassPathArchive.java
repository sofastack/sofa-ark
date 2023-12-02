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
import com.alipay.sofa.ark.common.util.FileUtils;
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
import java.util.Enumeration;
import java.util.LinkedList;
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
            return createDirectoryContainerArchive();
        }

        if (urlList.size() > 1) {
            throw new ArkRuntimeException("Duplicate Container Jar File Found.");
        }

        return new JarContainerArchive(getUrlJarFileArchive(urlList.get(0)));
    }

    @Override
    public List<BizArchive> getBizArchives() throws Exception {
        //将classpath中的biz包载入
        List<URL> urlList = filterBizUrl(Constants.ARK_BIZ_MARK_ENTRY);
        List<BizArchive> bizArchives = new LinkedList<>();
        for (URL url : urlList) {
            //判断是classpath下的还是fatjar内的biz包
            if (url.getPath().contains(".jar!")) {
                Archive archiveFromJarEntry = getArchiveFromJarEntry(url);
                if (archiveFromJarEntry != null) {
                    bizArchives.add(new JarBizArchive(archiveFromJarEntry));
                }

            } else {
                bizArchives
                    .add(new JarBizArchive(new JarFileArchive(FileUtils.file(url.getFile()))));
            }
        }
        return bizArchives;
    }

    /**
     * 从biz包内解析出archive
     * @param jarUrl biz包的url路径
     * @return 依赖的arkbiz包
     * @throws IOException
     */
    private Archive getArchiveFromJarEntry(URL jarUrl) throws IOException {
        String jarPath = jarUrl.getPath().substring(0, jarUrl.getPath().indexOf("!"));
        String bizPath = jarUrl.getPath().substring(jarUrl.getPath().indexOf("!") + 2);
        List<Archive> nestedArchives = new JarFileArchive(FileUtils.file(jarPath))
                .getNestedArchives(entry -> entry.getName().equals(bizPath));
        if (nestedArchives.isEmpty()) {
            return null;
        }
        return nestedArchives.get(0);
    }

    /**
     * 过滤出biz包
     */
    public List<URL> filterBizUrl(String resource) throws Exception {
        List<URL> urlList = new ArrayList<>();
        Enumeration<URL> enumeration = super.urlClassLoader.findResources(resource);
        while (enumeration.hasMoreElements()) {
            URL resourceUrl = enumeration.nextElement();
            String resourceFile = resourceUrl.getFile();
            String jarFile = resourceFile.substring(0, resourceFile.length() - resource.length()
                                                       - FILE_IN_JAR.length());
            urlList.add(new URL(jarFile));
        }
        return urlList;
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
            int pos = file.indexOf(FILE_IN_JAR);
            File fatJarFile = FileUtils.file(file.substring(0, pos));
            String nestedJar = file.substring(file.lastIndexOf("/") + 1);
            JarFileArchive fatJarFileArchive = new JarFileArchive(fatJarFile);
            List<Archive> matched = fatJarFileArchive.getNestedArchives(entry -> entry.getName().contains(nestedJar));
            return (JarFileArchive) matched.get(0);
        } else {
            return new JarFileArchive(FileUtils.file(file));
        }
    }
}
