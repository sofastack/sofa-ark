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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.gradle.api.GradleException;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.internal.file.copy.CopyActionProcessingStream;
import org.gradle.api.internal.file.copy.FileCopyDetailsInternal;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.WorkResult;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.util.GradleVersion;

import com.alipay.sofa.ark.tools.git.GitInfo;
import com.alipay.sofa.ark.tools.git.JGitParser;

public class ArkArchiveSupport {

    private static final byte[] ZIP_FILE_HEADER = new byte[] { 'P', 'K', 3, 4 };

    private static final String BIZ_MARKER = "com/alipay/sofa/ark/biz/mark";
    private static final String PLUGIN_MARKER = "com/alipay/sofa/ark/plugin/mark";
    private static final String CONTAINER_MARK = "com/alipay/sofa/ark/container/mark";

    private static final Set<String> DEFAULT_LAUNCHER_CLASSES;

    static {
        Set<String> defaultLauncherClasses = new HashSet<>();
        defaultLauncherClasses.add("org.springframework.boot.loader.JarLauncher");
        defaultLauncherClasses.add("org.springframework.boot.loader.PropertiesLauncher");
        defaultLauncherClasses.add("org.springframework.boot.loader.WarLauncher");
        DEFAULT_LAUNCHER_CLASSES = Collections.unmodifiableSet(defaultLauncherClasses);
    }

    private final PatternSet requiresUnpack = new PatternSet();

    private final PatternSet exclusions = new PatternSet();

    private final String loaderMainClass;

    private final Spec<FileCopyDetails> librarySpec;

    private final Function<FileCopyDetails, ZipCompression> compressionResolver;

    private final String arkVersion;

    private SofaArkGradlePluginExtension arkExtension;

    private final GitInfo gitInfo;

    private java.util.jar.Manifest arkManifest = new java.util.jar.Manifest();

    private final List<File> pluginFiles = new ArrayList<>();
    private final List<File> bizFiles = new ArrayList<>();
    private List<File> conFile = new ArrayList<>();

    public ArkArchiveSupport(String loaderMainClass, Spec<FileCopyDetails> librarySpec,
        Function<FileCopyDetails, ZipCompression> compressionResolver, File gitDic, SofaArkGradlePluginExtension arkExtension) {
        this.loaderMainClass = loaderMainClass;
        this.librarySpec = librarySpec;
        this.compressionResolver = compressionResolver;
        this.requiresUnpack.include(Specs.satisfyNone());
        // TODO: configure as the version of sofa-ark
        this.arkVersion = "2.2.14";
        this.arkExtension = arkExtension;
        this.gitInfo = JGitParser.parse(gitDic);
        buildArkManifest();
    }


    public void configureBizManifest(Manifest manifest, String mainClass, String classes, String lib, String classPathIndex) {
        Attributes attributes = manifest.getAttributes();
        attributes.putIfAbsent("Start-Class", mainClass);
        attributes.putIfAbsent("Main-Class", mainClass);
        attributes.putIfAbsent("Spring-Boot-Classes", classes);
        buildModuleManifest(manifest);

    }

    public void buildArkManifest(){
        this.arkManifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        this.arkManifest.getMainAttributes().putValue("Main-Class", this.loaderMainClass);
        this.arkManifest.getMainAttributes().putValue("Start-Class", this.loaderMainClass);
        this.arkManifest.getMainAttributes().putValue("Sofa-Ark-Version",this.arkVersion);
        this.arkManifest.getMainAttributes().putValue("Ark-Container-Root","SOFA-ARK/container/");
        this.arkManifest.getMainAttributes().putValue("build-time",
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));

