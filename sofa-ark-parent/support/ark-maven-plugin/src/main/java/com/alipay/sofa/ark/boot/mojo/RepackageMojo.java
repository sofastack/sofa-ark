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

import com.alipay.sofa.ark.boot.mojo.model.ArkConfigHolder;
import com.alipay.sofa.ark.tools.ArtifactItem;
import com.alipay.sofa.ark.tools.Libraries;
import com.alipay.sofa.ark.tools.Repackager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.tree.TreeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alipay.sofa.ark.boot.mojo.utils.ParseUtils.getStringSet;
import static com.alipay.sofa.ark.spi.constant.Constants.DECLARED_LIBRARIES_WHITELIST;
import static com.alipay.sofa.ark.tools.ArtifactItem.parseArtifactItem;

/**
 * Repackages existing JAR archives so that they can be executed from the command
 * line using {@literal java -jar}.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Mojo(name = "repackage", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class RepackageMojo extends TreeMojo {

    private static final String    BIZ_NAME                   = "com.alipay.sofa.ark.bizName";

    private static final String    DEFAULT_EXCLUDE_RULES      = "rules.txt";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject           mavenProject;

    @Component
    private MavenProjectHelper     projectHelper;

    @Component
    private MavenSession           mavenSession;

    @Component
    private ArtifactFactory        artifactFactory;

    @Component
    private RepositorySystem       repositorySystem;

    @Component
    private ProjectBuilder         projectBuilder;

    /**
     * Directory containing the generated archive
     * @since 0.1.0
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File                   outputDirectory;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                   baseDir;

    /**
     * Deprecated by the default conf/ark/bootstrap.properties or conf/ark/bootstrap.yml
     */
    @Deprecated
    @Parameter(defaultValue = "", required = false)
    private String                 packExcludesConfig;

    @Parameter(defaultValue = "", required = false)
    private String                 packExcludesUrl;

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
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
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
     * Colon separated groupId, artifactId [and classifier] to include (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includes                   = new LinkedHashSet<>();

    /**
     * list of groupId names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includeGroupIds            = new LinkedHashSet<>();

    /**
     * list of artifact names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includeArtifactIds         = new LinkedHashSet<>();

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

    @Parameter(defaultValue = "false")
    private boolean                disableGitInfo;

    /*----------------Git 相关参数---------------------*/
    /**
     * The root directory of the repository we want to check.
     */
    @Parameter(defaultValue = "")
    private File                   gitDirectory;

    /**
     * 基座依赖标识，以 ${groupId}:${artifactId}:${version} 标识，或以 ${groupId}:${artifactId} 标识
     */
    @Parameter(defaultValue = "")
    private String                 baseDependencyParentIdentity;

    private File                   sofaArkLogDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if ("war".equals(this.mavenProject.getPackaging())) {
            getLog().debug("repackage goal could not be applied to war project.");
            return;
        }
        if ("pom".equals(this.mavenProject.getPackaging())) {
            getLog().debug("repackage goal could not be applied to pom project.");
            return;
        }
        if (StringUtils.equals(this.arkClassifier, this.bizClassifier)) {
            getLog().debug("Executable fat jar should be different from 'plug-in' module jar.");
            return;
        }
        if (this.skip) {
            getLog().debug("skipping repackaging as configuration.");
            return;
        }

        projectBuildingRequest = this.mavenProject.getProjectBuildingRequest();
        sofaArkLogDirectory = new File(this.outputDirectory, "sofa-ark");
        if (!sofaArkLogDirectory.exists()) {
            sofaArkLogDirectory.mkdirs();
        }

        /* version of ark container packaged into fat jar follows the plugin version */
        PluginDescriptor pluginDescriptor = (PluginDescriptor) getPluginContext().get(
            "pluginDescriptor");
        arkVersion = pluginDescriptor.getVersion();

        repackage();
    }

    /**
     *
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    private void repackage() throws MojoExecutionException, MojoFailureException {
        File source = this.mavenProject.getArtifact().getFile();
        File appTarget = getAppTargetFile();
        File moduleTarget = getModuleTargetFile();

        Repackager repackager = getRepackager(source);
        Libraries libraries = new ArtifactsLibraries(getAdditionalArtifact(), this.requiresUnpack,
            getLog());
        try {
            if (repackager.isDeclaredMode()) {
                Set<ArtifactItem> artifactItems;
                if (MavenUtils.isRootProject(this.mavenProject)) {
                    artifactItems = getAllArtifact();
                } else {
                    artifactItems = getAllArtifactByMavenTree();
                }
                artifactItems.addAll(getDeclaredLibrariesWhitelist());
                repackager.prepareDeclaredLibraries(artifactItems);
            }
            MavenProject rootProject = MavenUtils.getRootProject(this.mavenProject);
            repackager.setGitDirectory(getGitDirectory(rootProject));
            repackager.repackage(appTarget, moduleTarget, libraries);
        } catch (IOException ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
        updateArtifact(appTarget, repackager.getModuleTargetFile());
    }

    protected Set<ArtifactItem> getDeclaredLibrariesWhitelist() throws IOException {
        Set<ArtifactItem> res = new HashSet<>();
        Properties prop = ArkConfigHolder.getArkProperties(baseDir.getAbsolutePath());
        Map<String, Object> arkYaml = ArkConfigHolder.getArkYaml(baseDir.getAbsolutePath());

        Set<String> declaredLibrariesWhitelist = new HashSet<>();
        declaredLibrariesWhitelist.addAll(getStringSet(prop, DECLARED_LIBRARIES_WHITELIST));
        declaredLibrariesWhitelist.addAll(getStringSet(arkYaml, DECLARED_LIBRARIES_WHITELIST));

        for (String declaredLibrary : declaredLibrariesWhitelist) {
            res.add(parseArtifactItem(declaredLibrary));
        }
        return res;
    }

    private File getGitDirectory(MavenProject rootProject) {
        if (disableGitInfo) {
            return null;
        }
        if (gitDirectory != null && gitDirectory.exists()) {
            return gitDirectory;
        }
        return com.alipay.sofa.ark.common.util.FileUtils.file(rootProject.getBasedir()
            .getAbsolutePath() + "/.git");
    }

    private void parseArtifactItems(DependencyNode rootNode, Set<ArtifactItem> result) {
        if (rootNode != null) {
            if (!StringUtils.equalsIgnoreCase(rootNode.getArtifact().getScope(), "test")) {
                result.add(parseArtifactItem(rootNode.getArtifact()));
            }

            if (CollectionUtils.isNotEmpty(rootNode.getChildren())) {
                for (DependencyNode node : rootNode.getChildren()) {
                    parseArtifactItems(node, result);
                }
            }
        }
    }

    private DependencyNode parseDependencyGraph() throws MojoExecutionException,
                                                 MojoFailureException {
        if (null == super.getDependencyGraph()) {
            super.execute();
        }
        return super.getDependencyGraph();
    }

    private Set<ArtifactItem> getAllArtifact() throws MojoExecutionException, MojoFailureException {
        DependencyNode dependencyNode = parseDependencyGraph();
        Set<ArtifactItem> results = new HashSet<>();
        parseArtifactItems(dependencyNode, results);
        return results;
    }

    private Set<ArtifactItem> getAllArtifactByMavenTree() throws MojoExecutionException,
                                                         MojoFailureException {
        MavenProject rootProject = MavenUtils.getRootProject(this.mavenProject);
        getLog().info("root project path: " + rootProject.getBasedir().getAbsolutePath());

        //  run  maven dependency:tree
        try {
            if (this.mavenProject.getBasedir() != null) {
                doGetAllArtifactByMavenTree(this.mavenProject);
                return getAllArtifact();
            }
        } catch (MojoExecutionException e) {
            getLog().warn(
                "execute dependency:tree failed, try to execute dependency:tree in root project");
        }
        return doGetAllArtifactByMavenTree(MavenUtils.getRootProject(this.mavenProject));
    }

    private Set<ArtifactItem> doGetAllArtifactByMavenTree(MavenProject project) throws MojoExecutionException {
        File baseDir = project.getBasedir();
        getLog().info("project path: " + baseDir.getAbsolutePath());

        // dependency:tree
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(com.alipay.sofa.ark.common.util.FileUtils.file(baseDir.getAbsolutePath() + "/pom.xml"));

        Properties userProperties = projectBuildingRequest.getUserProperties();
        String outputPath = baseDir.getAbsolutePath() + "/deps.log." + System.currentTimeMillis();
        List<String> goals = Stream.of("dependency:tree", "-DappendOutput=true", "-DoutputFile=\"" + outputPath + "\"").collect(Collectors.toList());
        if (userProperties != null) {
            userProperties.forEach((key, value) -> {
                if (key instanceof String && StringUtils.equals("outputFile", (String) key)) {
                    return;
                }
                goals.add(String.format("-D%s=%s", key, value));
            });
        }

        getLog().info(
                "execute 'mvn dependency:tree' with command 'mvn " + String.join(" ", goals) + "'");
        request.setGoals(goals);
        request.setBatchMode(mavenSession.getSettings().getInteractiveMode());
        request.setProfiles(mavenSession.getSettings().getActiveProfiles());
        setSettingsLocation(request);
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
            File outputFile = com.alipay.sofa.ark.common.util.FileUtils.file(outputPath);
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    private void setSettingsLocation(InvocationRequest request) {
        File userSettingsFile = mavenSession.getRequest().getUserSettingsFile();
        if (userSettingsFile != null && userSettingsFile.exists()) {
            request.setUserSettingsFile(userSettingsFile);
        }
        File globalSettingsFile = mavenSession.getRequest().getGlobalSettingsFile();
        if (globalSettingsFile != null && globalSettingsFile.exists()) {
            request.setGlobalSettingsFile(globalSettingsFile);
        }
    }

    /**
     * @return sofa-ark-all and all maven project's non-excluded artifacts
     * @throws MojoExecutionException
     */
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
            artifactResolutionRequest.setRemoteRepositories(this.mavenProject
                .getRemoteArtifactRepositories());
            repositorySystem.resolve(artifactResolutionRequest);
            Set<Artifact> artifacts = new HashSet<>(Collections.singleton(arkArtifact));

            // 读取需要打包的依赖
            artifacts.addAll(getSlimmedArtifacts());
            return artifacts;
        } catch (Exception ex) {
            throw new MojoExecutionException(ex.getMessage(), ex);
        }
    }

    private Set<Artifact> getSlimmedArtifacts() throws MojoExecutionException, IOException,
                                               MojoFailureException {
        if (StringUtils.endsWithAny(packExcludesConfig, ".yml", ".properties")) {
            throw new MojoExecutionException(
                "not support packExcludesConfig of .yml or .properties in sofa-ark-maven-plugin, please using default conf/ark/bootstrap.yml or conf/ark/bootstrap.properties");
        }
        ModuleSlimConfig moduleSlimConfig = (new ModuleSlimConfig())
            .setPackExcludesConfig(packExcludesConfig).setPackExcludesUrl(packExcludesUrl)
            .setExcludes(excludes).setExcludeGroupIds(excludeGroupIds)
            .setExcludeArtifactIds(excludeArtifactIds)
            .setBaseDependencyParentIdentity(baseDependencyParentIdentity);
        ModuleSlimStrategy slimStrategy = new ModuleSlimStrategy(this.mavenProject,
            parseDependencyGraph(), moduleSlimConfig, this.baseDir, this.getLog());
        return slimStrategy.getSlimmedArtifacts();
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
                                              + this.mavenProject.getArtifact()
                                                  .getArtifactHandler().getExtension());
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
                                              + this.mavenProject.getArtifact()
                                                  .getArtifactHandler().getExtension());
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
            if (!this.skipArkExecutable) {
                attachArtifact(repackaged, arkClassifier);
            }
            if (this.keepArkBizJar) {
                attachArtifact(modulePackaged, bizClassifier);
            }
        }
    }

    private void attachArtifact(File jarFile, String classifier) {
        getLog().info("Attaching archive:" + jarFile + ", with classifier: " + classifier);
        this.projectHelper.attachArtifact(this.mavenProject, this.mavenProject.getPackaging(),
            classifier, jarFile);
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

    public void setExcludes(String str) {
        this.excludes = parseToSet(str);
    }

    public void setExcludeGroupIds(String str) {
        this.excludeGroupIds = parseToSet(str);
    }

    public void setExcludeArtifactIds(String str) {
        this.excludeArtifactIds = parseToSet(str);
    }

    public void setDenyImportPackages(String str) {
        this.denyImportPackages = parseToSet(str);
    }

    public void setDenyImportClasses(String str) {
        this.denyImportClasses = parseToSet(str);
    }

    public void setDenyImportResources(String str) {
        this.denyImportResources = parseToSet(str);
    }

    public void setInjectPluginDependencies(String str) {
        this.injectPluginDependencies = parseToSet(str);
    }

    public void setInjectPluginExportPackages(String str) {
        this.injectPluginExportPackages = parseToSet(str);
    }

    private LinkedHashSet<String> parseToSet(String str) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (StringUtils.isBlank(str)) {
            return set;
        }
        Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .forEach(set::add);
        return set;
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
