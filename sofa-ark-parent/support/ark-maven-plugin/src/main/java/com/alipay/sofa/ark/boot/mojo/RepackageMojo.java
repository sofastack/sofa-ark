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

import com.alipay.sofa.ark.common.util.ClassUtils;
import com.alipay.sofa.ark.common.util.ParseUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.tools.ArtifactItem;
import com.alipay.sofa.ark.tools.Libraries;
import com.alipay.sofa.ark.tools.Repackager;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_BASE_DIR;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES_ARTIFACTIDS;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES_GROUPIDS;

/**
 * Repackages existing JAR archives so that they can be executed from the command
 * line using {@literal java -jar}.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageMojo extends AbstractMojo {

    private static final String    BIZ_NAME                   = "com.alipay.sofa.ark.bizName";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject           project;

    @Component
    private MavenProjectHelper     projectHelper;

    @Component
    private MavenSession           mavenSession;

    @Component
    private ArtifactFactory        artifactFactory;

    @Component
    private RepositorySystem       repositorySystem;

    /**
     * Directory containing the generated archive
     * @since 0.1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File                   outputDirectory;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                   baseDir;

    @Parameter(defaultValue = "", required = false)
    private String                 packExcludesConfig;

    /**
     * Name of the generated archive
     * @since 0.1.0
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String                 finalName;

    /**
     * Skip the repackage goal.
     * @since 0.1.0
     */
    @Parameter(property = "sofa.ark.repackage.skip", defaultValue = "false")
    private boolean                skip;

    /**
     * Classifier to add to the artifact generated. If attach is set 'true', the
     * artifact will be attached with that classifier. Attaching the artifact
     * allows to deploy it alongside to the main artifact.
     * @since 0.1.0
     */
    @Parameter(defaultValue = "ark-biz", readonly = true)
    private String                 bizClassifier;

    /**
     *
     */
    @Parameter(defaultValue = "${project.artifactId}")
    private String                 bizName;

    /**
     * ark biz version
     * @since 0.4.0
     */
    @Parameter(defaultValue = "${project.version}")
    private String                 bizVersion;

    /**
     * ark biz version
     * @since 0.4.0
     */
    @Parameter(defaultValue = "100", property = "sofa.ark.biz.priority")
    protected Integer              priority;

    /**
     * Classifier to add to the executable artifact generated, if needed,
     * 'sofa-ark' is recommended.
     *
     * @since 0.1.0
     */
    @Parameter(defaultValue = "ark-executable", readonly = true)
    private String                 arkClassifier;

    /**
     * Attach the module archive to be installed and deployed.
     * @since 0.1.0
     */
    @Parameter(defaultValue = "false")
    private boolean                attach;

    /**
     * The name of the main class. If not specified the first compiled class found that
     * contains a 'main' method will be used.
     * @since 0.1.0
     */
    @Parameter
    private String                 mainClass;

    /**
     * A list of the libraries that must be unpacked from fat jars in order to run.
     * Specify each library as a <code>&lt;dependency&gt;</code> with a
     * <code>&lt;groupId&gt;</code> and a <code>&lt;artifactId&gt;</code> and they will be
     * unpacked at runtime.
     * @since 0.1.0
     */
    @Parameter
    private List<Dependency>       requiresUnpack;

    /**
     * The version of SOFAArk, same with plugin version. when developer
     * want develop a application running on the SOFA-Ark. Just configure
     * sofa-ark-maven-plugin.
     */
    private String                 arkVersion;

    /**
     * mvn command user properties
     */
    private ProjectBuildingRequest projectBuildingRequest;

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match)
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  excludes                   = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  excludeGroupIds            = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  excludeArtifactIds         = new LinkedHashSet<>();

    /**
     * list of packages denied to be imported
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  denyImportPackages;

    /**
     * list of classes denied to be imported
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  denyImportClasses;

    /**
     * list of resources denied to be imported
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  denyImportResources;

    /**
     * list of inject plugin dependencies
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  injectPluginDependencies   = new LinkedHashSet<>();

    /**
     * list of inject plugin export packages
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  injectPluginExportPackages = new LinkedHashSet<>();

    /**
     * whether package provided dependencies into ark
     */
    @Parameter(defaultValue = "false")
    private boolean                packageProvided;

    /**
     * whether to skip package ark-executable jar
     */
    @Parameter(defaultValue = "false")
    private boolean                skipArkExecutable;

    /**
     * whether to keep ark biz jar after package finish, default value is true
     */
    @Parameter(defaultValue = "true")
    private boolean                keepArkBizJar;

    /**
     * web context path when biz is web app. it must start with "/", default value is "/"
     */
    @Parameter(defaultValue = "/", required = true)
    private String                 webContextPath;

    /**
     * the biz jar will record the declared libraries if true,
     * and will filter out only declared libraries when delegate classes and resources to ark-base
     */
    @Parameter(defaultValue = "false")
    private boolean                declaredMode;

    /*----------------Git 相关参数---------------------*/
    /**
     * The root directory of the repository we want to check.
     */
    @Parameter(defaultValue = "")
    private File                  gitDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if ("war".equals(this.project.getPackaging())) {
            getLog().debug("repackage goal could not be applied to war project.");
            return;
        }
        if ("pom".equals(this.project.getPackaging())) {
            getLog().debug("repackage goal could not be applied to pom project.");
            return;
        }
        if (StringUtils.isSameStr(this.arkClassifier, this.bizClassifier)) {
            getLog().debug("Executable fat jar should be different from 'plug-in' module jar.");
            return;
        }
        if (this.skip) {
            getLog().debug("skipping repackaging as configuration.");
            return;
        }

        projectBuildingRequest = this.project.getProjectBuildingRequest();

        /* version of ark container packaged into fat jar follows the plugin version */
        PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get(
            "pluginDescriptor");
        arkVersion = pluginDescriptor.getVersion();

        repackage();
    }

    private void repackage() throws MojoExecutionException {
        File source = this.project.getArtifact().getFile();
        File appTarget = getAppTargetFile();
        File moduleTarget = getModuleTargetFile();

        Repackager repackager = getRepackager(source);
        Libraries libraries = new ArtifactsLibraries(getAdditionalArtifact(), this.requiresUnpack,
            getLog());
        try {
            MavenProject rootProject = MavenUtils.getRootProject(this.project);
            if (repackager.isDeclaredMode()) {
                Set<ArtifactItem> artifactItems = getAllArtifact(rootProject);
                repackager.prepareDeclaredLibraries(artifactItems);
            }
            repackager.setGitDirectory(getGitDirectory(rootProject));
            repackager.repackage(appTarget, moduleTarget, libraries);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
        updateArtifact(appTarget, repackager.getModuleTargetFile());
    }

    private File getGitDirectory(MavenProject rootProject) {
        if (gitDirectory != null && gitDirectory.exists()) {
            return gitDirectory;
        }
        return new File(rootProject.getBasedir().getAbsolutePath() + "/.git");
    }

    private Set<ArtifactItem> getAllArtifact(MavenProject rootProject)
                                                                      throws MojoExecutionException {
        File baseDir = rootProject.getBasedir();
        getLog().info("root project path: " + baseDir.getAbsolutePath());

        // dependency:tree
        String outputPath = baseDir.getAbsolutePath() + "/deps.log." + System.currentTimeMillis();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(baseDir.getAbsolutePath() + "/pom.xml"));

        List<String> goals = Arrays.asList("dependency:tree", "-DappendOutput=true",
            "-DoutputFile=" + outputPath);

        Properties userProperties = projectBuildingRequest.getUserProperties();
        if (userProperties.contains("maven.repo.local")) {
            goals.add("-Dmaven.repo.local=" + userProperties.getProperty("maven.repo.local"));
        }
        request.setGoals(goals);
        Invoker invoker = new DefaultInvoker();
        try {
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("execute dependency:tree failed",
                    result.getExecutionException());
            }

            String depTreeStr = FileUtils.readFileToString(FileUtils.getFile(outputPath),
                Charset.defaultCharset());
            return MavenUtils.convert(depTreeStr);
        } catch (MavenInvocationException | IOException e) {
            throw new MojoExecutionException("execute dependency:tree failed", e);
        } finally {
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> getAdditionalArtifact() throws MojoExecutionException {
        Artifact arkArtifact = repositorySystem.createArtifact(ArkConstants.getGroupId(),
            ArkConstants.getArtifactId(), arkVersion, ArkConstants.getScope(),
            ArkConstants.getType());

        try {
            ArtifactResolutionRequest artifactResolutionRequest = new ArtifactResolutionRequest();
            artifactResolutionRequest.setArtifact(arkArtifact);
            artifactResolutionRequest.setLocalRepository(projectBuildingRequest
                .getLocalRepository());
            artifactResolutionRequest
                .setRemoteRepositories(project.getRemoteArtifactRepositories());
            repositorySystem.resolve(artifactResolutionRequest);
            Set<Artifact> artifacts = new HashSet<>(Collections.singleton(arkArtifact));
            artifacts.addAll(filterExcludeArtifacts(project.getArtifacts()));
            return artifacts;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    private File getAppTargetFile() {
        String classifier = (this.arkClassifier == null ? "" : this.arkClassifier.trim());
        if (classifier.length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        return new File(this.outputDirectory, this.finalName
                                              + classifier
                                              + "."
                                              + this.project.getArtifact().getArtifactHandler()
                                                  .getExtension());
    }

    private File getModuleTargetFile() {
        String classifier = (this.bizClassifier == null ? "" : this.bizClassifier.trim());
        if (classifier.length() > 0 && !classifier.startsWith("-")) {
            classifier = "-" + classifier;
        }
        if (!this.outputDirectory.exists()) {
            this.outputDirectory.mkdirs();
        }
        return new File(this.outputDirectory, this.finalName
                                              + classifier
                                              + "."
                                              + this.project.getArtifact().getArtifactHandler()
                                                  .getExtension());
    }

    private Repackager getRepackager(File source) {
        Repackager repackager = new Repackager(source);
        repackager.addMainClassTimeoutWarningListener(new LoggingMainClassTimeoutWarningListener());
        repackager.setMainClass(this.mainClass);
        repackager.setBizName(bizName);
        if (!StringUtils.isEmpty(System.getProperty(BIZ_NAME))) {
            repackager.setBizName(System.getProperty(BIZ_NAME));
        }
        repackager.setBizVersion(bizVersion);
        repackager.setPriority(String.valueOf(priority));
        repackager.setArkVersion(arkVersion);
        repackager.setDenyImportClasses(denyImportClasses);
        repackager.setDenyImportPackages(denyImportPackages);
        repackager.setDenyImportResources(denyImportResources);
        repackager.setInjectPluginDependencies(injectPluginDependencies);
        repackager.setInjectPluginExportPackages(injectPluginExportPackages);
        repackager.setPackageProvided(packageProvided);
        repackager.setSkipArkExecutable(skipArkExecutable);
        repackager.setKeepArkBizJar(keepArkBizJar);
        repackager.setBaseDir(baseDir);
        repackager.setWebContextPath(webContextPath);
        repackager.setDeclaredMode(declaredMode);
        return repackager;
    }

    private void updateArtifact(File repackaged, File modulePackaged) {
        if (this.attach) {
            attachArtifact(repackaged, arkClassifier);
            attachArtifact(modulePackaged, bizClassifier);
        }
    }

    private void attachArtifact(File jarFile, String classifier) {
        getLog().info("Attaching archive:" + jarFile + ", with classifier: " + classifier);
        this.projectHelper.attachArtifact(this.project, this.project.getPackaging(), classifier,
            jarFile);
    }

    private class LoggingMainClassTimeoutWarningListener implements
                                                        Repackager.MainClassTimeoutWarningListener {

        @Override
        public void handleTimeoutWarning(long duration, String mainMethod) {
            getLog()
                .warn(
                    String
                        .format(
                            "Searching for the main-class is taking some time: %dms, consider using the mainClass configuration parameter",
                            duration));
        }

    }

    /**
     * filter the excluded dependencies
     *
     * @param artifacts all dependencies of project
     * @return dependencies excluded the excludes config
     */
    protected Set<Artifact> filterExcludeArtifacts(Set<Artifact> artifacts) {
        // extension from other resource
        if (!StringUtils.isEmpty(packExcludesConfig)) {
            extensionExcludeArtifacts(baseDir + File.separator + ARK_CONF_BASE_DIR + File.separator
                                      + packExcludesConfig);
        }

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

            if (excludeGroupIds != null) {
                // 支持通配符
                for (String excludeGroupId : excludeGroupIds) {
                    if (excludeGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                        excludeGroupId = ClassUtils.getPackageName(excludeGroupId);
                    }
                    if (e.getGroupId().startsWith(excludeGroupId)) {
                        isExclude = true;
                    }
                }
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

    protected void extensionExcludeArtifacts(String extraResources) {
        try {
            File configFile = new File(extraResources);
            if (configFile.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
                String dataLine;
                while ((dataLine = bufferedReader.readLine()) != null) {
                    if (dataLine.startsWith(EXTENSION_EXCLUDES)) {
                        ParseUtils.parseExcludeConf(excludes, dataLine, EXTENSION_EXCLUDES);
                    } else if (dataLine.startsWith(EXTENSION_EXCLUDES_GROUPIDS)) {
                        ParseUtils.parseExcludeConf(excludeGroupIds, dataLine,
                            EXTENSION_EXCLUDES_GROUPIDS);
                    } else if (dataLine.startsWith(EXTENSION_EXCLUDES_ARTIFACTIDS)) {
                        ParseUtils.parseExcludeConf(excludeArtifactIds, dataLine,
                            EXTENSION_EXCLUDES_ARTIFACTIDS);
                    }
                }
            }
        } catch (IOException ex) {
            getLog().error("failed to extension excludes artifacts.", ex);
        }
    }

    public static class ArkConstants {

        private static String groupId    = "com.alipay.sofa";

        private static String artifactId = "sofa-ark-all";

        private static String classifier = "";

        private static String scope      = "compile";

        private static String type       = "jar";

        public static String getGroupId() {
            return groupId;
        }

        public static String getArtifactId() {
            return artifactId;
        }

        public static String getClassifier() {
            return classifier;
        }

        public static String getScope() {
            return scope;
        }

        public static String getType() {
            return type;
        }

    }

}