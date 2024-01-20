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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.tools.ArtifactItem;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class MavenUtils {
    public static boolean isRootProject(MavenProject project) {
        if (project == null) {
            return true;
        }

        if (project.hasParent()) {
            // if parent packaging is pom, then this is the root project
            if ("pom".equals(project.getParent().getPackaging())) {
                System.out.println("This is a root project.");
                return true;
            }
            // if parent basedir is null, then this is the root project
            if (project.getParent().getBasedir() != null) {
                System.out.println("This is a not root project.");
                return false;
            }
        }
        System.out.println("This is a root project.");
        return true;
    }

    public static MavenProject getRootProject(MavenProject project) {
        if (project == null) {
            return null;
        }
        MavenProject parent = project;
        while (parent.hasParent() && parent.getParent().getBasedir() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * @param depTreeContent
     * @return
     */
    public static Set<ArtifactItem> convert(String depTreeContent) {
        Set<ArtifactItem> artifactItems = new HashSet<>();
        String[] contents = depTreeContent.split("\n");

        for (String content : contents) {
            ArtifactItem artifactItem = getArtifactItem(content);
            if (artifactItem != null && !"test".equals(artifactItem.getScope())) {
                artifactItems.add(artifactItem);
            }
        }

        return artifactItems;
    }

    private static ArtifactItem getArtifactItem(String lineContent) {
        if (StringUtils.isEmpty(lineContent)) {
            return null;
        }
        lineContent = StringUtils.removeCR(lineContent);
        String[] contentInfos = lineContent.split(" ");
        if (contentInfos.length == 0) {
            return null;
        }
        Optional<String> artifactStrOp = Arrays.stream(contentInfos).filter(c -> c.contains(":")).findFirst();
        if (!artifactStrOp.isPresent()) {
            return null;
        }
        String[] artifactInfos = artifactStrOp.get().split(":");

        ArtifactItem artifactItem = new ArtifactItem();
        if (artifactInfos.length == 5) {
            // like "com.alipay.sofa:healthcheck-sofa-boot-starter:jar:3.11.1:provided"

            artifactItem.setGroupId(artifactInfos[0]);
            artifactItem.setArtifactId(artifactInfos[1]);
            artifactItem.setType(artifactInfos[2]);
            artifactItem.setVersion(artifactInfos[3]);
            artifactItem.setScope(artifactInfos[4]);
        } else if (artifactInfos.length == 6) {
            // like "io.sofastack:dynamic-stock-mng:jar:ark-biz:1.0.0:compile"

            artifactItem.setGroupId(artifactInfos[0]);
            artifactItem.setArtifactId(artifactInfos[1]);
            artifactItem.setType(artifactInfos[2]);
            artifactItem.setClassifier(artifactInfos[3]);
            artifactItem.setVersion(artifactInfos[4]);
            artifactItem.setScope(artifactInfos[5]);
        } else {
            return null;
        }
        return artifactItem;
    }

    private static List<String> UN_LOG_SCOPES = asList("provided", "test", "import", "system");

    public static boolean inUnLogScopes(String scope) {
        return UN_LOG_SCOPES.contains(scope);
    }

    public static void copyProject(MavenProject project, String newParentDirectoryPath)
                                                                                       throws IOException {
        MavenProject rootProject = getRootProject(project);
        File newParentDirectory = new File(newParentDirectoryPath);
        if (!newParentDirectory.exists()) {
            newParentDirectory.mkdirs();
        }
        copyDirectory(rootProject.getBasedir().toPath(), newParentDirectory.toPath());
    }

    private static MavenProject findMavenProjectByName(List<MavenProject> reactorProjects,
                                                       String moduleName) {
        for (MavenProject mavenProject : reactorProjects) {
            if (mavenProject.getArtifactId().equals(moduleName)) {
                return mavenProject;
            }
        }
        return null;
    }

    public static void copyDirectory(Path src, Path dest) throws IOException {
        // 检查源目录是否存在
        if (!Files.isDirectory(src)) {
            throw new IllegalArgumentException("Source Directory not found: " + src);
        }

        // 如果目标目录不存在, 则创建它
        if (!Files.exists(dest)) {
            Files.createDirectories(dest);
        }

        // 遍历源目录及其子目录
        Files.walk(src).filter(sourcePath -> !"target".equals(sourcePath.getFileName().toString())).forEach(sourcePath -> {
            try {
                // 对应的目标文件/目录路径
                Path targetPath = dest.resolve(src.relativize(sourcePath));
                // 判断是文件还是目录来选择复制方式
                if (Files.isDirectory(sourcePath)) {
                    if (!Files.exists(targetPath)) {
                        Files.createDirectory(targetPath);
                    }
                } else {
                    if ("pom.xml".equals(sourcePath.getFileName().toString())) {
                        // 读取Pom文件
                        Model model = readPom(sourcePath.toFile());
                        // 修改model，例如删除scope为provided的依赖
                        removeProvidedScopeDependencies(model);
                        // 将修改后的model写回
                        writePom(new File(targetPath.toFile(), "pom.xml"), model);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                System.err.println("Failed to copy " + sourcePath + " to " + dest);
                e.printStackTrace();
            }
        });
    }

    private static Model readPom(File pomFile) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new FileReader(pomFile));
    }

    private static void removeProvidedScopeDependencies(Model model) {
        if (model.getDependencies() != null) {
            model.getDependencies().stream()
                    .filter(dependency -> "provided".equals(dependency.getScope()))
                    .forEach(dependency -> dependency.setScope("compile"));
        }
    }

    private static void writePom(File destination, Model model) throws IOException {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileWriter(destination), model);
    }
}
