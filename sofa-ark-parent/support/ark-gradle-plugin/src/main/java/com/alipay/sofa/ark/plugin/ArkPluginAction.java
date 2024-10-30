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

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;

public class ArkPluginAction implements Action<Project> {

    private static final String PARAMETERS_COMPILER_ARG  = "-parameters";

    @Override
    public void execute(Project project) {
        classifyJarTask(project);
        configureDevelopmentOnlyConfiguration(project);

        project.afterEvaluate(this::configureArkAllArtifact);
        project.afterEvaluate(this::configureBootJarTask);

        configureBuildTask(project);

        project.afterEvaluate(this::configureUtf8Encoding);
        configureParametersCompilerArg(project);
        configureAdditionalMetadataLocations(project);
    }

    private void classifyJarTask(Project project) {
        project.getTasks()
            .named(JavaPlugin.JAR_TASK_NAME, Jar.class)
            .configure((task) -> task.getArchiveClassifier().convention("plain"));
    }


    private void configureArkAllArtifact(Project project) {
        SofaArkGradlePluginExtension arkConfig =  project.getExtensions().getByType(SofaArkGradlePluginExtension.class);

        Configuration runtimeClasspath = project.getConfigurations().getByName("runtimeClasspath");
        Configuration sofaArkConfig = project.getConfigurations().maybeCreate("sofaArkConfig");
        // TODO: configure as the version of sofa-ark
        Dependency arkDependency = project.getDependencies().create(SofaArkGradlePlugin.ARK_BOOTSTRAP+"2.2.14");
        ((ModuleDependency) arkDependency).setTransitive(false);
        sofaArkConfig.getDependencies().add(arkDependency);
        runtimeClasspath.extendsFrom(sofaArkConfig);
    }

    private void configureBuildTask(Project project) {
        project.getTasks()
            .named(BasePlugin.ASSEMBLE_TASK_NAME)
            .configure((task) -> task.dependsOn(project.getTasks().getByName("arkJar")));
    }

    private void configureBootJarTask(Project project) {

        SofaArkGradlePluginExtension arkExtension = project.getExtensions().findByType(SofaArkGradlePluginExtension.class);
        Configuration configuration = project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);

        applyExclusions(configuration, arkExtension);

        SourceSet mainSourceSet = sourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        Configuration developmentOnly = project.getConfigurations()
            .getByName(SofaArkGradlePlugin.DEVELOPMENT_ONLY_CONFIGURATION_NAME);
        Configuration productionRuntimeClasspath = project.getConfigurations()
            .getByName(SofaArkGradlePlugin.PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        Callable<FileCollection> classpath = () -> mainSourceSet.getRuntimeClasspath()
            .minus((developmentOnly.minus(productionRuntimeClasspath)))
            .filter(new JarTypeFileSpec());


        TaskProvider<ResolveMainClassName> resolveMainClassName = ResolveMainClassName
            .registerForTask(SofaArkGradlePlugin.ARK_BIZ_TASK_NAME, project, classpath);


        project.getTasks().register(SofaArkGradlePlugin.ARK_BIZ_TASK_NAME, ArkJar.class, (arkJar) -> {
            arkJar.setDescription(
                "Assembles an executable jar archive containing the main classes and their dependencies.");
            arkJar.setGroup(BasePlugin.BUILD_GROUP);
            arkJar.classpath(classpath);
            Provider<String> manifestStartClass = project
                .provider(() -> (String) arkJar.getManifest().getAttributes().get("Start-Class"));
            arkJar.getMainClass()
                .convention(resolveMainClassName.flatMap((resolver) -> manifestStartClass.isPresent()
                    ? manifestStartClass : resolveMainClassName.get().readMainClassName()));
        });

    }

    private void applyExclusions(Configuration configuration, SofaArkGradlePluginExtension arkConfig) {
        for (String exclude : arkConfig.getExcludes().get()) {
            String[] parts = exclude.split(":");
            // TODO: compatible with group:module:version
            if (parts.length == 2) {
                Map<String, String> excludeMap = new HashMap<>();
                excludeMap.put("group", parts[0]);
                excludeMap.put("module", parts[1]);
                configuration.exclude(excludeMap);
            }
        }

        arkConfig.getExcludeGroupIds().get().stream()
            .map(groupId -> Collections.singletonMap("group", groupId))
            .forEach(configuration::exclude);

        arkConfig.getExcludeArtifactIds().get().stream()
            .map(artifactId -> Collections.singletonMap("module", artifactId))
            .forEach(configuration::exclude);
    }


