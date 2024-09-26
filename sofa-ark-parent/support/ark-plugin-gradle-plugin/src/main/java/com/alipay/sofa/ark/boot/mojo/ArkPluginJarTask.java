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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

public class ArkPluginJarTask extends Jar {

    private final ArkPluginExtension arkPluginExtension;

    private final Set<ResolvedArtifact> filteredArtifacts;
    private final Set<ResolvedArtifact> conflictArtifacts;

    private final Set<File> shadeFiles = new HashSet<>();

    public ArkPluginJarTask(){
        super();
        Project project = getProject();
        arkPluginExtension = project.getExtensions().findByType(ArkPluginExtension.class);

        configureDestination();
        configurePluginJar();

        from(project.getTasks().getByName("classes"));

        Set<ResolvedArtifact> allArtifacts = getProjectArtifacts();
        filteredArtifacts = filterArtifacts1(allArtifacts);
        conflictArtifacts = filterConflictArtifacts(filteredArtifacts);

        configureSourceSet(project);
        configureArtifacts();
        handleConflictArtifacts(conflictArtifacts);

        configureManifest(project);

        addArkPluginMark();
    }

    private void configurePluginJar(){
        getArchiveFileName().set(getProject().provider(() -> {
            String pluginName = arkPluginExtension.getPluginName().get();
            return pluginName + ".jar";
        }));
    }

    private void configureArtifacts() {
        into("lib", copySpec -> {
                copySpec.from(getProject().provider(this::getFilteredArtifactFiles));
            copySpec.rename(this::renameArtifactIfConflict);
        });

        into("", copySpec -> {
            copySpec.from(getProject().provider(() -> shadeFiles));
        });
    }

    private String renameArtifactIfConflict(String fileName) {
        ResolvedArtifact artifact = findArtifactByFileName(fileName);
        if (artifact != null && conflictArtifacts.contains(artifact)) {
            return artifact.getModuleVersion().getId().getGroup() + "-" + fileName;
        }
        return fileName;
    }

    private ResolvedArtifact findArtifactByFileName(String fileName) {
        return filteredArtifacts.stream()
            .filter(artifact -> artifact.getFile().getName().equals(fileName))
            .findFirst()
            .orElse(null);
    }

    private Set<File> getFilteredArtifactFiles() {
        Set<String> shadeNames = arkPluginExtension.getShades().get().stream()
            .map(this::getShadeFileName)
            .collect(Collectors.toSet());

        return filteredArtifacts.stream()
            .filter(artifact -> {
                boolean isShade = shadeNames.contains(artifact.getFile().getName());
                if (isShade) {
                    shadeFiles.add(artifact.getFile());
                }
                return !isShade;
            })
            .map(ResolvedArtifact::getFile)
            .filter(this::isZip)
            .collect(Collectors.toSet());
    }

