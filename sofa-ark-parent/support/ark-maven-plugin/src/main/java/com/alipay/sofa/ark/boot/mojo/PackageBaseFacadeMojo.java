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

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.tools.ArtifactItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugins.dependency.tree.TreeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.alipay.sofa.ark.boot.mojo.MavenUtils.buildPomModel;
import static com.alipay.sofa.ark.boot.mojo.MavenUtils.getRootProject;
import static com.alipay.sofa.ark.boot.mojo.PackageBaseFacadeMojo.JVMFileTypeEnum.JAVA;
import static com.alipay.sofa.ark.boot.mojo.PackageBaseFacadeMojo.JVMFileTypeEnum.KOTLIN;

/**
 * compile and package base to a facade jar, as a dependency for non-master biz
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Mojo(name = "packageBaseFacade", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageBaseFacadeMojo extends TreeMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject                       mavenProject;

    @Component
    private MavenSession                       mavenSession;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                               baseDir;

    /**
     * ark biz version
     *
     * @since 0.4.0
     */
    @Parameter(defaultValue = "${project.version}")
    private String                             version;

    @Parameter(defaultValue = "artifact")
    private String                             artifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String                             groupId;

    /**
     * mvn command user properties
     */
    private ProjectBuildingRequest             projectBuildingRequest;

    @Parameter(defaultValue = "")
    private LinkedHashSet<String>              javaFiles                 = new LinkedHashSet<>();

    @Parameter(defaultValue = "true")
    private String                             cleanAfterPackage;

    /**
     * list of groupId names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>              excludeGroupIds           = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>              excludeArtifactIds        = new LinkedHashSet<>();

    private static final List<JVMFileTypeEnum> SUPPORT_FILE_TYPE_TO_COPY = Stream.of(JAVA, KOTLIN)
                                                                             .collect(
                                                                                 Collectors
                                                                                     .toList());

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        projectBuildingRequest = this.mavenProject.getProjectBuildingRequest();
        File facadeRootDir = null;
        try {
            //0. 创建一个空maven工程（跟当前工程没关系），准备好各种文件、目录。
            String facadeArtifactId = artifactId;
            facadeRootDir = new File(baseDir, facadeArtifactId);
            if (facadeRootDir.exists()) {
                FileUtils.deleteQuietly(facadeRootDir);
            }
            if (!facadeRootDir.exists()) {
                facadeRootDir.mkdirs();
            }

            File facadePom = new File(facadeRootDir, "pom.xml");
            if (!facadePom.exists()) {
                facadePom.createNewFile();
            }
            getLog().info("create base facade directory success." + facadeRootDir.getAbsolutePath());

            //1. 复制指定的jvm文件到该module
            copyMatchedJVMFiles(facadeRootDir);
            getLog().info("copy supported jvm files success.");

            // 2. 解析所有依赖，写入pom
            // 把所有依赖找到，平铺写到pom (同时排掉指定的依赖, 以及基座的子module)
            BufferedWriter pomWriter = new BufferedWriter(new FileWriter(facadePom, true));
            pomWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            pomWriter.write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                    + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
            pomWriter.write("    <modelVersion>4.0.0</modelVersion>\n");
            pomWriter.write("    <groupId>" + groupId + "</groupId>\n");
            pomWriter.write("    <artifactId>" + facadeArtifactId + "</artifactId>\n");
            pomWriter.write("    <version>" + version + "</version>\n");
            pomWriter.flush();
            // 解析基座所有子module，用于exclude
            Set<String> baseModuleArtifactIds = getBaseModuleArtifactIds();
            Set<ArtifactItem> artifactItems = getArtifactList();
            if (artifactItems != null && !artifactItems.isEmpty()) {
                pomWriter.write("<dependencies>");
                for (ArtifactItem i : artifactItems) {
                    if (shouldExclude(i) || baseModuleArtifactIds.contains(i.getArtifactId())) {
                        continue;
                    }
                    pomWriter.write("<dependency>\n");
                    pomWriter.write("<groupId>" + i.getGroupId() + "</groupId>\n");
                    pomWriter.write("<artifactId>" + i.getArtifactId() + "</artifactId>\n");
                    pomWriter.write("<version>" + i.getVersion() + "</version>\n");
                    // 排掉所有间接依赖
                    pomWriter.write("<exclusions>\n");
                    pomWriter.write("<exclusion>\n");
                    pomWriter.write("<groupId>*</groupId>\n");
                    pomWriter.write("<artifactId>*</artifactId>\n");
                    pomWriter.write("</exclusion>\n");
                    pomWriter.write("</exclusions>\n");
                    pomWriter.write("</dependency>\n");
                }
                pomWriter.write("</dependencies>\n");
            }
            // 打source、jar包
            pomWriter.write("<build>\n");
            pomWriter.write("<plugins>\n");
            // maven-source-plugin
            pomWriter.write("<plugin>\n");
            pomWriter.write("<groupId>org.apache.maven.plugins</groupId>\n");
            pomWriter.write("<artifactId>maven-source-plugin</artifactId>\n");
            pomWriter.write("<version>2.0.2</version>\n");
            pomWriter.write("<executions>\n");
            pomWriter.write("<execution>\n");
            pomWriter.write("<id>attach-sources</id>\n");
            pomWriter.write("<goals>\n");
            pomWriter.write("<goal>jar</goal>\n");
            pomWriter.write("</goals>\n");
            pomWriter.write("</execution>\n");
            pomWriter.write("</executions>\n");
            pomWriter.write("</plugin>\n");
            // maven-jar-plugin
            pomWriter.write("<plugin>\n");
            pomWriter.write("<groupId>org.apache.maven.plugins</groupId>\n");
            pomWriter.write("<artifactId>maven-jar-plugin</artifactId>\n");
            pomWriter.write("<version>2.2</version>\n");
            pomWriter.write("</plugin>\n");
            // maven-compiler-plugin
            pomWriter.write("<plugin>\n");
            pomWriter.write("<groupId>org.apache.maven.plugins</groupId>\n");
            pomWriter.write("<artifactId>maven-compiler-plugin</artifactId>\n");
            pomWriter.write("<version>3.8.1</version>\n");
            pomWriter.write("<configuration>\n");
            pomWriter.write("<source>1.8</source>\n");
            pomWriter.write("<target>1.8</target>\n");
            pomWriter.write("</configuration>\n");
            pomWriter.write("</plugin>\n");

            // kotlin-maven-plugin
            pomWriter.write("<plugin>\n");
            pomWriter.write("<groupId>org.jetbrains.kotlin</groupId>\n");
            pomWriter.write("<artifactId>kotlin-maven-plugin</artifactId>\n");
            pomWriter.write("<version>1.8.10</version>\n");
            pomWriter.write("<configuration>\n");
            pomWriter.write("<jvmTarget>1.8</jvmTarget>\n");
            pomWriter.write("</configuration>\n");
            pomWriter.write("<executions>\n");
            pomWriter.write("<execution>\n");
            pomWriter.write("<id>compile</id>\n");
            pomWriter.write("<phase>process-sources</phase>\n");
            pomWriter.write("<goals>\n");
            pomWriter.write("<goal>compile</goal>\n");
            pomWriter.write("</goals>\n");
            pomWriter.write("<configuration>\n");
            pomWriter.write("<sourceDirs>\n");
            pomWriter.write("<sourceDir>src/main/kotlin</sourceDir>\n");
            pomWriter.write("<sourceDir>src/main/java</sourceDir>\n");
            pomWriter.write("</sourceDirs>\n");
            pomWriter.write("</configuration>\n");
            pomWriter.write("</execution>\n");
            pomWriter.write("</executions>\n");
            pomWriter.write("</plugin>\n");

            pomWriter.write("</plugins>\n");
            pomWriter.write("</build>\n");

            pomWriter.write("</project>");
            pomWriter.close();
            getLog().info("analyze all dependencies and write facade pom.xml success.");

            //3. 打包
            InvocationRequest request = new DefaultInvocationRequest();
            request.setPomFile(facadePom);
            List<String> goals = Stream.of("install").collect(Collectors.toList());
            Properties userProperties = projectBuildingRequest.getUserProperties();
            if (userProperties != null) {
                userProperties.forEach((key, value) -> goals.add(String.format("-D%s=%s", key, value)));
            }
            request.setGoals(goals);
            request.setBatchMode(mavenSession.getSettings().getInteractiveMode());
            request.setProfiles(mavenSession.getSettings().getActiveProfiles());
            setSettingsLocation(request);
            Invoker invoker = new DefaultInvoker();
            InvocationResult result = invoker.execute(request);
            if (result.getExitCode() != 0) {
                throw new MojoExecutionException("execute mvn install failed for serverless facade",
                        result.getExecutionException());
            }
            getLog().info("package base facade success.");
            //4.移动构建出来的jar、source jar、pom到outputs目录
            File outputsDir = new File(baseDir, "outputs");
            if (!outputsDir.exists()) {
                outputsDir.mkdirs();
            }
            File facadeTargetDir = new File(facadeRootDir, "target");
            File[] targetFiles = facadeTargetDir.listFiles();
            for (File f : targetFiles) {
                if (f.getName().endsWith(".jar")) {
                    File newFile = new File(outputsDir, f.getName());
                    Files.copy(f.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    getLog().info("copy " + f.getAbsolutePath() + " to " + newFile.getAbsolutePath() + " success.");
                }
            }
            File newPomFile = new File(outputsDir, "pom.xml");
            Files.copy(facadePom.toPath(), newPomFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            getLog().info("copy pom.xml to " + newPomFile.getAbsolutePath() + " success.");
        } catch (Exception e) {
            throw new MojoExecutionException("package serverless facade exception", e);
        } finally {
            // 4. 清理
            if ("true".equals(cleanAfterPackage) && facadeRootDir != null) {
                FileUtils.deleteQuietly(facadeRootDir);
            }
        }
    }

    private Set<String> getBaseModuleArtifactIds() {
        List<String> baseModules = getRootProject(this.mavenProject).getModel().getModules();
        File basedir = getRootProject(this.mavenProject).getBasedir();
        Set<String> baseModuleArtifactIds = new HashSet<>();
        for (String module : baseModules) {
            String modulePath = new File(basedir, module).getAbsolutePath();
            Model modulePom = buildPomModel(modulePath + File.separator + "pom.xml");
            String artifactId = modulePom.getArtifactId();
            getLog().info("find maven module of base: " + artifactId);
            baseModuleArtifactIds.add(artifactId);
        }
        return baseModuleArtifactIds;
    }

    protected static List<File> getSupportedJVMFiles(File baseDir) {
        List<File> supportedJVMFiles = new ArrayList<>();
        getSupportedJVMFiles(baseDir, supportedJVMFiles);
        return supportedJVMFiles;
    }

    private static void getSupportedJVMFiles(File baseDir, List<File> supportedJVMFiles) {
        File[] files = baseDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                getSupportedJVMFiles(file, supportedJVMFiles);
            } else if (null != getMatchedType(file)) {
                supportedJVMFiles.add(file);
            }
        }
    }

    private void copyMatchedJVMFiles(File targetRoot) throws IOException {
        List<File> allSupportedJVMFiles = getSupportedJVMFiles(getRootProject(this.mavenProject)
            .getBasedir());

        for (File file : allSupportedJVMFiles) {
            JVMFileTypeEnum type = getMatchedType(file);
            String fullClassName = type.parseFullClassName(file);
            if (shouldCopy(fullClassName)) {
                File newFile = new File(targetRoot.getAbsolutePath() + File.separator
                                        + type.parseRelativePath(file));
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }

                Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                getLog().info(
                    "copy file from " + file.getAbsolutePath() + " to " + newFile.getAbsolutePath()
                            + " success.");
            }
        }
    }

    private boolean shouldCopy(String fullClassName) {
        for (String classPattern : javaFiles) {
            if (classPattern.equals(fullClassName)) {
                return true;
            }

            if (classPattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                classPattern = StringUtils.removeEnd(classPattern, Constants.PACKAGE_PREFIX_MARK);
                if (fullClassName.startsWith(classPattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static JVMFileTypeEnum getMatchedType(File file) {
        for (JVMFileTypeEnum type : SUPPORT_FILE_TYPE_TO_COPY) {
            if (type.matches(file)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 找到所有依赖，平铺返回
     *
     * @return
     * @throws MojoExecutionException
     */
    private Set<ArtifactItem> getArtifactList() throws MojoExecutionException {
        getLog().info("root project path: " + baseDir.getAbsolutePath());

        // dependency:tree
        String outputPath = baseDir.getAbsolutePath() + "/deps.log." + System.currentTimeMillis();
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(baseDir.getAbsolutePath() + "/pom.xml"));

        List<String> goals = Stream.of("dependency:tree", "-DoutputFile=" + outputPath).collect(Collectors.toList());

        Properties userProperties = projectBuildingRequest.getUserProperties();
        if (userProperties != null) {
            userProperties.forEach((key, value) -> goals.add(String.format("-D%s=%s", key, value)));
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
            File outputFile = new File(outputPath);
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

    private boolean shouldExclude(ArtifactItem artifact) {
        if (excludeGroupIds != null) {
            // 支持通配符
            for (String excludeGroupId : excludeGroupIds) {
                if (excludeGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                    excludeGroupId = StringUtils.removeEnd(excludeGroupId,
                        Constants.PACKAGE_PREFIX_MARK);
                    if (artifact.getGroupId().startsWith(excludeGroupId)) {
                        return true;
                    }
                } else {
                    if (artifact.getGroupId().equals(excludeGroupId)) {
                        return true;
                    }
                }
            }
        }

        if (excludeArtifactIds != null) {
            // 支持通配符
            for (String excludeArtifactId : excludeArtifactIds) {
                if (excludeArtifactId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                    excludeArtifactId = StringUtils.removeEnd(excludeArtifactId,
                        Constants.PACKAGE_PREFIX_MARK);
                    if (artifact.getArtifactId().startsWith(excludeArtifactId)) {
                        return true;
                    }
                } else {
                    if (artifact.getArtifactId().equals(excludeArtifactId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    enum JVMFileTypeEnum {
        JAVA("java", ".java", StringUtils.join(new String[] { "src", "main", "java" },
            File.separator) + File.separator), KOTLIN("kotlin", ".kt", StringUtils.join(
            new String[] { "src", "main", "kotlin" }, File.separator) + File.separator);

        private String name;
        private String suffix;
        private String parentRootDir;

        JVMFileTypeEnum(String name, String suffix, String parentRootDir) {
            this.name = name;
            this.suffix = suffix;
            this.parentRootDir = parentRootDir;
        }

        public boolean matches(File file) {
            String absPath = file.getAbsolutePath();
            boolean inParentRootDir = absPath.contains(parentRootDir);
            boolean onlyOneParentRootDir = absPath.indexOf(parentRootDir) == absPath
                .lastIndexOf(parentRootDir);
            boolean matchedType = absPath.endsWith(suffix);
            return inParentRootDir && onlyOneParentRootDir && matchedType;
        }

        public String parseFullClassName(File file) {
            if (!matches(file)) {
                return null;
            }
            String absPath = file.getAbsolutePath();
            return StringUtils
                .removeEnd(StringUtils.substringAfter(absPath, parentRootDir), suffix).replace(
                    File.separator, ".");
        }

        public String parseRelativePath(File file) {
            if (!matches(file)) {
                return null;
            }
            String absPath = file.getAbsolutePath();
            return parentRootDir + StringUtils.substringAfter(absPath, parentRootDir);
        }
    }
}
