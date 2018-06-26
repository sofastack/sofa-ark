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
import java.util.Set;

/**
 * An archive represents an ark-plugin
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface PluginArchive extends Archive {

    /**
     * fetch classpath of archive to startup ark-plugin
     *
     * @return the classpath contained in ark-plugin archive
     * @throws IOException throw exception when meets error
     */
    URL[] getUrls() throws IOException;

    /**
     * fetch plugin export index file content
     *
     * @return export index
     * @throws IOException throw exception when meets error
     */
    Set<String> getExportIndex() throws IOException;

    /**
     * check whether the entry satisfy the given {@link com.alipay.sofa.ark.spi.archive.Archive.EntryFilter}
     * exists or not
     *
     * @param filter
     * @return
     */
    boolean isEntryExist(EntryFilter filter);

}