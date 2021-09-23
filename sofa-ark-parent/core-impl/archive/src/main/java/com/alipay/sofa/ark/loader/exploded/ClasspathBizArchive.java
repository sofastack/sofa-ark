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
package com.alipay.sofa.ark.loader.exploded;

import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class ClasspathBizArchive implements BizArchive {
    private URL[]    urls;
    private URL[]    exportUrls;
    private Manifest manifest;

    public ClasspathBizArchive() throws MalformedURLException {
        this.urls = getUrlFromSystemClasspath();
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        throw new RuntimeException("unreachable invocation.");
    }

    @Override
    public Manifest getManifest() throws IOException {
        if (this.manifest == null) {
            Manifest manifest = new Manifest();
            manifest.getMainAttributes().putValue(MAIN_CLASS_ATTRIBUTE,
                System.getProperty(MAIN_CLASS_ATTRIBUTE));
            manifest.getMainAttributes().putValue(PRIORITY_ATTRIBUTE, String.valueOf(100));
            manifest.getMainAttributes().putValue(ARK_BIZ_NAME,
                System.getProperty(ARK_BIZ_NAME, System.getProperty("app_name", "MockApp")));
            manifest.getMainAttributes().putValue(ARK_BIZ_VERSION, "1.0.0");
            manifest.getMainAttributes().putValue(WEB_CONTEXT_PATH,
                System.getProperty(WEB_CONTEXT_PATH, ROOT_WEB_CONTEXT_PATH));
            manifest.getMainAttributes().putValue(INJECT_PLUGIN_DEPENDENCIES,
                System.getProperty(INJECT_PLUGIN_DEPENDENCIES));
            manifest.getMainAttributes().putValue(INJECT_EXPORT_PACKAGES,
                System.getProperty(INJECT_EXPORT_PACKAGES));
            this.manifest = manifest;
        }
        return manifest;
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
    public URL[] getUrls() throws IOException {
        return this.urls;
    }

    public URL[] getExportUrls() throws IOException {
        String injectPluginDependencies = System.getProperty(INJECT_PLUGIN_DEPENDENCIES);
        String[] dependencies = injectPluginDependencies.split(STRING_SEMICOLON);
        if (this.exportUrls == null) {
            this.exportUrls = getUrls(entry -> {
                UrlEntry urlEntry = (UrlEntry) entry;
                for (String dependency : dependencies) {
                    String artifactId = dependency.split(STRING_COLON)[0];
                    String version = dependency.split(STRING_COLON)[1];
                    if (urlEntry.getUrl().getPath().endsWith(artifactId + "-" + version + ".jar!/")
                            || urlEntry.getUrl().getPath().endsWith(artifactId + "-" + version + ".jar")) {
                        return true;
                    }

                }
                return false;
            });
        }
        return this.exportUrls;
    }

    public URL[] getUrls(EntryFilter entryFilter) throws IOException {
        List<URL> filterUrls = new ArrayList<>(this.urls.length);
        for (Entry entry : this) {
            if (entryFilter.matches(entry)) {
                filterUrls.add(((UrlEntry) entry).getUrl());
            }
        }
        return filterUrls.toArray(new URL[filterUrls.size()]);
    }

    @Override
    public boolean isEntryExist(EntryFilter filter) {
        for (Entry entry : this) {
            if (filter.matches(entry)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<Entry> iterator() {
        List<Entry> entries = new ArrayList<>(this.urls.length);
        for (URL url : this.urls) {
            entries.add(new UrlEntry(url));
        }
        return entries.iterator();
    }

    protected URL[] getUrlFromSystemClasspath() throws MalformedURLException {
        String classPath = System.getProperty("java.class.path");
        String[] classPaths = classPath.split(":");
        Set<URL> urlSet = new HashSet<>();
        for (String path : classPaths) {
            File file = new File(path);
            urlSet.add(file.toURI().toURL());
        }
        URL confUrl = getArkConfUrl();
        if (confUrl != null) {
            urlSet.add(confUrl);
        }
        return urlSet.toArray(new URL[urlSet.size()]);
    }

    protected URL getArkConfUrl() throws MalformedURLException {
        File file = new File(Constants.ARK_CONF_BASE_DIR + "/" + Constants.ARK_CONF_FILE);
        if (file.exists()) {
            return file.toURI().toURL();
        }
        return null;
    }

    protected static class UrlEntry implements Entry {
        private URL url;

        public UrlEntry(URL url) {
            this.url = url;
        }

        @Override
        public boolean isDirectory() {
            return !url.getFile().endsWith(".jar") && url.getFile().endsWith("/");
        }

        @Override
        public String getName() {
            return url.getFile();
        }

        public URL getUrl() {
            return url;
        }
    }

}
