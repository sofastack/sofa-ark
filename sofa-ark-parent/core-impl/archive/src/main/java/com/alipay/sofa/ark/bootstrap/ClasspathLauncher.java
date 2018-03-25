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

import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.loader.DirectoryBizModuleArchive;
import com.alipay.sofa.ark.loader.JarBizModuleArchive;
import com.alipay.sofa.ark.loader.JarContainerArchive;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.*;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClasspathLauncher extends ArkLauncher {

    public ClasspathLauncher(ClassPathArchive classPathArchive) {
        super(classPathArchive);
    }

    public static class ClassPathArchive implements ExecutableArchive {

        public static final String   FILE_IN_JAR = "!/";

        private final String         className;

        private final String         methodName;

        private final String         methodDescription;

        private final URL[]          urls;

        private final URLClassLoader urlClassLoader;

        public ClassPathArchive(URL[] urls) {
            this.className = null;
            this.methodName = null;
            this.methodDescription = null;
            this.urls = urls;
            urlClassLoader = new URLClassLoader(urls, null);
        }

        public ClassPathArchive(String className, String methodName, String methodDescription,
                                URL[] urls) {
            this.className = className;
            this.methodName = methodName;
            this.methodDescription = methodDescription;
            this.urls = urls;
            urlClassLoader = new URLClassLoader(urls, null);
        }

        public List<URL> filterUrls(String resource) throws Exception {
            List<URL> urlList = new ArrayList<>();

            Enumeration<URL> enumeration = urlClassLoader.findResources(resource);
            while (enumeration.hasMoreElements()) {
                URL resourceUrl = enumeration.nextElement();
                String resourceFile = resourceUrl.getFile();
                String jarFile = resourceFile.substring(0,
                    resourceFile.length() - resource.length() - FILE_IN_JAR.length());
                urlList.add(new URL(jarFile));
            }

            return urlList;
        }

        @Override
        public ContainerArchive getContainerArchive() throws Exception {
            List<URL> urlList = filterUrls(Constants.ARK_CONTAINER_MARK_ENTRY);

            if (urlList.isEmpty()) {
                throw new ArkException("No Ark Container Jar File Found.");
            }

            if (urlList.size() > 1) {
                throw new ArkException("Duplicate Container Jar File Found.");
            }

            return new JarContainerArchive(new JarFileArchive(new File(urlList.get(0).getFile())));

        }

        @Override
        public List<BizArchive> getBizArchives() throws Exception {
            List<URL> urlList = filterUrls(Constants.ARK_MODULE_MARK_ENTRY);

            List<BizArchive> bizArchives = new LinkedList<>();
            bizArchives.add(createDirectoryBizModuleArchive());

            for (URL url : urlList) {
                bizArchives
                    .add(new JarBizModuleArchive(new JarFileArchive(new File(url.getFile()))));
            }

            return bizArchives;
        }

        @Override
        public List<PluginArchive> getPluginArchives() throws Exception {
            List<URL> urlList = filterUrls(Constants.ARK_PLUGIN_MARK_ENTRY);

            List<PluginArchive> pluginArchives = new ArrayList<>();
            for (URL url : urlList) {
                pluginArchives
                    .add(new JarPluginArchive(new JarFileArchive(new File(url.getFile()))));
            }

            return pluginArchives;
        }

        @Override
        public URL getUrl() throws MalformedURLException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Manifest getManifest() throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Archive getNestedArchive(Entry entry) throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Iterator<Entry> iterator() {
            throw new RuntimeException("unreachable invocation.");
        }

        protected BizArchive createDirectoryBizModuleArchive() {
            return new DirectoryBizModuleArchive(className, methodName, methodDescription, urls);
        }
    }

}