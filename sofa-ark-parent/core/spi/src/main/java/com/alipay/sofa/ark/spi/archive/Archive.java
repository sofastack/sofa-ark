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
package com.alipay.sofa.ark.spi.archive;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * An archive that can be parsed in uniform style
 *
 * @author Phillip Webb
 */
public interface Archive extends Iterable<Archive.Entry> {

    /**
     * Returns a URL that can be used to load the archive.
     * @return the archive URL
     * @throws MalformedURLException if the URL is malformed
     */
    URL getUrl() throws MalformedURLException;

    /**
     * Returns the manifest of the archive.
     * @return the manifest
     * @throws IOException if the manifest cannot be read
     */
    Manifest getManifest() throws IOException;

    /**
     * Returns nested {@link Archive}s for entries that match the specified filter.
     * @param filter the filter used to limit entries
     * @return nested archives
     * @throws IOException if nested archives cannot be read
     */
    List<Archive> getNestedArchives(EntryFilter filter) throws IOException;

    Archive getNestedArchive(Entry entry) throws IOException;

    InputStream getInputStream(ZipEntry zipEntry) throws IOException;

    /**
     * Represents a single entry in the archive.
     */
    interface Entry {

        /**
         * Returns {@code true} if the entry represents a directory.
         * @return if the entry is a directory
         */
        boolean isDirectory();

        /**
         * Returns the name of the entry.
         * @return the name of the entry
         */
        String getName();

    }

    /**
     * Strategy interface to filter {@link Entry Entries}.
     */
    interface EntryFilter {

        /**
         * Apply the jar entry filter.
         * @param entry the entry to filter
         * @return {@code true} if the filter matches
         */
        boolean matches(Entry entry);

    }

}