    private String getShadeFileName(String shade) {
        String[] parts = shade.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid shade format: " + shade);
        }
        String name = parts[1];
        String version = parts[2];
        return name + "-" + version + ".jar";
    }

    private void configureManifest(Project project){
        Provider<Map<String, String>> manifestAttributes = project.provider(() -> {
            Map<String, String> attributes = new HashMap<>();
            attributes.put("Manifest-Version", "1.0");
            attributes.put("Ark-Plugin-Name", arkPluginExtension.getPluginName().get());
            attributes.put("Ark-Plugin-Version", project.getVersion().toString());

            attributes.put("groupId", project.getGroup().toString());
            attributes.put("artifactId", project.getName());
            attributes.put("version", project.getVersion().toString());
            attributes.put("priority", arkPluginExtension.getPriority().get());
            attributes.put("pluginName", arkPluginExtension.getPluginName().get());
            attributes.put("description", arkPluginExtension.getDescription().get());
            attributes.put("activator", arkPluginExtension.getActivator().get());

            attributes.putAll(arkPluginExtension.getImported().toAttributes("import"));
            attributes.putAll(arkPluginExtension.getExported().toAttributes("export"));
            return attributes;
        });

        getManifest().attributes(manifestAttributes.get());
    }

    private void configureArtifacts(Project project, Set<ResolvedArtifact> filteredArtifacts){
        from(project.provider(() -> {
            return filteredArtifacts.stream()
                .map(ResolvedArtifact::getFile)
                .collect(Collectors.toSet());
        }));
    }

    private void configureDestination(){
        getDestinationDirectory().set(arkPluginExtension.getOutputDirectory());
    }
    private void configureSourceSet(Project project){
        SourceSet mainSourceSet = project.getExtensions()
                .getByType(JavaPluginExtension.class)
                .getSourceSets()
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        from(mainSourceSet.getOutput());
    }

    private void configureCopySpec(Project project) {

        from(project.provider(() -> {
            Configuration runtimeClasspath = project.getConfigurations().getByName("runtimeClasspath");
            Set<ResolvedArtifact> artifacts = runtimeClasspath.getResolvedConfiguration().getResolvedArtifacts();
            return filterArtifacts(artifacts);
        }));

    }

    private Set<ResolvedArtifact> filterArtifacts1(Set<ResolvedArtifact> artifacts) {
        return artifacts.stream()
            .filter(this::shouldIncludeArtifact)
            .collect(Collectors.toSet());
    }


    private Set<File> filterArtifacts(Set<ResolvedArtifact> artifacts) {
        return artifacts.stream()
            .filter(this::shouldIncludeArtifact)
            .map(ResolvedArtifact::getFile)
            .collect(Collectors.toSet());
    }

    private boolean shouldIncludeArtifact(ResolvedArtifact artifact) {
        String groupId = artifact.getModuleVersion().getId().getGroup();
        String artifactId = artifact.getName();
        String gav = groupId + ":" + artifactId ;

        if (this.arkPluginExtension.getExcludes().get().contains(gav)) {
            return false;
        }

        if (this.arkPluginExtension.getExcludeGroupIds().get().contains(groupId)) {
            return false;
        }

        if (this.arkPluginExtension.getExcludeArtifactIds().get().contains(artifactId)) {
            return false;
        }

        return true;
    }

    private Set<ResolvedArtifact> getProjectArtifacts() {
        Configuration configuration = getProject().getConfigurations().getByName("runtimeClasspath");
        return configuration.getResolvedConfiguration().getResolvedArtifacts();
    }

    private boolean isZip(File file) {
        try (ZipFile zipFile = new ZipFile(file)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected CopyAction createCopyAction() {
        File jarFile = getArchiveFile().get().getAsFile();
        return new ArkPluginCopyAction(jarFile, shadeFiles);
    }

    @TaskAction
    private void action(){
        super.copy();
    }

    private void addArkPluginMark() {
        String markContent = "this is plugin mark";
        String markPath = "com/alipay/sofa/ark/plugin";

        from(getProject().provider(() -> {
            try {
                File tempFile = File.createTempFile("mark", null);
                tempFile.deleteOnExit();
                Files.write(tempFile.toPath(), markContent.getBytes(StandardCharsets.UTF_8));
                return tempFile;
            } catch (IOException e) {
                throw new RuntimeException("Failed to create mark file", e);
            }
        }), copySpec -> {
            copySpec.into(markPath);
            copySpec.rename(fileName -> "mark");
        });
    }

    protected Set<ResolvedArtifact> filterConflictArtifacts(Set<ResolvedArtifact> artifacts) {
        Project project = getProject();
        String projectArtifactId = project.getName();

        Map<String, ResolvedArtifact> existArtifacts = new HashMap<>();

        existArtifacts.put(projectArtifactId, null); //  ResolvedArtifact

        Set<ResolvedArtifact> conflictArtifacts = new HashSet<>();

        for (ResolvedArtifact artifact : artifacts) {
            String artifactId = artifact.getName();
            if (existArtifacts.containsKey(artifactId)) {
                conflictArtifacts.add(artifact);
                ResolvedArtifact existingArtifact = existArtifacts.get(artifactId);
                if (existingArtifact != null) {
                    conflictArtifacts.add(existingArtifact);
                }
            } else {
                existArtifacts.put(artifactId, artifact);
            }
        }
        return conflictArtifacts;
    }

    private void handleConflictArtifacts(Set<ResolvedArtifact> conflictArtifacts) {
        for (ResolvedArtifact conflictArtifact : conflictArtifacts) {
            getLogger().warn("Conflict artifact found: {}:{}:{}",
                conflictArtifact.getModuleVersion().getId().getGroup(),
                conflictArtifact.getName(),
                conflictArtifact.getModuleVersion().getId().getVersion());
        }
    }

}
