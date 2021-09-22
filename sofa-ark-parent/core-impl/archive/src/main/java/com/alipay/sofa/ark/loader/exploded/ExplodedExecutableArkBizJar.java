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
package com.alipay.sofa.ark.loader.exploded;

import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.JarContainerArchive;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.spi.archive.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.CONF_BASE_DIR;

/**
 * Executable Ark Biz Fat Jar
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class ExplodedExecutableArkBizJar extends ExecutableArkBizJar {

    public ExplodedExecutableArkBizJar(Archive archive) {
        super(archive);
    }

    public ExplodedExecutableArkBizJar(Archive archive, URL url) {
        super(archive, url);
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
     *
     * @return biz-app archives
     * @throws Exception
     */
    @Override
    public List<BizArchive> getBizArchives() throws Exception {
        List<BizArchive> bizArchives = new ArrayList<>();
        if ("true".equals(System.getProperty("start_in_ide", "false"))) {
            ClasspathBizArchive bizArchive = new ClasspathBizArchive();
            bizArchives.add(bizArchive);
            return bizArchives;
        }
        List<Archive> archives = getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_MODULE)
                       && entry.getName().startsWith(SOFA_ARK_MODULE) && entry.isDirectory()
                       && entry.getName().split("/").length == 3;
            }
        });
        for (Archive archive : archives) {
            bizArchives.add(new JarBizArchive(archive));
        }
        return bizArchives;
    }

    /**
     * Returns the ark plugin archives that will be applied to class isolation strategy of ark container
     *
     * @return ark plugin archives
     * @throws Exception
     */
    @Override
    public List<PluginArchive> getPluginArchives() throws Exception {
        List<Archive> archives = this.archive.getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_PLUGIN)
                       && entry.getName().startsWith(SOFA_ARK_PLUGIN) && entry.isDirectory()
                       && entry.getName().split("/").length == 3;
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