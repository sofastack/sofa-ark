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

import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Ark  Biz Module exploded directory archive
 *
 * @author bingjie.lbj
 */
public class ExplodedBizArchive implements BizArchive {
    private static final String SOFA_ARK_BIZ_LIB = "lib/";
    private static final String MANIFEST_NAME    = "META-INF/MANIFEST.MF";
    private File                file;
    private URL[]               urls;
    private Manifest            manifest;

    public ExplodedBizArchive(File root) throws IOException {
        this.file = root;
        this.urls = scanUrl();
        this.manifest = new Manifest(new FileInputStream(new File(root, MANIFEST_NAME)));
    }

    private URL[] scanUrl() throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        urls.add(this.file.toURI().toURL());
        File libs = new File(file, SOFA_ARK_BIZ_LIB);
        urls.add(libs.toURI().toURL());
        File[] files = libs.listFiles();
        if (files != null) {
            for (File lib : files) {
                urls.add(lib.toURI().toURL());
            }
        }

        return urls.toArray(new URL[] {});
    }

    @Override
    public URL[] getUrls() throws IOException {
        return urls;
    }

    @Override
    public boolean isEntryExist(EntryFilter filter) {
        throw new UnsupportedOperationException("unreachable invocation.");
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return file.toURI().toURL();
    }

    @Override
    public Manifest getManifest() throws IOException {
        return this.manifest;
    }

    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
        throw new UnsupportedOperationException("unreachable invocation.");
    }

    @Override
    public Archive getNestedArchive(Entry entry) throws IOException {
        throw new UnsupportedOperationException("unreachable invocation.");
    }

    @Override
    public InputStream getInputStream(ZipEntry zipEntry) throws IOException {
        throw new UnsupportedOperationException("unreachable invocation.");
    }

    @Override
    public Iterator<Entry> iterator() {
        throw new UnsupportedOperationException("unreachable invocation.");
    }
}
