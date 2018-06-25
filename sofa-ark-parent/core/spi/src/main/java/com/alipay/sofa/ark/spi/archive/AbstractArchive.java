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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * implementation of {@link ContainerArchive}, {@link PluginArchive}, {@link BizArchive}
 * should extends this
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class AbstractArchive implements Archive {

    @SuppressWarnings("unchecked")
    @Override
    public List<Archive> getNestedArchives(EntryFilter filter) throws IOException {
        List<Archive> nestedArchives = new ArrayList();
        for (Entry entry : this) {
            if (filter.matches(entry)) {
                nestedArchives.add(getNestedArchive(entry));
            }
        }
        return Collections.unmodifiableList(nestedArchives);
    }

    public URL[] getUrls(EntryFilter entryFilter) throws IOException {
        List<Archive> archives = getNestedArchives(entryFilter);

        List<URL> urls = new ArrayList<>(archives.size());
        urls.add(getUrl());

        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }

        return urls.toArray(new URL[urls.size()]);
    }

    public boolean isEntryExist(EntryFilter filter) {
        for (Entry entry : this) {
            if (filter.matches(entry)) {
                return true;
            }
        }
        return false;
    }
}