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
import com.alipay.sofa.ark.spi.archive.ContainerArchive;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Ark Container Directory
 *
 * @author qilong.zql
 * @since 0.3.0
 */
public class DirectoryContainerArchive implements ContainerArchive {

    private final URL[] urls;

    public DirectoryContainerArchive(URL[] urls) {
        this.urls = urls;
    }

    @Override
    public URL[] getUrls() {
        return urls;
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
}