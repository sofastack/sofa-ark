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

import com.alipay.sofa.ark.spi.archive.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static com.alipay.sofa.ark.spi.constant.Constants.CONF_BASE_DIR;

/**
 * Executable Ark Biz Fat Jar
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class ExecutableArkBizJar implements ExecutableArchive {

    public final String  SOFA_ARK_CONTAINER = "SOFA-ARK/container/";

    public final String  SOFA_ARK_MODULE    = "SOFA-ARK/biz/";

    public final String  SOFA_ARK_PLUGIN    = "SOFA-ARK/plugin/";

    public final Archive archive;

    public final URL     url;

    public ExecutableArkBizJar(Archive archive) {
        this(archive, null);
    }

    public ExecutableArkBizJar(Archive archive, URL url) {
        this.archive = archive;
        this.url = url;
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return this.url == null ? this.archive.getUrl() : this.url;
    }

    @Override
    public Manifest getManifest() throws IOException {
        return archive.getManifest();
    }

    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
        List<Archive> nestedArchives = new ArrayList<>();
        for (Entry entry : this) {
            if (filter.matches(entry)) {
                nestedArchives.add(getNestedArchive(entry));
            }
        }
        return Collections.unmodifiableList(nestedArchives);
    }

    @Override
    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        return this.archive.getInputStream(zipEntry);
    }

    @Override
    public Archive getNestedArchive(Entry entry) throws IOException {
        return this.archive.getNestedArchive(entry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.archive.iterator();
    }

    @Override
    public ContainerArchive getContainerArchive() throws Exception {

        List<Archive> archives = getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_CONTAINER)
                       && entry.getName().startsWith(SOFA_ARK_CONTAINER);
            }
        });

        if (archives.isEmpty()) {
            throw new RuntimeException("No ark container archive found!");
        }

        return new JarContainerArchive(archives.get(0));
    }

    /**
     * Returns the ark-biz module archives that will run upon ark container
     * @return biz-app archives
     * @throws Exception
     */
    @Override
    public List<BizArchive> getBizArchives() throws Exception {

        List<Archive> archives = getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_MODULE)
                       && entry.getName().startsWith(SOFA_ARK_MODULE);
            }
        });

        List<BizArchive> bizArchives = new ArrayList<>();
        for (Archive archive : archives) {
            bizArchives.add(new JarBizArchive(archive));
        }
        return bizArchives;

    }

    /**
     * Returns the ark plugin archives that will be applied to class isolation strategy of ark container
     * @return ark plugin archives
     * @throws Exception
     */
    @Override
    public List<PluginArchive> getPluginArchives() throws Exception {

        List<Archive> archives = this.archive.getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_PLUGIN)
                       && entry.getName().startsWith(SOFA_ARK_PLUGIN);
            }
        });

        List<PluginArchive> pluginArchives = new ArrayList<>();
        for (Archive archive : archives) {
            pluginArchives.add(new JarPluginArchive(archive));
        }
        return pluginArchives;

    }

    @Override
    public List<URL> getConfClasspath() throws Exception {
        List<Archive> archives = getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getName().startsWith(CONF_BASE_DIR) && entry.isDirectory();
            }
        });
        List<URL> urls = new ArrayList<>();
        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }
        return urls;
    }
}