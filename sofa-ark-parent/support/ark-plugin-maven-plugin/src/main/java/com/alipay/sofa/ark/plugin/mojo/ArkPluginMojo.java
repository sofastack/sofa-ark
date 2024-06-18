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
package com.alipay.sofa.ark.plugin.mojo;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.tools.ArtifactItem;
import com.alipay.sofa.ark.tools.JarWriter;
import com.alipay.sofa.ark.tools.Repackager;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
@Mojo(name = "ark-plugin", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ArkPluginMojo extends AbstractMojo {

    @Component
    protected MavenProject          project;

    @Component
    protected ArchiverManager       archiverManager;

    @Component
    protected MavenProjectHelper    projectHelper;

    /**
     * The location of the generated ark plugin
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "sofa.ark.plugin.repository")
    protected File                  outputDirectory;

    /**
     * The location of sofa-ark-maven-plugin temporary file
     */
    @Parameter(defaultValue = "${project.build.directory}/sofa-ark-maven-plugin")
    protected File                  workDirectory;

    /**
     * The configuration of ark plugin
     */
    @Parameter(defaultValue = "${project.groupId}", readonly = true)
    protected String                groupId;

    @Parameter(defaultValue = "${project.artifactId}", readonly = true)
    protected String                artifactId;

    @Parameter(defaultValue = "${project.version}", readonly = true)
    protected String                version;

    @Parameter(defaultValue = "${project.artifactId}")
    public String                   pluginName;

    @Parameter(defaultValue = " ")
    protected String                description;

    @Parameter(defaultValue = "100", property = "sofa.ark.plugin.priority")
    protected Integer               priority;

    @Parameter
    protected String                activator;

    @Parameter
    protected ExportConfig          exported;

    @Parameter
    protected ImportConfig          imported;

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match)
     */
    @Parameter(defaultValue = "")
    protected LinkedHashSet<String> excludes           = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    protected LinkedHashSet<String> excludeGroupIds;

    /**
     * list of artifact names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    protected LinkedHashSet<String> excludeArtifactIds;

    /**
     * Colon separated groupId, artifactId, classifier(optional), version.
     */
    @Parameter(defaultValue = "")
    protected LinkedHashSet<String> shades             = new LinkedHashSet<>();

    /**
     * whether install ark-plugin to local maven repository, if 'true',
     * it will be installed when execute 'mvn install' or 'mvn deploy';
     * default set 'true'.
     */
    @Parameter(defaultValue = "true")
    private Boolean                 attach;

    /**
     * default ark plugin artifact classifier: empty
     */
    @Parameter(defaultValue = "")
    private String                  classifier;

    /**
     * Export plugin project classes by default
     */
    @Parameter(defaultValue = "true")
    protected Boolean               exportPluginClass;

    private static final String     ARCHIVE_MODE       = "zip";
    private static final String     PLUGIN_SUFFIX      = ".ark.plugin";
    private static final String     TEMP_PLUGIN_SUFFIX = ".ark.plugin.bak";

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws MojoExecutionException {

        Archiver archiver;

        try {
            archiver = getArchiver();
        } catch (NoSuchArchiverException e) {
            throw new MojoExecutionException(e.getMessage());
        }

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        String fileName = getFileName();
        File destination = new File(outputDirectory, fileName);
        File tmpDestination = new File(outputDirectory, getTempFileName());
        if (destination.exists()) {
            destination.delete();
        }
        if (tmpDestination.exists()) {
            tmpDestination.delete();
        }
        archiver.setDestFile(tmpDestination);

        Set<Artifact> artifacts = project.getArtifacts();

        artifacts = filterExcludeArtifacts(artifacts);

        Set<Artifact> conflictArtifacts = filterConflictArtifacts(artifacts);

        addArkPluginArtifact(archiver, artifacts, conflictArtifacts);

        addArkPluginConfig(archiver);

        try {
            archiver.createArchive();
            shadeJarIntoArkPlugin(destination, tmpDestination, artifacts);
        } catch (ArchiverException | IOException e) {
            throw new MojoExecutionException(e.getMessage());
        } finally {
            tmpDestination.delete();
        }
        if (isAttach()) {
            if (StringUtils.isEmpty(classifier)) {
                Artifact artifact = project.getArtifact();
                artifact.setFile(destination);
                project.setArtifact(artifact);
            } else {
                projectHelper.attachArtifact(project, destination, classifier);
            }
        }
    }

    public void shadeJarIntoArkPlugin(File pluginFile, File tmpPluginFile, Set<Artifact> artifacts)
                                                                                                   throws IOException {
        Set<Artifact> shadeJars = new HashSet<>();
        shadeJars.add(project.getArtifact());
        for (Artifact artifact : artifacts) {
            if (isShadeJar(artifact)) {
                shadeJars.add(artifact);
            }
        }

        JarWriter writer = new JarWriter(pluginFile);
        JarFile tmpJarFile = new JarFile(tmpPluginFile);
        try {
            writer.writeEntries(tmpJarFile);
            for (Artifact jar : shadeJars) {
                writer.writeEntries(new JarFile(jar.getFile()));
            }
        } finally {
            writer.close();
            tmpJarFile.close();
        }
    }

    public LinkedHashSet<String> getShades() {
        return shades;
    }

    public void setShades(LinkedHashSet<String> shades) {
        this.shades = shades;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public boolean isShadeJar(Artifact artifact) {
        for (String shade : getShades()) {
            ArtifactItem artifactItem = ArtifactItem.parseArtifactItemWithVersion(shade);
            if (!artifact.getGroupId().equals(artifactItem.getGroupId())) {
                continue;
            }
            if (!artifact.getArtifactId().equals(artifactItem.getArtifactId())) {
                continue;
            }
            if (!artifact.getVersion().equals(artifactItem.getVersion())) {
                continue;
            }
            if (!StringUtils.isEmpty(artifactItem.getClassifier())
                && !artifactItem.getClassifier().equals(artifact.getClassifier())) {
                continue;
            }
            if (artifact.getArtifactId().equals(project.getArtifactId())
                && artifact.getGroupId().equals(project.getGroupId())) {
                throw new RuntimeException("Can't shade jar-self.");
            }
            return true;
        }
        return false;
    }

    /**
     * put all dependencies together into archive
     *
     * @param archiver ark plugin archiver
     * @param dependencies all dependencies of ark plugin
     * @param conflicts dependencies whose jar name (artifact id) is conflict
     */
    protected void addArkPluginArtifact(Archiver archiver, Set<Artifact> dependencies,
                                        Set<Artifact> conflicts) {
        for (Artifact artifact : dependencies) {
            if (Repackager.isZip(artifact.getFile())) {
                addArtifact(archiver, artifact, conflicts.contains(artifact));
            }
        }
    }

    /**
     * add a artifact into archiver
     *
     * @param archiver archiver which represents a ark plugin
     * @param artifact artifact which will be put into archiver
     * @param artifactIdConflict whether artifact is conflicted, it will determine the final jar name.
     */
    protected void addArtifact(Archiver archiver, Artifact artifact, boolean artifactIdConflict) {
        if (isShadeJar(artifact)) {
            return;
        }
        String destination = artifact.getFile().getName();
        if (artifactIdConflict) {
            destination = artifact.getGroupId() + "-" + destination;
        }
        destination = "lib/" + destination;
        getLog().debug("  " + artifact + " => " + destination);
        archiver.addFile(artifact.getFile(), destination);
    }

    /**
     * compute conflict artifacts
     *
     * @param artifacts all dependencies of project
     * @return artifacts whose jar name (artifact id) is conflict
     */
    protected Set<Artifact> filterConflictArtifacts(Set<Artifact> artifacts) {
        HashMap<String, Artifact> existArtifacts = new HashMap<>(Collections.singletonMap(project
            .getArtifact().getArtifactId(), project.getArtifact()));
        HashSet<Artifact> conflictArtifacts = new HashSet<>();

        for (Artifact artifact : artifacts) {
            if (existArtifacts.containsKey(artifact.getArtifactId())) {
                conflictArtifacts.add(artifact);
                conflictArtifacts.add(existArtifacts.get(artifact.getArtifactId()));
            } else {
                existArtifacts.put(artifact.getArtifactId(), artifact);
            }
        }

        return conflictArtifacts;
    }

    /**
     * filter the excluded dependencies
     *
     * @param artifacts all dependencies of project
     * @return dependencies excluded the excludes config
     */
    protected Set<Artifact> filterExcludeArtifacts(Set<Artifact> artifacts) {
        List<ArtifactItem> excludeList = new ArrayList<>();
        for (String exclude : excludes) {
            ArtifactItem item = ArtifactItem.parseArtifactItemIgnoreVersion(exclude);
            excludeList.add(item);
        }

        Set<Artifact> result = new LinkedHashSet<>();
        for (Artifact e : artifacts) {
            boolean isExclude = false;

            for (ArtifactItem exclude : excludeList) {
                if (exclude.isSameIgnoreVersion(ArtifactItem.parseArtifactItem(e))) {
                    isExclude = true;
                    break;
                }
            }

            if (excludeGroupIds != null && excludeGroupIds.contains(e.getGroupId())) {
                isExclude = true;
            }

            if (excludeArtifactIds != null && excludeArtifactIds.contains(e.getArtifactId())) {
                isExclude = true;
            }

            if (!isExclude) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * create a zip archiver
     *
     * @return a un-compress zip archiver
     * @throws NoSuchArchiverException
     */
    protected Archiver getArchiver() throws NoSuchArchiverException {
        Archiver archiver = archiverManager.getArchiver(ARCHIVE_MODE);
        ((AbstractZipArchiver) archiver).setCompress(false);
        return archiver;
    }

    /**
     * repackage jar name
     *
     * @return ark plugin name
     */
    protected String getFileName() {
        return String.format("%s%s", pluginName, PLUGIN_SUFFIX);
    }

    protected String getTempFileName() {
        return String.format("%s%s", pluginName, TEMP_PLUGIN_SUFFIX);
    }

    /**
     * check whether install ark plugin install local repo
     * default true.
     *
     * @return whether install local repo
     */
    protected boolean isAttach() {
        return attach;
    }

    /**
     * check whether to export plugin project
     * default true.
     *
     * @return whether to export plugin project
     */
    protected boolean getExportPluginClass() {
        return exportPluginClass;
    }

    /**
     * generate ark.plugin configuration file
     * archive
     * @param archiver
     * @throws MojoExecutionException
     */
    protected void addArkPluginConfig(Archiver archiver) throws MojoExecutionException {
        addManifest(archiver);
        addArkPluginMark(archiver);
    }

    private void addManifest(Archiver archiver) throws MojoExecutionException {
        LinkedProperties properties = new LinkedProperties();

        properties.setProperty("groupId", groupId);
        properties.setProperty("artifactId", artifactId);
        properties.setProperty("version", version);
        properties.setProperty("priority", String.valueOf(priority));
        properties.setProperty("pluginName", pluginName);
        properties.setProperty("description", description);
        properties.setProperty("activator", activator == null ? "" : activator);
        properties.putAll(collectArkPluginImport());
        properties.putAll(collectArkPluginExport());

        addArkPluginConfig(archiver, "META-INF/MANIFEST.MF", properties);
    }

    private Properties collectArkPluginExport() throws MojoExecutionException {
        Properties properties = new LinkedProperties();
        if (exported == null) {
            exported = new ExportConfig();
        }
        if (exportPluginClass) {
            Set<String> projectClasses = findProjectClasses();
            for (String projectClass : projectClasses) {
                if (!StringUtils.isEmpty(projectClass)) {
                    exported.addClass(projectClass);
                }
            }
        }
        exported.store(properties);
        return properties;
    }

    private Set<String> findProjectClasses() throws MojoExecutionException {
        try {
            // Accessing the target/classes directory where compiled classes are located
            File outputDirectory = new File(project.getBuild().getOutputDirectory());
            // Ensure the directory exists
            if (outputDirectory.exists()) {
                Set<String> classes = new HashSet<>(ClassUtils.collectClasses(outputDirectory));
                classes = classes.stream().filter(className -> !className.equals(this.activator)).collect(
                        Collectors.toSet());
                return classes;
            } else {
                getLog().warn("Output directory does not exist!");
            }
            return new HashSet<>();
        } catch (IOException e) {
            throw new MojoExecutionException("Error finding compiled classes", e);
        }
    }

    private Properties collectArkPluginImport() {
        Properties properties = new LinkedProperties();
        if (imported == null) {
            imported = new ImportConfig();
        }
        imported.store(properties);
        return properties;
    }

    private void addArkPluginConfig(Archiver archiver, String path, LinkedProperties properties)
                                                                                                throws MojoExecutionException {

        File file = new File(workDirectory.getPath() + File.separator + path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        PrintStream outputStream = null;
        Manifest manifest = new LinkedManifest();
        manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        Enumeration enumeration = properties.keys();
        while (enumeration.hasMoreElements()) {
            String key = (String) enumeration.nextElement();
            manifest.getMainAttributes().putValue(key, properties.getProperty(key));
        }

        try {
            outputStream = new PrintStream(file, "UTF-8");
            manifest.write(outputStream);
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage());
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        archiver.addFile(file, path);

    }

    private void addArkPluginMark(Archiver archiver) throws MojoExecutionException {
        addArkPluginConfig(archiver, Constants.ARK_PLUGIN_MARK_ENTRY, new LinkedProperties());
    }
}
