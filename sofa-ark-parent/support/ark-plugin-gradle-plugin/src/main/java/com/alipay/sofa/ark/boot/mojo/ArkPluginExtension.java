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
package com.alipay.sofa.ark.boot.mojo;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

abstract public class ArkPluginExtension {

    public ArkPluginExtension(Project project){
        getPriority().convention(project.provider(() -> "100"));
        getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("libs"));
        getPluginName().convention(project.getName());
        getDescription().convention("");
        getActivator().convention("");
    }

    abstract public SetProperty<String> getShades();
    abstract public SetProperty<String> getExcludeArtifactIds();
    abstract public SetProperty<String> getExcludeGroupIds();
    abstract public SetProperty<String> getExcludes();
    abstract public Property<String> getActivator();
    abstract public Property<String>  getPriority();
    abstract public Property<String>  getPluginName();

    abstract public Property<String> getDescription();
    abstract public DirectoryProperty getOutputDirectory();
    abstract public Property<Boolean>  getAttach();

    private final ImportedConfig imported = new ImportedConfig();
    private final ExportedConfig exported = new ExportedConfig();

    public ImportedConfig getImported() {
        return imported;
    }

    public void imported(Action<? super ImportedConfig> action) {
        action.execute(imported);
    }

    public ExportedConfig getExported() {
        return exported;
    }

    public void exported(Action<? super ExportedConfig> action) {
        action.execute(exported);
    }

    public static class ImportedConfig extends BaseConfig{

    }

    public static class ExportedConfig extends BaseConfig{

    }

}
