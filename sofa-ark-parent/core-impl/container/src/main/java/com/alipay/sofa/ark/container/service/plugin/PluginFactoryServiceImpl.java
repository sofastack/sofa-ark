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

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
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
            .setImportResources(manifestMainAttributes.getValue(IMPORT_RESOURCES_ATTRIBUTE))
            .setExportResources(manifestMainAttributes.getValue(EXPORT_RESOURCES_ATTRIBUTE))
            .setPluginClassLoader(
                new PluginClassLoader(plugin.getPluginName(), plugin.getClassPath()))
            .setPluginContext(new PluginContextImpl(plugin));
        return plugin;
    }

    @Override
    public Plugin createPlugin(PluginArchive pluginArchive, URL[] extensions,
                               Set<String> exportPackages) throws IOException,
                                                          IllegalArgumentException {
        AssertUtils.isTrue(isArkPlugin(pluginArchive), "Archive must be a ark plugin!");
        if (extensions == null || extensions.length == 0) {
            return createPlugin(pluginArchive);
        }

        PluginModel plugin = new PluginModel();
        Attributes manifestMainAttributes = pluginArchive.getManifest().getMainAttributes();
        plugin
            .setPluginName(manifestMainAttributes.getValue(PLUGIN_NAME_ATTRIBUTE))
            .setGroupId(manifestMainAttributes.getValue(GROUP_ID_ATTRIBUTE))
            .setArtifactId(manifestMainAttributes.getValue(ARTIFACT_ID_ATTRIBUTE))
            .setVersion(manifestMainAttributes.getValue(PLUGIN_VERSION_ATTRIBUTE))
            .setPriority(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE))
            .setPluginActivator(manifestMainAttributes.getValue(ACTIVATOR_ATTRIBUTE))
            .setClassPath(getFinalPluginUrls(pluginArchive, extensions, plugin.getPluginName()))
            .setPluginUrl(pluginArchive.getUrl())
            .setExportClasses(manifestMainAttributes.getValue(EXPORT_CLASSES_ATTRIBUTE))
            .setExportPackages(manifestMainAttributes.getValue(EXPORT_PACKAGES_ATTRIBUTE),
                exportPackages)
            .setImportClasses(manifestMainAttributes.getValue(IMPORT_CLASSES_ATTRIBUTE))
            .setImportPackages(manifestMainAttributes.getValue(IMPORT_PACKAGES_ATTRIBUTE))
            .setImportResources(manifestMainAttributes.getValue(IMPORT_RESOURCES_ATTRIBUTE))
            .setExportResources(manifestMainAttributes.getValue(EXPORT_RESOURCES_ATTRIBUTE))
            .setPluginClassLoader(
                new PluginClassLoader(plugin.getPluginName(), plugin.getClassPath()))
            .setPluginContext(new PluginContextImpl(plugin));
        return plugin;
    }

    public URL[] getFinalPluginUrls(PluginArchive pluginArchive, URL[] extensions, String pluginName)
                                                                                                     throws IOException {
        // get config by PLUGIN-EXPORT key
        URL[] urls = pluginArchive.getUrls();
        String excludeArtifact = ArkConfigs.getStringValue(String.format(PLUGIN_EXTENSION_FORMAT,
            pluginName));
        if (StringUtils.isEmpty(excludeArtifact) || extensions == null) {
            return urls;
        }
        pluginArchive.setExtensionUrls(extensions);
        ArrayList<URL> urlList = new ArrayList<>(Arrays.asList(urls));
        List<URL> preRemoveList = new ArrayList<>();
        urlList.remove(null);
        for (URL url : urlList) {
            String[] dependencies = excludeArtifact.split(STRING_SEMICOLON);
            for (String dependency : dependencies) {
                String artifactId = dependency.split(STRING_COLON)[0];
                String version = dependency.split(STRING_COLON)[1];
                if (url.getPath().endsWith(artifactId + "-" + version + ".jar!/")) {
                    preRemoveList.add(url);
                    break;
                }
            }
        }
        urlList.removeAll(preRemoveList);
        if (pluginArchive instanceof JarPluginArchive) {
            URL[] extensionUrls = ((JarPluginArchive) pluginArchive).getExtensionUrls();
            if (extensionUrls != null) {
                urlList.addAll(Arrays.asList(extensionUrls));
            }
        }

        Object[] objects = urlList.toArray();
        urls = new URL[objects.length];
        return urlList.toArray(urls);
    }

    @Override
    public Plugin createPlugin(File file) throws IOException {
        JarFile pluginFile = new JarFile(file);
        JarFileArchive jarFileArchive = new JarFileArchive(pluginFile);
        JarPluginArchive jarPluginArchive = new JarPluginArchive(jarFileArchive);
        return createPlugin(jarPluginArchive);
    }

    @Override
    public Plugin createEmbedPlugin(PluginArchive pluginArchive, ClassLoader masterClassLoader)
                                                                                               throws IOException {
        AssertUtils.isTrue(isArkPlugin(pluginArchive), "Archive must be a ark plugin!");
        PluginModel plugin = new PluginModel();
        Attributes manifestMainAttributes = pluginArchive.getManifest().getMainAttributes();
        boolean enableExportClass = "true".equals(System.getProperty(PLUGIN_EXPORT_CLASS_ENABLE));
        plugin
            .setPluginName(manifestMainAttributes.getValue(PLUGIN_NAME_ATTRIBUTE))
            .setGroupId(manifestMainAttributes.getValue(GROUP_ID_ATTRIBUTE))
            .setArtifactId(manifestMainAttributes.getValue(ARTIFACT_ID_ATTRIBUTE))
            .setVersion(manifestMainAttributes.getValue(PLUGIN_VERSION_ATTRIBUTE))
            .setPriority(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE))
            .setPluginActivator(manifestMainAttributes.getValue(ACTIVATOR_ATTRIBUTE))
            .setClassPath(((URLClassLoader) masterClassLoader).getURLs())
            .setPluginUrl(pluginArchive.getUrl())
            .setExportClasses(
                enableExportClass ? manifestMainAttributes.getValue(EXPORT_CLASSES_ATTRIBUTE)
                    : null)
            .setExportPackages(
                enableExportClass ? manifestMainAttributes.getValue(EXPORT_PACKAGES_ATTRIBUTE)
                    : null)
            .setImportClasses(manifestMainAttributes.getValue(IMPORT_CLASSES_ATTRIBUTE))
            .setImportPackages(manifestMainAttributes.getValue(IMPORT_PACKAGES_ATTRIBUTE))
            .setImportResources(manifestMainAttributes.getValue(IMPORT_RESOURCES_ATTRIBUTE))
            .setExportResources(manifestMainAttributes.getValue(EXPORT_RESOURCES_ATTRIBUTE))
            .setPluginClassLoader(masterClassLoader)
            .setPluginContext(new PluginContextImpl(plugin));
        return plugin;
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