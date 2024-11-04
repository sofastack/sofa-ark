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

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.internal.impldep.org.bouncycastle.pqc.crypto.newhope.NHSecretKeyProcessor.PartyUBuilder;
import org.gradle.util.GradleVersion;

public class SofaArkGradlePlugin implements Plugin<Project> {

    public static final String ARK_VERSION = "2.2.14";
    public static final String ARK_BIZ_TASK_NAME = "arkJar";
    public static final String DEVELOPMENT_ONLY_CONFIGURATION_NAME = "developmentOnly";
    public static final String PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME = "productionRuntimeClasspath";
    public static final String ARK_BOOTSTRAP = "com.alipay.sofa:sofa-ark-all:";

    @Override
    public void apply(Project project) {
        verifyGradleVersion();
        createAndSetExtension(project);
        registerPluginActions(project);
    }

    private void verifyGradleVersion() {
        GradleVersion currentVersion = GradleVersion.current();
        if (currentVersion.compareTo(GradleVersion.version("6.8")) < 0) {
            throw new GradleException("Spring Boot plugin requires Gradle 6.8.+ "
                + "The current version is " + currentVersion);
        }
    }

    private void createAndSetExtension(Project project) {
        project.getExtensions().create("arkConfig", SofaArkGradlePluginExtension.class, project);
    }

    private void  registerPluginActions(Project project) {
        ArkPluginAction arkAction =  new ArkPluginAction();
        arkAction.execute(project);
    }

}
