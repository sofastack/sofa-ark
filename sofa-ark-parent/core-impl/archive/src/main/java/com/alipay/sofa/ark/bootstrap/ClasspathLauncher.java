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

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.*;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.*;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_BASE_DIR;
import static com.alipay.sofa.ark.spi.constant.Constants.SUREFIRE_BOOT_CLASSPATH;
import static com.alipay.sofa.ark.spi.constant.Constants.SUREFIRE_BOOT_CLASSPATH_SPLIT;

/**
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

        private final URL[]          urls;

        private final URLClassLoader urlClassLoader;

        private File                 arkConfBaseDir;

        public ClassPathArchive(String className, String methodName, URL[] urls) throws IOException {
            AssertUtils.isFalse(StringUtils.isEmpty(className),
                "Entry class name must be specified.");
            this.className = className;
            this.methodName = methodName;
            this.urls = urls;
            List<URL> classpath = getConfClasspath();
            classpath.addAll(Arrays.asList(this.urls));
            urlClassLoader = new URLClassLoader(classpath.toArray(new URL[] {}), null);
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

            ContainerArchive archive = getJarContainerArchive();

            if (archive == null) {
                archive = createDirectoryContainerArchive();
            }

            if (archive == null) {
                throw new ArkRuntimeException("No Ark Container Jar File Found.");
            }

            return archive;
        }

        protected ContainerArchive getJarContainerArchive() throws Exception {
            List<URL> urlList = filterUrls(Constants.ARK_CONTAINER_MARK_ENTRY);

            if (urlList.isEmpty()) {
                return null;
            }

            if (urlList.size() > 1) {
                throw new ArkRuntimeException("Duplicate Container Jar File Found.");
            }

            return new JarContainerArchive(new JarFileArchive(new File(urlList.get(0).getFile())));
        }

        @Override
        public List<BizArchive> getBizArchives() throws Exception {
            List<URL> urlList = filterUrls(Constants.ARK_BIZ_MARK_ENTRY);

            List<BizArchive> bizArchives = new LinkedList<>();
            if (className != null && methodName != null) {
                bizArchives.add(createDirectoryBizModuleArchive());
            }

            for (URL url : urlList) {
                bizArchives.add(new JarBizArchive(new JarFileArchive(new File(url.getFile()))));
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
        public List<URL> getConfClasspath() throws IOException {
            List<URL> urls = new ArrayList<>();
            if (arkConfBaseDir == null) {
                arkConfBaseDir = deduceArkConfBaseDir();
            }
            scanConfClasspath(arkConfBaseDir, urls);
            return urls;
        }

        private void scanConfClasspath(File arkConfBaseDir, List<URL> classpath) throws IOException {
            if (arkConfBaseDir == null || arkConfBaseDir.isFile()
                || arkConfBaseDir.listFiles() == null) {
                return;
            }
            classpath.add(arkConfBaseDir.toURI().toURL());
            for (File subFile : arkConfBaseDir.listFiles()) {
                scanConfClasspath(subFile, classpath);
            }
        }

        private File deduceArkConfBaseDir() {
            File arkConfDir = null;
            try {
                URLClassLoader tempClassLoader = new URLClassLoader(urls);
                Class entryClass = tempClassLoader.loadClass(className);
                String classLocation = ClassUtils.getCodeBase(entryClass);
                if (classLocation.startsWith("file:")) {
                    classLocation = classLocation.substring("file:".length());
                }
                File file = classLocation == null ? null : new File(classLocation);
                while (file != null) {
                    arkConfDir = new File(file.getPath() + File.separator + ARK_CONF_BASE_DIR);
                    if (arkConfDir.exists() && arkConfDir.isDirectory()) {
                        break;
                    }
                    file = file.getParentFile();
                }
            } catch (Throwable throwable) {
                throw new ArkRuntimeException(throwable);
            }
            // return 'conf/' directory or null
            return arkConfDir == null ? null : arkConfDir.getParentFile();
        }

        @Override
        public URL getUrl() {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Manifest getManifest() {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public List<Archive> getNestedArchives(EntryFilter filter) {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Archive getNestedArchive(Entry entry) {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public InputStream getInputStream(ZipEntry zipEntry) {
            throw new RuntimeException("unreachable invocation.");
        }

        @Override
        public Iterator<Entry> iterator() {
            throw new RuntimeException("unreachable invocation.");
        }

        protected BizArchive createDirectoryBizModuleArchive() {
            return new DirectoryBizArchive(className, methodName, filterBizUrls(urls));
        }

        protected ContainerArchive createDirectoryContainerArchive() {
            URL[] candidates;
            if (urls.length == 1 || urls.length == 2) {
                candidates = parseClassPathFromSurefireBoot(getSurefireBooterJar(urls));
            } else {
                candidates = urls;
            }
            URL[] filterUrls = filterURLs(candidates);
            return filterUrls == null ? null : new DirectoryContainerArchive(filterUrls);
        }

        private URL getSurefireBooterJar(URL[] urls) {
            for (URL url : urls) {
                if (url.getFile().contains(Constants.SUREFIRE_BOOT_JAR)) {
                    return url;
                }
            }
            return null;
        }

        /**
         * this method is used to choose jar file which is contained in sofa-ark-all.jar
         *
         * @return
         */
        protected URL[] filterURLs(URL[] urls) {
            Set<String> arkContainerJarMarkers = DirectoryContainerArchive
                .getArkContainerJarMarkers();

            Set<URL> containerClassPath = new HashSet<>();
            for (String marker : arkContainerJarMarkers) {
                for (URL url : urls) {
                    if (url.getPath().contains(marker)) {
                        containerClassPath.add(url);
                    }
                }
            }

            return arkContainerJarMarkers.size() != containerClassPath.size() ? null
                : containerClassPath.toArray(new URL[] {});
        }

        /**
         * this method is used to eliminate agent classpath and biz classpath
         *
         * @param urls
         * @return
         */
        protected URL[] filterBizUrls(URL[] urls) {
            URL[] agentClassPath = ClassLoaderUtils.getAgentClassPath();
            List<URL> urlList;
            try {
                urlList = filterUrls(Constants.ARK_BIZ_MARK_ENTRY);
            } catch (Throwable throwable) {
                // ignore
                urlList = Collections.emptyList();
            }
            List<URL> bizURls = new ArrayList<>();
            boolean isAgent;
            for (URL url : urls) {
                isAgent = false;
                for (URL agentUrl : agentClassPath) {
                    if (url.equals(agentUrl)) {
                        isAgent = true;
                        break;
                    }
                }
                if (!isAgent && !urlList.contains(url)) {
                    bizURls.add(url);
                }
            }

            return bizURls.toArray(new URL[] {});
        }

        /**
         * when execute mvn test, the classpath would be recorded in a MANIFEST.MF file ,
         * including a surefire boot jar.
         *
         * @param surefireBootJar
         * @return
         */
        protected URL[] parseClassPathFromSurefireBoot(URL surefireBootJar) {
            AssertUtils.assertNotNull(surefireBootJar, "SurefireBooter jar should not be null.");
            try (JarFile jarFile = new JarFile(surefireBootJar.getFile())) {
                String[] classPath = jarFile.getManifest().getMainAttributes()
                    .getValue(SUREFIRE_BOOT_CLASSPATH).split(SUREFIRE_BOOT_CLASSPATH_SPLIT);
                List<URL> urls = new ArrayList<>();
                for (String path : classPath) {
                    urls.add(new URL(path));
                }
                return urls.toArray(new URL[] {});
            } catch (IOException ex) {
                throw new ArkRuntimeException("Parse classpath failed from surefire boot jar.", ex);
            }
        }
    }

}