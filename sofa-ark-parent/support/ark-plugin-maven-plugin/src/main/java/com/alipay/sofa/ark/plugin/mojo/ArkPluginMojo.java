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

import java.io.*;
import java.util.*;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.tools.ArtifactItem;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
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

    @Parameter(defaultValue = "1000", property = "sofa.ark.plugin.priority")
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
    protected LinkedHashSet<String> excludes          = new LinkedHashSet<>();

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
     * whether install ark-plugin to local maven repository, if 'true',
     * it will be installed when execute 'mvn install' or 'mvn deploy';
     * default set 'true'.
     */
    @Parameter(defaultValue = "true")
    private Boolean                 attach;

    private static final String     ARCHIVE_MODE      = "zip";
    private static final String     PLUGIN_SUFFIX     = ".ark.plugin";
    private static final String     PLUGIN_CLASSIFIER = "ark-plugin";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

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
        if (destination.exists()) {
            destination.delete();
        }
        archiver.setDestFile(destination);

        Set<Artifact> artifacts = project.getArtifacts();

        artifacts = filterExcludeArtifacts(artifacts);

        Set<Artifact> conflictArtifacts = filterConflictArtifacts(artifacts);

        addArkPluginArtifact(archiver, artifacts, conflictArtifacts);

        addArkPluginConfig(archiver);

        try {
            archiver.createArchive();
        } catch (ArchiverException e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage());
        }
        if (isAttach()) {
            projectHelper.attachArtifact(project, destination, getClassifier());
        }
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
        addArtifact(archiver, project.getArtifact(), conflicts.contains(project.getArtifact()));
        for (Artifact artifact : dependencies) {
            addArtifact(archiver, artifact, conflicts.contains(artifact));
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

    /**
     * default ark plugin artifact classifier: ark-plugin
     *
     * @return ark plugin artifact classifier
     */
    protected String getClassifier() {
        return PLUGIN_CLASSIFIER;
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
     * generate ark.plugin configuration file
     * archive
     * @param archiver
     * @throws MojoExecutionException
     */
    protected void addArkPluginConfig(Archiver archiver) throws MojoExecutionException {
        addManifest(archiver);
        addExportIndex(archiver);
        addArkPluginMark(archiver);
    }

    private void addManifest(Archiver archiver) throws MojoExecutionException {
        LinkedProperties properties = new LinkedProperties();

        properties.setProperty("groupId", groupId);
        properties.setProperty("artifactId", artifactId);
        properties.setProperty("version", version);
        properties.setProperty("priority", String.valueOf(priority));
        properties.setProperty("pluginName", pluginName);
        properties.setProperty("activator", activator == null ? "" : activator);
        properties.putAll(collectArkPluginImport());
        properties.putAll(collectArkPluginExport());

        addArkPluginConfig(archiver, "META-INF/MANIFEST.MF", properties);
    }

    private Properties collectArkPluginExport() {
        Properties properties = new LinkedProperties();
        if (exported == null) {
            exported = new ExportConfig();
        }
        exported.store(properties);
        return properties;
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

    private void addArkPluginConfig(Archiver archiver, String path, String content)
                                                                                   throws MojoExecutionException {

        File file = new File(workDirectory.getPath() + File.separator + path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        PrintStream outputStream = null;
        try {
            outputStream = new PrintStream(file, "UTF-8");
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.toString());
        } catch (UnsupportedEncodingException e) {
            throw new MojoExecutionException(e.toString());
        }
        try {
            outputStream.println(content);
        } finally {
            outputStream.close();
        }
        archiver.addFile(file, path);
    }

    private void addExportIndex(Archiver archiver) throws MojoExecutionException {
        List<ArchiveEntry> archiveEntries = getLibFileArchiveEntries(archiver);
        List<String> exportClasses = new ArrayList<>();

        for (ArchiveEntry archiveEntry : archiveEntries) {
            if (this.exported.getPackages() != null) {
                exportClasses.addAll(scanJar(archiveEntry, this.exported.getPackages()));
            }
        }

        if (this.exported.getClasses() != null) {
            exportClasses.addAll(this.exported.getClasses());
        }

        StringBuilder sb = new StringBuilder(8092);
        for (String clazz : exportClasses) {
            sb.append(clazz).append('\n');
        }

        addArkPluginConfig(archiver, "conf/export.index", sb.toString());
        this.getLog().info(
            String.format("Generate conf/export.index, total export classes count: %d",
                exportClasses.size()));
    }

    private void addArkPluginMark(Archiver archiver) throws MojoExecutionException {
        addArkPluginConfig(archiver, Constants.ARK_PLUGIN_MARK_ENTRY, new LinkedProperties());
    }

    private List<ArchiveEntry> getLibFileArchiveEntries(Archiver archiver) {
        ResourceIterator resourceIterator = archiver.getResources();
        List<ArchiveEntry> result = new ArrayList<>();
        while (resourceIterator.hasNext()) {
            ArchiveEntry archiveEntry = resourceIterator.next();
            if (archiveEntry.getType() == ArchiveEntry.FILE) {
                String name = archiveEntry.getName();
                if (name.startsWith("lib/") && name.endsWith(".jar")) {
                    if (name.indexOf("/") == name.lastIndexOf("/") && name.contains("/")) {
                        result.add(archiveEntry);
                    }
                }
            }
        }
        return result;
    }

    /**
     * scan all class name contained by package
     *
     * @param archiveEntry ark plugin dependencies
     * @param pkgs exported packages
     * @throws MojoExecutionException
     */
    private List<String> scanJar(ArchiveEntry archiveEntry, Set<String> pkgs)
                                                                             throws MojoExecutionException {
        List<String> classes = new ArrayList<>();
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(archiveEntry.getInputStream());
            LinkedHashMap<String, ZipEntry> allEntries = new LinkedHashMap<>();

            ZipEntry iteratorEntry;
            while ((iteratorEntry = zip.getNextEntry()) != null) {
                allEntries.put(iteratorEntry.getName(), iteratorEntry);
            }

            for (Map.Entry<String, ZipEntry> entry : allEntries.entrySet()) {
                String className = convertClassName(entry.getValue());
                if (className != null) {
                    if (classMatchPackage(className, pkgs)) {
                        classes.add(className);
                        int index = className.lastIndexOf('.');
                        if (index != -1) {
                            String packageEntryName = className.substring(0, index).replace('.',
                                '/')
                                                      + "/";
                            ZipEntry packageEntry = allEntries.get(packageEntryName);
                            if (packageEntry == null || !packageEntry.isDirectory()) {
                                throw new MojoExecutionException(
                                    String
                                        .format(
                                            "packageEntry: %s do not exist, do not exist. please check the jar file: %s",
                                            packageEntryName, archiveEntry.getName()));
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("", e);
        } finally {
            IOUtils.closeQuietly(zip);
        }
        this.getLog().info(
            String.format("Export jar: %s, export classes count: %d",
                new File(archiveEntry.getName()).getName(), classes.size()));
        return classes;
    }

    /**
     * get class name from ZipEntry
     *
     * @param entry ZipEntry
     * @return class
     */
    private static String convertClassName(ZipEntry entry) {
        if (entry.isDirectory()) {
            return null;
        }

        String entryName = entry.getName();
        if (!entryName.endsWith(".class")) {
            return null;
        }

        if (entryName.charAt(0) == '/') {
            entryName = entryName.substring(1);
        }
        entryName = entryName.replace("/", ".");
        return entryName.substring(0, entryName.length() - ".class".length());
    }

    /**
     * check className is contained by packages
     *
     * @param className
     * @param pkgs
     * @return
     */
    private static boolean classMatchPackage(String className, Set<String> pkgs) {
        if (className == null || pkgs == null || pkgs.isEmpty()) {
            return false;
        }

        for (String pkg : pkgs) {
            if (className.startsWith(pkg)) {
                return true;
            }
        }
        return false;
    }
}