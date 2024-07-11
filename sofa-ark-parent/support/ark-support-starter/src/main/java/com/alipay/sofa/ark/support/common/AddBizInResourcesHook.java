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
package com.alipay.sofa.ark.support.common;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.biz.AddBizToStaticDeployHook;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.alipay.sofa.common.utils.StringUtil;
import org.springframework.core.env.Environment;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.SOFA_ARK_MODULE;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: AddBizInResourcesHook.java, v 0.1 2024年07月06日 19:48 立蓬 Exp $
 */
@Extension("add-biz-in-resources-to-deploy")
public class AddBizInResourcesHook implements AddBizToStaticDeployHook {

    @Override
    public List<BizArchive> getStaticBizToAdd() throws Exception {
        List<BizArchive> archives = new ArrayList<>();
        if (ArkConfigs.isEmbedEnable() && isEmbedStaticBizInResourceEnable()) {
            archives.addAll(getBizArchiveFromResources());
        }
        return archives;
    }

    private boolean isEmbedStaticBizInResourceEnable() {
        return ArkConfigs.getBooleanValue(Constants.EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE,
            Boolean.TRUE);
    }

    protected List<BizArchive> getBizArchiveFromResources() throws Exception {
        List<BizArchive> archives = new ArrayList<>();
        URL bizDirURL = ArkClient.getMasterBiz().getBizClassLoader().getResource(SOFA_ARK_MODULE);
        if (null == bizDirURL) {
            return archives;
        }

        if (bizDirURL.getProtocol().equals("file")) {
            return getBizArchiveForFile(bizDirURL);
        }

        if (bizDirURL.getProtocol().equals("jar")) {
            return getBizArchiveForJar(bizDirURL);
        }

        return archives;
    }

    private List<BizArchive> getBizArchiveForFile(URL bizDirURL) throws Exception {
        List<BizArchive> archives = new ArrayList<>();

        File bizDir = org.apache.commons.io.FileUtils.toFile(bizDirURL);
        if (!bizDir.exists() || !bizDir.isDirectory() || null == bizDir.listFiles()) {
            return archives;
        }

        for (File bizFile : bizDir.listFiles()) {
            archives.add(new JarBizArchive(new JarFileArchive(bizFile)));
        }
        return archives;
    }

    private List<BizArchive> getBizArchiveForJar(URL bizDirURL) throws Exception{
        List<BizArchive> archives = new ArrayList<>();

        JarFileArchive jarFileArchive = getJarFileArchiveFromUrl(bizDirURL);
        String prefix = getEntryName(bizDirURL);
        List<Archive>  archivesFromJar = jarFileArchive.getNestedArchives(entry -> !entry.isDirectory() && entry.getName().startsWith(prefix) && !entry.getName().equals(prefix));

        for (Archive archiveFromJarEntry : archivesFromJar) {
            archives.add(new JarBizArchive(archiveFromJarEntry));
        }
        return archives;
    }

    private JarFileArchive getJarFileArchiveFromUrl(URL url) throws Exception {
        String jarPath = StringUtil.substringBefore(((JarURLConnection) url.openConnection())
            .getJarFile().getName(), "!");
        return new JarFileArchive(com.alipay.sofa.ark.common.util.FileUtils.file(jarPath));
    }

    private String getEntryName(URL url) throws IOException {
        String classPathEntryName = StringUtil.substringAfter(
            ((JarURLConnection) url.openConnection()).getJarFile().getName(), "!/");
        String urlEntryNameFromClassPath = ((JarURLConnection) url.openConnection()).getJarEntry()
            .getName();
        return StringUtil.join(new String[] { classPathEntryName, urlEntryNameFromClassPath }, "/");
    }
}