        if (gitInfo != null) {
            this.arkManifest.getMainAttributes().putValue("remote-origin-url", gitInfo.getRepository());
            this.arkManifest.getMainAttributes().putValue("commit-branch", gitInfo.getBranchName());
            this.arkManifest.getMainAttributes().putValue("commit-id", gitInfo.getLastCommitId());
            this.arkManifest.getMainAttributes().putValue("commit-user-name", gitInfo.getLastCommitUser());
            this.arkManifest.getMainAttributes()
                .putValue("commit-user-email", gitInfo.getLastCommitEmail());
            this.arkManifest.getMainAttributes().putValue("COMMIT_TIME", gitInfo.getLastCommitDateTime());
            this.arkManifest.getMainAttributes().putValue("COMMIT_TIMESTAMP",
                String.valueOf(gitInfo.getLastCommitTime()));
            this.arkManifest.getMainAttributes().putValue("build-user", gitInfo.getBuildUser());
            this.arkManifest.getMainAttributes().putValue("build-email", gitInfo.getBuildEmail());
        }
    }

    private void buildModuleManifest(Manifest manifest){
        Attributes attributes = manifest.getAttributes();
        attributes.putIfAbsent("Ark-Biz-Name",this.arkExtension.getBizName().get());
        attributes.putIfAbsent("Ark-Biz-Version",this.arkExtension.getBizVersion().get());
        attributes.putIfAbsent("priority",this.arkExtension.getPriority().get());
        attributes.putIfAbsent("web-context-path", this.arkExtension.getWebContextPath().get());
        attributes.putIfAbsent("deny-import-packages",joinSet(this.arkExtension.getDenyImportPackages().get()));
        attributes.putIfAbsent("deny-import-classes",joinSet(this.arkExtension.getDenyImportClasses().get()));
        attributes.putIfAbsent("deny-import-resources",joinSet(this.arkExtension.getDenyImportResources().get()));
        attributes.putIfAbsent("inject-plugin-dependencies", joinSet(this.arkExtension.getInjectPluginDependencies().get()));
        attributes.putIfAbsent("inject-export-packages",joinSet(this.arkExtension.getInjectPluginExportPackages().get()));
        appendBuildInfo(manifest);
    }

    private String joinSet(Set<String> set) {
        return set != null ? String.join(",", set) : "";
    }


    private void appendBuildInfo(Manifest manifest) {
        Attributes attributes = manifest.getAttributes();
        attributes.putIfAbsent("build-time",  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date()));

        if (gitInfo != null) {
            attributes.putIfAbsent("remote-origin-url", gitInfo.getRepository());
            attributes.putIfAbsent("commit-branch", gitInfo.getBranchName());
            attributes.putIfAbsent("commit-id", gitInfo.getLastCommitId());
            attributes.putIfAbsent("commit-user-name", gitInfo.getLastCommitUser());
            attributes.putIfAbsent("commit-user-email", gitInfo.getLastCommitEmail());
            attributes.putIfAbsent("COMMIT_TIME", gitInfo.getLastCommitDateTime());
            attributes.putIfAbsent("COMMIT_TIMESTAMP", String.valueOf(gitInfo.getLastCommitTime()));
            attributes.putIfAbsent("build-user", gitInfo.getBuildUser());
            attributes.putIfAbsent("build-email", gitInfo.getBuildEmail());
        }

    }

    private String determineSpringBootVersion() {
        String version = getClass().getPackage().getImplementationVersion();
        return (version != null) ? version : "unknown";
    }

    public CopyAction createCopyAction(Jar jar) throws IOException {
        return createCopyAction(jar, null);
    }

    public CopyAction createCopyAction(Jar jar, String layerToolsLocation) throws IOException {
        File bizOutput = getTargetFile(jar, this.arkExtension.getBizClassifier().get());
        File arkOutput = getTargetFile(jar, this.arkExtension.getArkClassifier().get());

        Manifest manifest = jar.getManifest();
        boolean preserveFileTimestamps = jar.isPreserveFileTimestamps();
        Integer dirMode = getDirMode(jar);
        Integer fileMode = getFileMode(jar);
        boolean includeDefaultLoader = isUsingDefaultLoader(jar);
        Spec<FileTreeElement> requiresUnpack = this.requiresUnpack.getAsSpec();
        Spec<FileTreeElement> exclusions = this.exclusions.getAsExcludeSpec();
        Spec<FileCopyDetails> librarySpec = this.librarySpec;
        Function<FileCopyDetails, ZipCompression> compressionResolver = this.compressionResolver;
        String encoding = jar.getMetadataCharset();

        CopyAction action = new ArkBizCopyAction(bizOutput,arkOutput, manifest, preserveFileTimestamps, dirMode, fileMode,
            includeDefaultLoader,  requiresUnpack, exclusions, librarySpec,
            compressionResolver, encoding, this.arkManifest, pluginFiles, bizFiles, conFile);


        return jar.isReproducibleFileOrder() ? new ReproducibleOrderingCopyAction(action) : action;
    }


    private File getTargetFile(Jar jar, String classifier) {
        File outputDir = this.arkExtension.getOutputDirectory()
            .getOrElse(jar.getDestinationDirectory().get())
            .getAsFile();

        if (!outputDir.exists()) {
            boolean created = outputDir.mkdirs();
            if (!created) {
                throw new GradleException("Failed to create output directory: " + outputDir.getAbsolutePath());
            }
            System.out.println("Created output directory: " + outputDir.getAbsolutePath());
        }
        File targetFile = new File(outputDir, getArkBizName(jar, classifier));
        System.out.println("Target file will be created at: " + targetFile.getAbsolutePath());

        return targetFile;
    }

    private String getArkBizName(Jar jar, String classifier){
        String name = "";
        name += maybe(name, jar.getArchiveBaseName().getOrNull());
        name += maybe(name,  jar.getArchiveAppendix().getOrNull());
        name += maybe(name,  jar.getArchiveVersion().getOrNull());
        name += maybe(name,  jar.getArchiveClassifier().getOrNull());
        name += maybe(name,  classifier);
        String extension = jar.getArchiveExtension().getOrNull();
        name += (isTrue(extension) ? "." + extension : "");
        return name;
    }

    private Boolean isTrue(Object object){
        if (object instanceof String) {
            return !((String) object).isEmpty();
        }
        return false;
    }

    private String maybe(String prefix, String value) {
        if (isTrue(value)) {
            return isTrue(prefix) ? "-".concat(value) : value;
        } else {
            return "";
        }
    }


    private Integer getDirMode(CopySpec copySpec) {
        return getMode(copySpec, "getDirPermissions", copySpec::getDirMode);
    }

    private Integer getFileMode(CopySpec copySpec) {
        return getMode(copySpec, "getFilePermissions", copySpec::getFileMode);
    }

    @SuppressWarnings("unchecked")
    private Integer getMode(CopySpec copySpec, String methodName, Supplier<Integer> fallback) {
        if (GradleVersion.current().compareTo(GradleVersion.version("8.3")) >= 0) {
            try {
                Object filePermissions = ((Property<Object>) copySpec.getClass().getMethod(methodName).invoke(copySpec))
                    .getOrNull();
                return (filePermissions != null)
                    ? (int) filePermissions.getClass().getMethod("toUnixNumeric").invoke(filePermissions) : null;
            }
            catch (Exception ex) {
                throw new GradleException("Failed to get permissions", ex);
            }
        }
        return fallback.get();
    }

    private boolean isUsingDefaultLoader(Jar jar) {
        return DEFAULT_LAUNCHER_CLASSES.contains(jar.getManifest().getAttributes().get("Main-Class"));
    }

    void requiresUnpack(String... patterns) {
        this.requiresUnpack.include(patterns);
    }

    void requiresUnpack(Spec<FileTreeElement> spec) {
        this.requiresUnpack.include(spec);
    }

    void excludeNonZipLibraryFiles(FileCopyDetails details) {
        if (this.librarySpec.isSatisfiedBy(details)) {
            excludeNonZipFiles(details);
        }
    }

    public void excludeNonZipFiles(FileCopyDetails details) {
        if (!isZip(details.getFile())  || isSofaArk(details.getFile())) {
            details.exclude();
        }
    }

    private boolean isZip(File file) {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                return isZip(fileInputStream);
            }
        }
        catch (IOException ex) {
            return false;
        }
    }

    private boolean isZip(InputStream inputStream) throws IOException {
        for (byte headerByte : ZIP_FILE_HEADER) {
            if (inputStream.read() != headerByte) {
                return false;
            }
        }
        return true;
    }


    private boolean isSofaArk(File jarFile){
        try (JarFile jar = new JarFile(jarFile)) {
            for (JarEntry entry : Collections.list(jar.entries())) {
                if (entry.getName().contains(BIZ_MARKER)) {
                    bizFiles.add(jarFile);
                    return true;
                } else if (entry.getName().contains(PLUGIN_MARKER)) {
                    pluginFiles.add(jarFile);
                    return true;
                } else if (entry.getName().contains(CONTAINER_MARK)){
                    conFile.add(jarFile);
                    return true;
                }
            }
        } catch (IOException e) {

        }
        return false;
    }


    public void moveModuleInfoToRoot(CopySpec spec) {
        spec.filesMatching("module-info.class", this::moveToRoot);
    }

    public void moveToRoot(FileCopyDetails details) {
        details.setRelativePath(details.getRelativeSourcePath());
    }

    /**
     * {@link CopyAction} variant that sorts entries to ensure reproducible ordering.
     */
    private static final class ReproducibleOrderingCopyAction implements CopyAction {

        private final CopyAction delegate;

        private ReproducibleOrderingCopyAction(CopyAction delegate) {
            this.delegate = delegate;
        }

        @Override
        public WorkResult execute(CopyActionProcessingStream stream) {
            return this.delegate.execute((action) -> {
                Map<RelativePath, FileCopyDetailsInternal> detailsByPath = new TreeMap<>();
                stream.process((details) -> detailsByPath.put(details.getRelativePath(), details));
                detailsByPath.values().forEach(action::processFile);
            });
        }

    }
}
