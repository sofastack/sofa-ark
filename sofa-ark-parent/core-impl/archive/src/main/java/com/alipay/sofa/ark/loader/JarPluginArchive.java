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

import com.alipay.sofa.ark.spi.archive.AbstractArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class JarPluginArchive extends AbstractArchive implements PluginArchive {

    public final Archive archive;

    private final String SOFA_ARK_PLUGIN_LIB          = "lib/";
    private final String SOFA_ARK_PLUGIN_EXPORT_INDEX = "conf/export.index";

    public JarPluginArchive(Archive archive) {
        this.archive = archive;
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return this.archive.getUrl();
    }

    @Override
    public Manifest getManifest() throws IOException {
        return this.archive.getManifest();
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

    public Archive getNestedArchive(Entry entry) throws IOException {
        return this.archive.getNestedArchive(entry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.archive.iterator();
    }

    /**
     * fetch classpath to startup sofa-ark plugin
     *
     * @return
     */
    public URL[] getUrls() throws IOException {
        return getUrls(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getName().startsWith(SOFA_ARK_PLUGIN_LIB)
                       && !entry.getName().equals(SOFA_ARK_PLUGIN_LIB);
            }
        });
    }

    public Set<String> getExportIndex() throws IOException {
        InputStream inputStream = archive
            .getInputStream(new ZipEntry(SOFA_ARK_PLUGIN_EXPORT_INDEX));
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        Set<String> exportIndex = new HashSet<>();
        for (String line = bufferedReader.readLine(); line != null; line = bufferedReader
            .readLine()) {
            if (!line.trim().isEmpty()) {
                exportIndex.add(line.trim());
            }
        }

        return exportIndex;
    }
}