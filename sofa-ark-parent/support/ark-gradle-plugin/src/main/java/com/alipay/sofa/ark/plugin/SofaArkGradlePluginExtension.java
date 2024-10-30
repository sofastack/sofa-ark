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
package com.alipay.sofa.ark.plugin;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

abstract public class SofaArkGradlePluginExtension {

    private final String ARK_JAR_PLUGIN_VERSION = "2.2.14";

    private final Integer PRIORITY = 100;

    private final String ARK_CLASSIFIER = "ark-executable";

    private final String FINAL_NAME = "";
    private final String BIZ_NAME = "";
    private final String BIZ_CLASSIFIER = "ark-biz";

    private final String WEB_CONTEXT_PATH = "/";

    private final Property<String> mainClass;

    public SofaArkGradlePluginExtension(Project project){

        this.mainClass = project.getObjects().property(String.class);

        getPriority().convention(project.provider(() -> PRIORITY));
        getArkClassifier().convention(project.provider(() -> ARK_CLASSIFIER));
        getFinalName().convention(project.provider(() -> FINAL_NAME));
        getBizName().convention(project.provider(() -> BIZ_NAME));
        getBizClassifier().convention(project.provider(() -> BIZ_CLASSIFIER));

        getBizVersion().convention(project.provider(() -> project.getVersion().toString()));
        getWebContextPath().convention(project.provider(()-> WEB_CONTEXT_PATH));

        getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("libs"));
    }

    public Property<String> getMainClass() {
        return this.mainClass;
    }

    @OutputDirectory
    abstract public DirectoryProperty getOutputDirectory();

    abstract public Property<String> getFinalName();

    abstract public Property<String> getArkClassifier();

    abstract public Property<String> getWebContextPath();


    abstract public Property<String> getBizName();
    abstract public Property<String> getBizClassifier();
    abstract public Property<String> getBizVersion();

    abstract public Property<Integer> getPriority();

    @Optional
    abstract public SetProperty<String> getExcludes();
    @Optional
    abstract public SetProperty<String> getExcludeArtifactIds();
    @Optional
    abstract public SetProperty<String> getExcludeGroupIds();
    @Optional
    abstract public SetProperty<String> getDenyImportPackages();
    @Optional
    abstract public SetProperty<String> getDenyImportClasses();
    @Optional
    abstract public SetProperty<String> getDenyImportResources();
    @Optional
    abstract public SetProperty<String> getInjectPluginDependencies();
    @Optional
    abstract public SetProperty<String> getInjectPluginExportPackages();

}
