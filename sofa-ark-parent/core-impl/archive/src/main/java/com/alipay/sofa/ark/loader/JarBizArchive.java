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
import com.alipay.sofa.ark.spi.archive.BizArchive;

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

/**
 * Ark Biz Module Fat Jar
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class JarBizArchive extends AbstractArchive implements BizArchive {

    public final Archive archive;

    private final String SOFA_ARK_BIZ_LIB = "lib/";

    public JarBizArchive(Archive archive) {
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

    @Override
    public Archive getNestedArchive(Entry entry) throws IOException {
        return archive.getNestedArchive(entry);
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.archive.iterator();
    }

    @Override
    public URL[] getUrls() throws IOException {
        return getUrls(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return entry.getName().startsWith(SOFA_ARK_BIZ_LIB);
            }
        });
    }

}