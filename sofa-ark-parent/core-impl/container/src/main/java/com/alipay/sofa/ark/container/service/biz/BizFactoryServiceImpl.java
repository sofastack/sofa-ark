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
package com.alipay.sofa.ark.container.service.biz;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.loader.ExplodedBizArchive;
import com.alipay.sofa.ark.loader.DirectoryBizArchive;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizInfo.StateChangeReason;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * {@link BizFactoryService}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class BizFactoryServiceImpl implements BizFactoryService {

    @Inject
    private PluginManagerService pluginManagerService;

    @Override
    public Biz createBiz(BizArchive bizArchive) throws IOException {
        AssertUtils.isTrue(isArkBiz(bizArchive), "Archive must be a ark biz!");
        BizModel bizModel = new BizModel();
        Attributes manifestMainAttributes = bizArchive.getManifest().getMainAttributes();
        String mainClass = manifestMainAttributes.getValue(MAIN_CLASS_ATTRIBUTE);
        String startClass = manifestMainAttributes.getValue(START_CLASS_ATTRIBUTE);
        bizModel
            .setBizState(BizState.RESOLVED, StateChangeReason.CREATED)
            .setBizName(manifestMainAttributes.getValue(ARK_BIZ_NAME))
            .setBizVersion(manifestMainAttributes.getValue(ARK_BIZ_VERSION))
            .setMainClass(!StringUtils.isEmpty(startClass) ? startClass : mainClass)
            .setPriority(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE))
            .setWebContextPath(manifestMainAttributes.getValue(WEB_CONTEXT_PATH))
            .setDenyImportPackages(manifestMainAttributes.getValue(DENY_IMPORT_PACKAGES))
            .setDenyImportClasses(manifestMainAttributes.getValue(DENY_IMPORT_CLASSES))
            .setDenyImportResources(manifestMainAttributes.getValue(DENY_IMPORT_RESOURCES))
            .setInjectPluginDependencies(
                getInjectDependencies(manifestMainAttributes.getValue(INJECT_PLUGIN_DEPENDENCIES)))
            .setInjectExportPackages(manifestMainAttributes.getValue(INJECT_EXPORT_PACKAGES))
            .setDeclaredLibraries(manifestMainAttributes.getValue(DECLARED_LIBRARIES))
            .setClassPath(bizArchive.getUrls()).setPluginClassPath(getPluginURLs());

        if (!(bizArchive instanceof DirectoryBizArchive)) {
            bizModel.setBizUrl(bizArchive.getUrl());
        }

        BizClassLoader bizClassLoader = new BizClassLoader(bizModel.getIdentity(),
            getBizUcp(bizModel.getClassPath()), bizArchive instanceof ExplodedBizArchive
                                                || bizArchive instanceof DirectoryBizArchive);
        bizClassLoader.setBizModel(bizModel);
        bizModel.setClassLoader(bizClassLoader);
        return bizModel;
    }

    @Override
    public Biz createBiz(File file) throws IOException {
        BizArchive bizArchive;
        boolean unpackBizWhenInstall = Boolean.parseBoolean(ArkConfigs.getStringValue(
            UNPACK_BIZ_WHEN_INSTALL, "true"));
        if (ArkConfigs.isEmbedEnable() && unpackBizWhenInstall) {
            File unpackFile = FileUtils.file(file.getAbsolutePath() + "-unpack");
            if (!unpackFile.exists()) {
                unpackFile = FileUtils.unzip(file, file.getAbsolutePath() + "-unpack");
            }
            if (file.exists()) {
                file.delete();
            }
            file = unpackFile;
            bizArchive = new ExplodedBizArchive(unpackFile);
        } else {
            JarFile bizFile = new JarFile(file);
            JarFileArchive jarFileArchive = new JarFileArchive(bizFile);
            bizArchive = new JarBizArchive(jarFileArchive);
        }
        BizModel biz = (BizModel) createBiz(bizArchive);
        biz.setBizTempWorkDir(file);
        return biz;
    }

    @Override
    public Biz createBiz(BizOperation bizOperation, File file) throws IOException {
        BizModel biz = (BizModel) createBiz(file);
        if (bizOperation != null && !StringUtils.isEmpty(bizOperation.getBizVersion())) {
            biz.setBizVersion(bizOperation.getBizVersion());
            if (biz.getBizClassLoader() instanceof BizClassLoader) {
                BizClassLoader bizClassLoader = (BizClassLoader) (biz.getBizClassLoader());
                bizClassLoader.setBizIdentity(biz.getIdentity());
            }
        }
        return biz;
    }

    @Override
    public Biz createEmbedMasterBiz(ClassLoader masterClassLoader) {
        BizModel bizModel = new BizModel();
        bizModel.setBizState(BizState.RESOLVED, StateChangeReason.CREATED)
            .setBizName(ArkConfigs.getStringValue(MASTER_BIZ)).setBizVersion("1.0.0")
            .setMainClass("embed main").setPriority("100").setWebContextPath("/")
            .setDenyImportPackages(null).setDenyImportClasses(null).setDenyImportResources(null)
            .setInjectPluginDependencies(new HashSet<>()).setInjectExportPackages(null)
            .setClassPath(ClassLoaderUtils.getURLs(masterClassLoader))
            .setClassLoader(masterClassLoader);
        return bizModel;
    }

    private Set<String> getInjectDependencies(String injectPluginDependencies) {
        Set<String> dependencies = new HashSet<>();
        if (StringUtils.strToSet(injectPluginDependencies, Constants.MANIFEST_VALUE_SPLIT) != null) {
            dependencies.addAll(StringUtils.strToSet(injectPluginDependencies,
                Constants.MANIFEST_VALUE_SPLIT));
        }
        return dependencies;
    }

    private boolean isArkBiz(BizArchive bizArchive) {
        if (ArkConfigs.isEmbedEnable() && bizArchive instanceof ExplodedBizArchive) {
            return true;
        }
        return bizArchive.isEntryExist(new Archive.EntryFilter() {
            @Override
            public boolean matches(Archive.Entry entry) {
                return !entry.isDirectory() && entry.getName().equals(Constants.ARK_BIZ_MARK_ENTRY);
            }
        });
    }

    private URL[] getBizUcp(URL[] bizClassPath) {
        List<URL> bizUcp = new ArrayList<>();
        bizUcp.addAll(Arrays.asList(bizClassPath));
        bizUcp.addAll(Arrays.asList(getPluginURLs()));
        return bizUcp.toArray(new URL[bizUcp.size()]);
    }

    private URL[] getPluginURLs() {
        List<URL> pluginUrls = new ArrayList<>();
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            pluginUrls.add(plugin.getPluginURL());
        }
        return pluginUrls.toArray(new URL[pluginUrls.size()]);
    }
}
