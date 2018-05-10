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
package com.alipay.sofa.ark.container.model;

import com.alipay.sofa.ark.common.util.ClassloaderUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;

import java.net.URL;
import java.util.Set;

/**
 * ARk Plugin Standard Model
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class PluginModel implements Plugin {

    private String          pluginName;

    private String          groupId;

    private String          artifactId;

    private String          version;

    private int             priority;

    private Set<String>     exportPackages;

    private Set<String>     exportClasses;

    private Set<String>     importPackages;

    private Set<String>     importClasses;

    private Set<String>     exportIndex;

    private Set<String>     importResources;

    private Set<String>     exportResources;

    private String          activator;

    private URL[]           urls;

    private ClassLoader     pluginClassLoader;

    private PluginContext   pluginContext;

    private PluginActivator pluginActivator;

    public PluginModel setPluginName(String pluginName) {
        this.pluginName = pluginName;
        return this;
    }

    public PluginModel setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public PluginModel setArtifactId(String artifactId) {
        this.artifactId = artifactId;
        return this;
    }

    public PluginModel setVersion(String version) {
        this.version = version;
        return this;
    }

    public PluginModel setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public PluginModel setPluginActivator(String activator) {
        this.activator = activator;
        return this;
    }

    public PluginModel setClassPath(URL[] urls) {
        this.urls = urls;
        return this;
    }

    public PluginModel setExportPackages(String exportPackages) {
        this.exportPackages = StringUtils.strToSet(exportPackages, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setExportClasses(String exportClasses) {
        this.exportClasses = StringUtils.strToSet(exportClasses, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setImportPackages(String importPackages) {
        this.importPackages = StringUtils.strToSet(importPackages, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setImportClasses(String importClasses) {
        this.importClasses = StringUtils.strToSet(importClasses, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setExportIndex(Set<String> exportIndex) {
        this.exportIndex = exportIndex;
        return this;
    }

    public PluginModel setImportResources(String importResources) {
        this.importResources = StringUtils
            .strToSet(importResources, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setExportResources(String exportResources) {
        this.exportResources = StringUtils
            .strToSet(exportResources, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public PluginModel setPluginClassLoader(ClassLoader classLoader) {
        this.pluginClassLoader = classLoader;
        return this;
    }

    public PluginModel setPluginContext(PluginContext context) {
        this.pluginContext = context;
        return this;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public String getPluginName() {
        return this.pluginName;
    }

    @Override
    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String getArtifactId() {
        return this.artifactId;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getPluginActivator() {
        return this.activator;
    }

    @Override
    public URL[] getClassPath() {
        return this.urls;
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return this.pluginClassLoader;
    }

    @Override
    public PluginContext getPluginContext() {
        return this.pluginContext;
    }

    @Override
    public Set<String> getExportPackages() {
        return this.exportPackages;
    }

    @Override
    public Set<String> getExportClasses() {
        return this.exportClasses;
    }

    @Override
    public Set<String> getImportPackages() {
        return this.importPackages;
    }

    @Override
    public Set<String> getImportClasses() {
        return this.importClasses;
    }

    @Override
    public Set<String> getExportIndex() {
        return this.exportIndex;
    }

    @Override
    public Set<String> getImportResources() {
        return importResources;
    }

    @Override
    public Set<String> getExportResources() {
        return exportResources;
    }

    @Override
    public void start() throws ArkException {
        if (activator == null || activator.isEmpty()) {
            return;
        }

        ClassLoader oldClassloader = ClassloaderUtils
            .pushContextClassloader(this.pluginClassLoader);
        try {
            pluginActivator = (PluginActivator) pluginClassLoader.loadClass(activator)
                .newInstance();
            pluginActivator.start(pluginContext);
        } catch (Throwable ex) {
            throw new ArkException(ex.getMessage(), ex);
        } finally {
            ClassloaderUtils.popContextClassloader(oldClassloader);
        }
    }

    @Override
    public void stop() throws ArkException {
        pluginActivator.stop(pluginContext);
    }
}