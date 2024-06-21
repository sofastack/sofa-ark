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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.AbstractArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArkPluginMojoTest {

    ArkPluginMojo arkPluginMojo = new ArkPluginMojo();

    @Test
    public void testExecute() throws Exception {

        ArchiverManager archiverManager = mock(ArchiverManager.class);
        AtomicInteger finalResourcesCountInJar = new AtomicInteger(0);
        when(archiverManager.getArchiver("zip")).thenReturn(new ZipArchiver() {
            @Override
            public void cleanUp() throws IOException {
                try {
                    Field field = AbstractArchiver.class.getDeclaredField("resources");
                    field.setAccessible(true);
                    finalResourcesCountInJar.set(((List<Object>) field.get(this)).size());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                super.cleanUp();
            }
        });

        Field field = ArkPluginMojo.class.getDeclaredField("archiverManager");
        field.setAccessible(true);
        field.set(arkPluginMojo, archiverManager);

        File outputDirectory = new File("./");
        field = ArkPluginMojo.class.getDeclaredField("outputDirectory");
        field.setAccessible(true);
        field.set(arkPluginMojo, outputDirectory);
        new File(outputDirectory, "xxx.ark.plugin").createNewFile();
        new File(outputDirectory, "xxx.ark.plugin.bak").createNewFile();

        LinkedHashSet<String> excludes = new LinkedHashSet<>();
        excludes.add("group1:artifact3");
        excludes.add("group1:artifact2:17");
        excludes.add("groupx:x");
        arkPluginMojo.excludes = excludes;
        LinkedHashSet<String> excludeGroupIds = new LinkedHashSet<>();
        excludeGroupIds.add("groupy");
        arkPluginMojo.excludeGroupIds = excludeGroupIds;
        LinkedHashSet<String> excludeArtifactIds = new LinkedHashSet<>();
        excludeArtifactIds.add("art1");
        arkPluginMojo.excludeArtifactIds = excludeArtifactIds;

        Set<Artifact> artifacts = new HashSet<>();
        DefaultArtifact defaultArtifact = new DefaultArtifact("groupx", "x", "version", "provided",
            "jar", "17", new DefaultArtifactHandler());
        defaultArtifact.setFile(new File("src/test/resources/test-demo.jar"));
        artifacts.add(defaultArtifact);

        artifacts.add(new DefaultArtifact("group1", "artifact3", "version", "provided", "jar",
            null, new DefaultArtifactHandler()));
        artifacts.add(new DefaultArtifact("group1", "artifact2", "version", "provided", "jar",
            "17", new DefaultArtifactHandler()));

        defaultArtifact = new DefaultArtifact("groupyxx", "x", "version", "provided", "jar", "17",
            new DefaultArtifactHandler());
        defaultArtifact.setFile(new File("src/test/resources/test-demo.jar"));
        artifacts.add(defaultArtifact);

        artifacts.add(new DefaultArtifact("groupy", "x", "version", "provided", "jar", "17",
            new DefaultArtifactHandler()));
        artifacts.add(new DefaultArtifact("groupyxx", "art1", "version", "provided", "jar", "17",
            new DefaultArtifactHandler()));

        DefaultArtifact shadeArtifact = new DefaultArtifact("shadeGroup", "shadeArtifact",
            "shadeVersion", "provided", "jar", "shadeClassifier", new DefaultArtifactHandler());
        shadeArtifact.setFile(new File("src/test/resources/test-demo.jar"));
        artifacts.add(shadeArtifact);

        DefaultArtifact projectArtifact = new DefaultArtifact("a", "b", "c", "compile", "jar",
            null, new DefaultArtifactHandler());
        projectArtifact.setFile(new File("src/test/resources/test-demo.jar"));

        Build build = new Build();
        build.setOutputDirectory("./notexist");
        MavenProject mavenProject = mock(MavenProject.class);
        when(mavenProject.getArtifacts()).thenReturn(artifacts);
        when(mavenProject.getArtifact()).thenReturn(projectArtifact);
        when(mavenProject.getBuild()).thenReturn(build);
        arkPluginMojo.setProject(mavenProject);
        arkPluginMojo.setShades(new LinkedHashSet<>(Collections
            .singleton("shadeGroup:shadeArtifact:shadeVersion:shadeClassifier")));

        field = ArkPluginMojo.class.getDeclaredField("attach");
        field.setAccessible(true);
        field.set(arkPluginMojo, true);

        arkPluginMojo.groupId = "a";
        arkPluginMojo.artifactId = "b";
        arkPluginMojo.version = "c";
        arkPluginMojo.priority = 10;
        arkPluginMojo.pluginName = "xxx";
        arkPluginMojo.description = "yyy";
        arkPluginMojo.workDirectory = new File("./");
        arkPluginMojo.exportPluginClass = true;
        arkPluginMojo.execute();
        assertEquals(4, finalResourcesCountInJar.get());
    }

    @Test
    public void testExportConfig() {
        Properties properties = new Properties();
        ExportConfig exportConfig = new ExportConfig();
        LinkedHashSet<String> classes = new LinkedHashSet();
        classes.add("a");
        classes.add("b");
        exportConfig.setClasses(classes);
        exportConfig.store(properties);
        assertEquals("a,b", properties.getProperty("export-classes"));
        assertEquals("", properties.getProperty("export-mode"));
        assertEquals("", properties.getProperty("export-resources"));
        assertEquals("", properties.getProperty("export-packages"));
    }

    @Test
    public void testImportConfig() {
        Properties properties = new Properties();
        ImportConfig importConfig = new ImportConfig();
        LinkedHashSet<String> resources = new LinkedHashSet();
        resources.add("c");
        resources.add("d");
        importConfig.setResources(resources);
        importConfig.store(properties);
        assertEquals("", properties.getProperty("import-classes"));
        assertEquals("c,d", properties.getProperty("import-resources"));
        assertEquals("", properties.getProperty("import-packages"));
    }

}
