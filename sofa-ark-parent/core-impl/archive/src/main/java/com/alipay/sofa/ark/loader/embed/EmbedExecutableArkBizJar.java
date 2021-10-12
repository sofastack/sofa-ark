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
package com.alipay.sofa.ark.loader.embed;

import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.JarContainerArchive;
import com.alipay.sofa.ark.loader.archive.ExplodedArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.*;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.alipay.sofa.ark.spi.constant.Constants.CONF_BASE_DIR;

public class EmbedExecutableArkBizJar extends ExecutableArkBizJar {
    public final String SOFA_ARK_CONTAINER = "sofa-ark-all";

    public EmbedExecutableArkBizJar(Archive archive, URL url) {
        super(archive, url);
    }

    @Override
    public ContainerArchive getContainerArchive() throws Exception {
        List<Archive> archives = getNestedArchives(new EntryFilter() {
            @Override
            public boolean matches(Entry entry) {
                return !entry.getName().equals(SOFA_ARK_CONTAINER)
                       && entry.getName().contains(SOFA_ARK_CONTAINER);
            }
        });

        if (archives.isEmpty()) {
            File sofaArkAll = getSofaArkAllFromClasspath();
            if (sofaArkAll != null) {
                return new JarContainerArchive(sofaArkAll.isDirectory() ? new ExplodedArchive(
                    sofaArkAll) : new JarFileArchive(sofaArkAll));
            }
            throw new RuntimeException("No ark container archive found!");
        }
        return new JarContainerArchive(archives.get(0));
    }

    protected File getSofaArkAllFromClasspath() {
        String classpath = System.getProperty("java.class.path");
        if (classpath.contains("sofa-ark-all")) {
            String[] classPaths = classpath.split(":");
            for (String path : classPaths) {
                if (path.contains("sofa-ark-all")) {
                    return new File(path);
                }
            }
        }
        return null;
    }

    /**
     * Returns the ark-biz module archives that will run upon ark container
     *
     * @return biz-app archives
     * @throws Exception
     */
    @Override
    public List<BizArchive> getBizArchives() throws Exception {
        return new ArrayList<>();
    }

    /**
     * Returns the ark plugin archives that will be applied to class isolation strategy of ark container
     *
     * @return ark plugin archives
     * @throws Exception
     */
    @Override
    public List<PluginArchive> getPluginArchives() throws Exception {
        return new ArrayList<>();
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
