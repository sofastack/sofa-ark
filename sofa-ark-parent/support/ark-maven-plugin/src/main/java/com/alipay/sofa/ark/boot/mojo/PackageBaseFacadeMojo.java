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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.dependency.tree.TreeMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * compile and package base to a facade jar, as a dependency for non-master biz
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Mojo(name = "packageBaseFacade", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class PackageBaseFacadeMojo extends TreeMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject           mavenProject;

    @Component
    private MavenSession           mavenSession;

    @Parameter(defaultValue = "${project.basedir}", required = true)
    private File                   baseDir;

    /**
     * ark biz version
     *
     * @since 0.4.0
     */
    @Parameter(defaultValue = "${project.version}")
    private String                 version;

    @Parameter(defaultValue = "artifact")
    private String                 artifactId;

    @Parameter(defaultValue = "${project.groupId}", required = true)
    private String                 groupId;

    /**
     * mvn command user properties
     */
    private ProjectBuildingRequest projectBuildingRequest;

    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  javaFiles          = new LinkedHashSet<>();

    @Parameter(defaultValue = "true")
    private String                 cleanAfterPackage;

    /**
     * list of groupId names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  excludeGroupIds    = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  excludeArtifactIds = new LinkedHashSet<>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        projectBuildingRequest = this.mavenProject.getProjectBuildingRequest();
        File facadeRootDir = null;
        try {
            //0. 创建一个空maven工程（跟当前工程没关系），准备好各种文件、目录。
            String facadeArtifactId = artifactId;
            facadeRootDir = new File(baseDir, facadeArtifactId);
            if (facadeRootDir.exists()) {
                deleteDirectory(facadeRootDir);
            }
            if (!facadeRootDir.exists()) {
                facadeRootDir.mkdirs();
            }
            File facadeJavaDir = new File(facadeRootDir, "src/main/java");
            if (!facadeJavaDir.exists()) {
                facadeJavaDir.mkdirs();
            }
            File facadePom = new File(facadeRootDir, "pom.xml");
            if (!facadePom.exists()) {
                facadePom.createNewFile();
            }
            getLog().info("create base facade directory success." + facadeRootDir.getAbsolutePath());

            //2. 解析所有依赖，写入pom
            // 第一次，把所有依赖找到，平铺写到pom (同时排掉指定的依赖, 以及基座的子module)
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
                    pomWriter.write("</dependency>\n");
                }
                pomWriter.write("</dependencies>\n");
            }
            pomWriter.write("</project>");
            pomWriter.close();
            getLog().info("analyze all dependencies and write facade pom.xml success.");

            // 反复执行dependency tree，并排除指定的依赖（删掉根 + 排除间接依赖），直到排干净
            if (CollectionUtils.isNotEmpty(excludeGroupIds) || CollectionUtils.isNotEmpty(excludeArtifactIds)) {
                clearExclusion(facadeRootDir, facadePom, facadeArtifactId);
                getLog().info("exclusion cleared success.");
            }

            //1. 复制指定的java文件到该module
            List<File> allJavaFiles = new LinkedList<>();
            getJavaFiles(baseDir, allJavaFiles);
            copyFiles(allJavaFiles, facadeJavaDir, javaFiles);
            getLog().info("copy java files success.");

            // 3. 打包
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
            // 移动构建出来的jar到baseDir
            File facadeTargetDir = new File(facadeRootDir, "target");
            File[] targetFiles = facadeTargetDir.listFiles();
            for (File f : targetFiles) {
                if (f.getName().endsWith(".jar")) {
                    File newFile = new File(baseDir, f.getName());
                    f.renameTo(newFile);
                    getLog().info("move " + f.getAbsolutePath() + " to " + newFile.getAbsolutePath() + " success.");
                    break;
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException("package serverless facade exception", e);
        } finally {
            // 4. 清理
            if ("true".equals(cleanAfterPackage) && facadeRootDir != null) {
                deleteDirectory(facadeRootDir);
            }
        }
    }

    private Set<String> getBaseModuleArtifactIds() throws IOException, InterruptedException {
        List<String> baseModules = this.mavenProject.getParent().getModel().getModules();
        File basedir = mavenProject.getParent().getBasedir();
        Set<String> baseModuleArtifactIds = new HashSet<>();
        for (String module : baseModules) {
            String pomPath = new File(basedir, module).getAbsolutePath();
            String artifactId = getArtifactIdFromPom(pomPath);
            getLog().info("find maven module of base: " + artifactId);
            baseModuleArtifactIds.add(artifactId);
        }
        return baseModuleArtifactIds;
    }

    private static String getArtifactIdFromPom(String pomFilePath) throws IOException,
                                                                  InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("mvn", "help:evaluate", "-Dexpression=project.artifactId", "-q",
            "-DforceStdout", "-f", pomFilePath);

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            return output.toString().trim();
        } else {
            throw new IOException("Failed to get artifactId from pom.xml");
        }
    }

    private void clearExclusion(File facadeRootDir, File facadePom, String facadeArtifactId)
                                                                                            throws MojoExecutionException,
                                                                                            IOException {
        int i = 1;
        Map<ArtifactItem, ArtifactWithExclusion> artifactWithExclusions = new HashMap<>();
        while (true) {
            int count = clearExclusionOnePass(facadeRootDir, facadePom.getAbsolutePath(),
                facadeArtifactId, artifactWithExclusions);
            getLog().info("clear exclusion success. " + i++ + " times. exclusion:" + count);
            if (count == 0) {
                break;
            }
        }
    }

    private int clearExclusionOnePass(File facadeRootDir,
                                      String facadePomPath,
                                      String facadeArtifactId,
                                      Map<ArtifactItem, ArtifactWithExclusion> artifactWithExclusions)
                                                                                                      throws MojoExecutionException,
                                                                                                      IOException {
        Set<ArtifactItem> artifactItems = getArtifactsTree(facadeRootDir);
        int exclusionCount = 0;
        // 清空
        File pomFile = new File(facadePomPath);
        pomFile.delete();
        pomFile = new File(facadePomPath);
        pomFile.createNewFile();

        BufferedWriter pomWriter = new BufferedWriter(new FileWriter(pomFile, true));
        pomWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        pomWriter
            .write("<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                   + "xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n");
        pomWriter.write("    <modelVersion>4.0.0</modelVersion>\n");
        pomWriter.write("    <groupId>" + groupId + "</groupId>\n");
        pomWriter.write("    <artifactId>" + facadeArtifactId + "</artifactId>\n");
        pomWriter.write("    <version>" + version + "</version>\n");

        for (ArtifactItem artifact : artifactItems) {
            ArtifactWithExclusion withExclusion = new ArtifactWithExclusion(artifact);
            if (shouldExclude(artifact)) {
                exclusionCount++;
                artifactWithExclusions.remove(artifact);
                continue;
            } else {
                artifactWithExclusions.putIfAbsent(artifact, withExclusion);
                withExclusion = artifactWithExclusions.get(artifact);
            }

            for (ArtifactItem child : artifact.getDependencies()) {
                if (shouldExclude(child)) {
                    exclusionCount++;
                    withExclusion.addExclusion(child);
                }
            }
        }

        if (artifactWithExclusions != null && !artifactWithExclusions.isEmpty()) {
            pomWriter.write("<dependencies>");
            for (Map.Entry<ArtifactItem, ArtifactWithExclusion> e : artifactWithExclusions
                .entrySet()) {
                ArtifactWithExclusion i = e.getValue();
                pomWriter.write("<dependency>\n");
                pomWriter.write("<groupId>" + i.artifactItem.getGroupId() + "</groupId>\n");
                pomWriter
                    .write("<artifactId>" + i.artifactItem.getArtifactId() + "</artifactId>\n");
                pomWriter.write("<version>" + i.artifactItem.getVersion() + "</version>\n");
                if (!i.exclusions.isEmpty()) {
                    pomWriter.write("<exclusions>\n");
                    for (ArtifactItem exclusion : i.exclusions) {
                        pomWriter.write("<exclusion>\n");
                        pomWriter.write("<groupId>" + exclusion.getGroupId() + "</groupId>\n");
                        pomWriter.write("<artifactId>" + exclusion.getArtifactId()
                                        + "</artifactId>\n");
                        pomWriter.write("</exclusion>\n");
                    }
                    pomWriter.write("</exclusions>\n");
                }
                pomWriter.write("</dependency>\n");
            }
            pomWriter.write("</dependencies>\n");
        }
        pomWriter.write("</project>");
        pomWriter.close();

        return exclusionCount;
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    private static void getJavaFiles(File baseDir, List<File> allJavaFiles) {
        File[] files = baseDir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                getJavaFiles(file, allJavaFiles);
            } else if (file.getName().endsWith(".java")) {
                allJavaFiles.add(file);
            }
        }
    }

    public static void copyFiles(List<File> allJavaFiles, File targetDir, Set<String> patterns)
                                                                                               throws IOException {
        for (File file : allJavaFiles) {
            File newFile;
            if ((newFile = shouldCopy(targetDir, file, patterns)) != null) {
                if (!newFile.getParentFile().exists()) {
                    newFile.getParentFile().mkdirs();
                }
                Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static File shouldCopy(File newDir, File file, Set<String> javaFiles) {
        String fileAbsPath = file.getAbsolutePath();
        if (!fileAbsPath.endsWith(".java")) {
            return null;
        }
        String s = "src/main/java/";
        if (!fileAbsPath.contains(s)) {
            return null;
        }
        if (fileAbsPath.indexOf(s) != fileAbsPath.lastIndexOf(s)) {
            return null;
        }
        fileAbsPath = fileAbsPath.substring(0, fileAbsPath.indexOf(".java"));
        fileAbsPath = fileAbsPath.substring(fileAbsPath.lastIndexOf(s) + s.length());
        String javaName = fileAbsPath.replace('/', '.');
        for (String pattern : javaFiles) {
            if (pattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                pattern = StringUtils.removeEnd(pattern, Constants.PACKAGE_PREFIX_MARK);
                if (javaName.startsWith(pattern)) {
                    System.out.println(javaName);
                    return new File(newDir, file.getAbsolutePath().substring(
                        file.getAbsolutePath().indexOf(s)));
                }
            } else {
                if (pattern.equals(javaName)) {
                    System.out.println(javaName);
                    return new File(newDir, file.getAbsolutePath().substring(
                        file.getAbsolutePath().indexOf(s)));
                }
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

    /**
     * 解析依赖，按树状返回
     *
     * @param baseDir
     * @return
     * @throws MojoExecutionException
     */
    private Set<ArtifactItem> getArtifactsTree(File baseDir) throws MojoExecutionException {
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
            return MavenUtils.convertToTree(depTreeStr);
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

    static class ArtifactWithExclusion {
        ArtifactItem      artifactItem;
        Set<ArtifactItem> exclusions = new HashSet<>();

        public ArtifactWithExclusion(ArtifactItem artifactItem) {
            this.artifactItem = artifactItem;
        }

        public void addExclusion(ArtifactItem a) {
            this.exclusions.add(a);
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
}
