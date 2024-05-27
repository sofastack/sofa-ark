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

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Ark Container Directory
 *
 * @author qilong.zql
 * @since 0.3.0
 */
public class DirectoryContainerArchive implements ContainerArchive {

    private final URL[]           urls;

    private final static String[] AKR_CONTAINER_JAR = { "aopalliance-1.0", "commons-io-2.7",
            "guava-33.0.0-jre", "guice-6.0.0", "failureaccess-1.0.1", "javax.inject-1",
            "jakarta.inject-api-2.0.1", "logback-core-1.4.14", "logback-classic-1.4.14",
            "slf4j-api-2.0.11", "sofa-common-tools", "netty-all-4.1.109.Final",
            "netty-transport-4.1.109.Final", "netty-common-4.1.109.Final",
            "jakarta.inject-api-2.0.1", "netty-handler-4.1.109.Final", "netty-codec-4.1.109.Final",
            "netty-buffer-4.1.109.Final", "sofa-ark-parent/core-impl/container/target/classes",
            "sofa-ark-parent/core-impl/archive/target/classes",
            "sofa-ark-parent/core/spi/target/classes",
            "sofa-ark-parent/core/common/target/classes",
            "sofa-ark-parent/core/exception/target/classes",
            "sofa-ark-parent/core/api/target/classes" };

    public DirectoryContainerArchive(URL[] urls) {
        this.urls = urls;
    }

    public static Set<String> getArkContainerJarMarkers() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(AKR_CONTAINER_JAR)));
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
