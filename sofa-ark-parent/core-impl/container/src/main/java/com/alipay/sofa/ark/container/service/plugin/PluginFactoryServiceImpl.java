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
package com.alipay.sofa.ark.container.service.plugin;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.container.model.PluginContextImpl;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.classloader.PluginClassLoader;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.google.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * {@link PluginFactoryService}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class PluginFactoryServiceImpl implements PluginFactoryService {
    @Override
    public Plugin createPlugin(PluginArchive pluginArchive) throws IOException,
                                                           IllegalArgumentException {
        AssertUtils.isTrue(isArkPlugin(pluginArchive), "Archive must be a ark plugin!");
        PluginModel plugin = new PluginModel();
        Attributes manifestMainAttributes = pluginArchive.getManifest().getMainAttributes();
        plugin
            .setPluginName(manifestMainAttributes.getValue(PLUGIN_NAME_ATTRIBUTE))
            .setGroupId(manifestMainAttributes.getValue(GROUP_ID_ATTRIBUTE))
            .setArtifactId(manifestMainAttributes.getValue(ARTIFACT_ID_ATTRIBUTE))
            .setVersion(manifestMainAttributes.getValue(PLUGIN_VERSION_ATTRIBUTE))
            .setPriority(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE))
            .setPluginActivator(manifestMainAttributes.getValue(ACTIVATOR_ATTRIBUTE))
            .setClassPath(pluginArchive.getUrls())
            .setPluginUrl(pluginArchive.getUrl())
            .setExportClasses(manifestMainAttributes.getValue(EXPORT_CLASSES_ATTRIBUTE))
            .setExportPackages(manifestMainAttributes.getValue(EXPORT_PACKAGES_ATTRIBUTE))
            .setImportClasses(manifestMainAttributes.getValue(IMPORT_CLASSES_ATTRIBUTE))
            .setImportPackages(manifestMainAttributes.getValue(IMPORT_PACKAGES_ATTRIBUTE))
            .setExportIndex(pluginArchive.getExportIndex())
            .setImportResources(manifestMainAttributes.getValue(IMPORT_RESOURCES_ATTRIBUTE))
            .setExportResources(manifestMainAttributes.getValue(EXPORT_RESOURCES_ATTRIBUTE))
            .setPluginClassLoader(
                new PluginClassLoader(plugin.getPluginName(), plugin.getClassPath()))
            .setPluginContext(new PluginContextImpl(plugin));
        return plugin;
    }

    @Override
    public Plugin createPlugin(File file) throws IOException {
        JarFile pluginFile = new JarFile(file);
        JarFileArchive jarFileArchive = new JarFileArchive(pluginFile);
        JarPluginArchive jarPluginArchive = new JarPluginArchive(jarFileArchive);
        return createPlugin(jarPluginArchive);
    }

    private boolean isArkPlugin(PluginArchive pluginArchive) {
        return pluginArchive.isEntryExist(new Archive.EntryFilter() {
            @Override
            public boolean matches(Archive.Entry entry) {
                return !entry.isDirectory()
                       && entry.getName().equals(Constants.ARK_PLUGIN_MARK_ENTRY);
            }
        });
    }
}