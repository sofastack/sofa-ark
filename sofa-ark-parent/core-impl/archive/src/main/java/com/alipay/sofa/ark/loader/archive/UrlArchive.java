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
package com.alipay.sofa.ark.loader.archive;

import com.alipay.sofa.ark.spi.archive.Archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

public class UrlArchive implements Archive {
    private URL url;

    public UrlArchive(URL url) {
        this.url = url;
    }

    public UrlArchive(File file) throws MalformedURLException {
        this.url = file.toURI().toURL();
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return this.url;
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
        List<Entry> entries = new ArrayList<>();
        entries.add(new UrlEntry(this.url));
        return entries.iterator();
    }

    protected static class UrlEntry implements Entry {
        private URL url;

        public UrlEntry(URL url) {
            this.url = url;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public String getName() {
            return url.getFile();
        }
    }
}