    private SourceSetContainer sourceSets(Project project) {
        if (GradleVersion.current().compareTo(GradleVersion.version("7.1")) < 0) {
            return project.getConvention().getPlugin(org.gradle.api.plugins.JavaPluginConvention.class).getSourceSets();
        }
        return project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
    }

    private void configureUtf8Encoding(Project evaluatedProject) {
        evaluatedProject.getTasks().withType(JavaCompile.class).configureEach(this::configureUtf8Encoding);
    }

    private void configureUtf8Encoding(JavaCompile compile) {
        if (compile.getOptions().getEncoding() == null) {
            compile.getOptions().setEncoding("UTF-8");
        }
    }

    private void configureParametersCompilerArg(Project project) {
        project.getTasks().withType(JavaCompile.class).configureEach((compile) -> {
            List<String> compilerArgs = compile.getOptions().getCompilerArgs();
            if (!compilerArgs.contains(PARAMETERS_COMPILER_ARG)) {
                compilerArgs.add(PARAMETERS_COMPILER_ARG);
            }
        });
    }

    private void configureAdditionalMetadataLocations(Project project) {
        project.afterEvaluate((evaluated) -> evaluated.getTasks()
            .withType(JavaCompile.class)
            .configureEach(this::configureAdditionalMetadataLocations));
    }

    private void configureAdditionalMetadataLocations(JavaCompile compile) {
        sourceSets(compile.getProject()).stream()
            .filter((candidate) -> candidate.getCompileJavaTaskName().equals(compile.getName()))
            .map((match) -> match.getResources().getSrcDirs())
            .findFirst()
            .ifPresent((locations) -> compile.doFirst(new AdditionalMetadataLocationsConfigurer(locations)));
    }

    private void configureDevelopmentOnlyConfiguration(Project project) {
        Configuration developmentOnly = project.getConfigurations()
            .create(SofaArkGradlePlugin.DEVELOPMENT_ONLY_CONFIGURATION_NAME);
        developmentOnly
            .setDescription("Configuration for development-only dependencies such as Spring Boot's DevTools.");
        Configuration runtimeClasspath = project.getConfigurations()
            .getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        Configuration productionRuntimeClasspath = project.getConfigurations()
            .create(SofaArkGradlePlugin.PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        AttributeContainer attributes = productionRuntimeClasspath.getAttributes();
        ObjectFactory objectFactory = project.getObjects();
        attributes.attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, Usage.JAVA_RUNTIME));
        attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, Bundling.EXTERNAL));
        attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            objectFactory.named(LibraryElements.class, LibraryElements.JAR));
        productionRuntimeClasspath.setVisible(false);
        productionRuntimeClasspath.setExtendsFrom(runtimeClasspath.getExtendsFrom());
        productionRuntimeClasspath.setCanBeResolved(runtimeClasspath.isCanBeResolved());
        productionRuntimeClasspath.setCanBeConsumed(runtimeClasspath.isCanBeConsumed());
        runtimeClasspath.extendsFrom(developmentOnly);
    }


    private static final class AdditionalMetadataLocationsConfigurer implements Action<Task> {

        private final Set<File> locations;

        private AdditionalMetadataLocationsConfigurer(Set<File> locations) {
            this.locations = locations;
        }

        @Override
        public void execute(Task task) {
            if (!(task instanceof JavaCompile)) {
                return;
            }
            JavaCompile compile = (JavaCompile) task;
            if (hasConfigurationProcessorOnClasspath(compile)) {
                configureAdditionalMetadataLocations(compile);
            }
        }

        private boolean hasConfigurationProcessorOnClasspath(JavaCompile compile) {
            Set<File> files = (compile.getOptions().getAnnotationProcessorPath() != null)
                ? compile.getOptions().getAnnotationProcessorPath().getFiles() : compile.getClasspath().getFiles();
            return files.stream()
                .map(File::getName)
                .anyMatch((name) -> name.startsWith("spring-boot-configuration-processor"));
        }

        private void configureAdditionalMetadataLocations(JavaCompile compile) {
            compile.getOptions()
                .getCompilerArgs()
                .add("-Aorg.springframework.boot.configurationprocessor.additionalMetadataLocations="
                    + StringUtils.collectionToCommaDelimitedString(this.locations));
        }

    }

}
