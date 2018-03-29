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

import com.alipay.sofa.ark.container.service.classloader.ClassloaderUtil;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;

import java.net.URL;
import java.util.HashSet;
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

    private Set<String>     importIndex;

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

    public PluginModel setExportPackages(Set<String> exportPackages) {
        this.exportPackages = exportPackages;
        return this;
    }

    public PluginModel setExportClasses(Set<String> exportClasses) {
        this.exportClasses = exportClasses;
        return this;
    }

    public PluginModel setImportPackages(Set<String> importPackages) {
        this.importPackages = importPackages;
        return this;
    }

    public PluginModel setImportClasses(Set<String> importClasses) {
        this.importClasses = importClasses;
        return this;
    }

    public PluginModel setExportIndex(Set<String> exportIndex) {
        this.exportIndex = exportIndex;
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
    public Set<String> getImportIndex() {
        if (this.importIndex == null) {
            this.importIndex = new HashSet<>();
            this.importIndex.addAll(importClasses);
            this.importIndex.addAll(importPackages);
        }
        return this.importIndex;
    }

    @Override
    public void start() throws ArkException {
        if (activator == null || activator.isEmpty()) {
            return;
        }

        ClassLoader oldClassloader = ClassloaderUtil.pushContextClassloader(this.pluginClassLoader);
        try {
            pluginActivator = (PluginActivator) pluginClassLoader.loadClass(activator)
                .newInstance();
            pluginActivator.start(pluginContext);
        } catch (Throwable ex) {
            throw new ArkException(ex.getMessage(), ex);
        } finally {
            ClassloaderUtil.pushContextClassloader(oldClassloader);
        }
    }

    @Override
    public void stop() throws ArkException {
        pluginActivator.stop(pluginContext);
    }
}