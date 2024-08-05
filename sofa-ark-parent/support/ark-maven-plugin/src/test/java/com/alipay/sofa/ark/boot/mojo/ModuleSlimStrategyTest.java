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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.alipay.sofa.ark.boot.mojo.ReflectionUtils.setField;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ModuleSlimStrategyTest.java, v 0.1 2024年07月24日 16:33 立蓬 Exp $
 */
public class ModuleSlimStrategyTest {

    @Test
    public void testGetSlimmedArtifacts() throws MojoExecutionException, IOException,
                                         URISyntaxException {
        MavenProject proj = mock(MavenProject.class);
        Artifact a1 = mock(Artifact.class);
        Artifact a2 = mock(Artifact.class);
        Artifact a3 = mock(Artifact.class);

        Set<Artifact> artifacts = Sets.newHashSet(a1, a2, a3);
        when(proj.getArtifacts()).thenReturn(artifacts);

        ModuleSlimStrategy strategy = spy(new ModuleSlimStrategy(proj, new ModuleSlimConfig(),
            mockBaseDir(), null));
        doReturn(Sets.newHashSet(a1)).when(strategy).getArtifactsToFilterByParentIdentity(anySet());
        doReturn(Sets.newHashSet(a2)).when(strategy).getArtifactsToFilterByExcludeConfig(anySet());

        assertEquals(1, strategy.getSlimmedArtifacts().size());
    }

    @Test
    public void testGetArtifactsToFilterByParentIdentity() throws URISyntaxException,
                                                          MojoExecutionException {
        ModuleSlimConfig config = (new ModuleSlimConfig())
            .setBaseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0");
        ModuleSlimStrategy strategy = new ModuleSlimStrategy(getMockBootstrapProject(), config,
            mockBaseDir(), null);

        Artifact sameArtifact = mock(Artifact.class);
        when(sameArtifact.getGroupId()).thenReturn("com.mock");
        when(sameArtifact.getArtifactId()).thenReturn("same-dependency-artifact");
        when(sameArtifact.getVersion()).thenReturn("1.0");

        Artifact differenceArtifact = mock(Artifact.class);
        when(differenceArtifact.getGroupId()).thenReturn("com.mock");
        when(differenceArtifact.getArtifactId()).thenReturn("difference-dependency-artifact");
        when(differenceArtifact.getVersion()).thenReturn("2.0");

        Set<Artifact> res = strategy.getArtifactsToFilterByParentIdentity(Sets.newHashSet(
            sameArtifact, differenceArtifact));
        assertTrue(res.contains(sameArtifact));
        assertFalse(res.contains(differenceArtifact));
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifactsByDefault() throws URISyntaxException,
                                                                  IOException {
        ModuleSlimConfig config = new ModuleSlimConfig();
        ModuleSlimStrategy strategy = new ModuleSlimStrategy(getMockBootstrapProject(), config,
            mockBaseDir(), mockLog());

        strategy.configExcludeArtifactsByDefault();

        // 验证 ark.properties
        assertTrue(config.getExcludes().contains("commons-beanutils:commons-beanutils"));
        assertTrue(config.getExcludeGroupIds().contains("org.springframework"));
        assertTrue(config.getExcludeArtifactIds().contains("sofa-ark-spi"));

        // 验证 ark.yml
        assertTrue(config.getExcludes().contains("commons-beanutils:commons-beanutils-yml"));
        assertTrue(config.getExcludeGroupIds().contains("org.springframework-yml"));
        assertTrue(config.getExcludeArtifactIds().contains("sofa-ark-spi-yml"));
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifacts() throws URISyntaxException {
        ModuleSlimConfig config = new ModuleSlimConfig();
        ModuleSlimStrategy strategy = new ModuleSlimStrategy(null, config, mockBaseDir(), mockLog());
        URL resource = this.getClass().getClassLoader().getResource("excludes.txt");
        strategy.extensionExcludeAndIncludeArtifacts(resource.getPath());

        assertTrue(config.getExcludes().contains("tracer-core:3.0.10")
                   && config.getExcludes().contains("tracer-core:3.0.11"));
    }

    @Test
    public void testLogExcludeMessage() throws URISyntaxException {
        List<String> jarGroupIds = asList("com.alipay.sofa", "org.springframework");
        List<String> jarArtifactIds = asList("netty");
        List<String> jarList = asList("commons-io:commons-io:2.7");

        DefaultArtifact defaultArtifact = new DefaultArtifact("com.alipay.sofa", "artifactId",
            "version", "compile", "jar", null, new DefaultArtifactHandler());
        DefaultArtifact defaultArtifact1 = new DefaultArtifact("io.netty", "netty", "version",
            "compile", "jar", null, new DefaultArtifactHandler());
        DefaultArtifact defaultArtifact2 = new DefaultArtifact("commons-io", "commons-io", "2.7",
            "compile", "jar", null, new DefaultArtifactHandler());
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(defaultArtifact);
        artifacts.add(defaultArtifact1);
        artifacts.add(defaultArtifact2);

        ModuleSlimStrategy strategy = new ModuleSlimStrategy(null, null, mockBaseDir(), mockLog());
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, true);
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, false);
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifactsFromUrl() throws URISyntaxException {

        DefaultArtifact defaultArtifact = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        DefaultArtifact defaultArtifact1 = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(defaultArtifact);
        artifacts.add(defaultArtifact1);

        // NOTE: Access httpbin to run unit test, need vpn maybe.
        String packExcludesUrl = "http://httpbin.org/get";

        ModuleSlimStrategy strategy = new ModuleSlimStrategy(null, new ModuleSlimConfig(),
            mockBaseDir(), mockLog());
        strategy.extensionExcludeArtifactsFromUrl(packExcludesUrl, artifacts);
    }

    @Test
    public void testLogExcludeMessageWithMoreCases() throws URISyntaxException {
        List<String> jarGroupIds = new ArrayList<>();
        jarGroupIds.add("group1*");
        jarGroupIds.add("group2.*");

        List<String> jarArtifactIds = new ArrayList<>();
        jarArtifactIds.add("artifact1*");
        jarArtifactIds.add("artifact2.g.*");

        List<String> jarList = new ArrayList<>();
        Set<Artifact> artifacts = new HashSet<>();
        Artifact artifact = new DefaultArtifact("group1.a.b", "artifact1gkl", "1.0", "test", "",
            null, new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);
        artifact = new DefaultArtifact("group2.c", "artifact1gkl", "1.0", "", "", null,
            new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);
        artifact = new DefaultArtifact("group3", "artifact1.e", "1.0", "", "", null,
            new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);
        artifact = new DefaultArtifact("group3", "artifact2.g.h", "1.0", "", "", null,
            new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        ModuleSlimStrategy strategy = new ModuleSlimStrategy(null, null, mockBaseDir(), mockLog());
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, true);
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, false);
    }

    private File getResourceFile(String resourceName) throws URISyntaxException {
        URL url = this.getClass().getClassLoader().getResource(resourceName);
        return new File(url.toURI());
    }

    private MavenProject getMockBootstrapProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-bootstrap");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("jar");

        Artifact artifact = mock(Artifact.class);
        when(artifact.getArtifactId()).thenReturn("base-bootstrap");
        when(artifact.getGroupId()).thenReturn("com.mock");
        when(artifact.getVersion()).thenReturn("0.0.1-SNAPSHOT");

        project.setArtifact(artifact);

        project.setParent(getRootProject());

        setField("basedir", project, getResourceFile("baseDir"));
        return project;
    }

    private MavenProject getRootProject() {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-dependencies-starter");
        project.setGroupId("com.mock");
        project.setVersion("1.0");
        project.setPackaging("pom");

        Artifact artifact = mock(Artifact.class);
        when(artifact.getArtifactId()).thenReturn("base-dependencies-starter");
        when(artifact.getGroupId()).thenReturn("com.mock");
        when(artifact.getVersion()).thenReturn("1.0");

        project.setArtifact(artifact);
        project.setParent(null);

        Dependency sameDependency = new Dependency();
        sameDependency.setArtifactId("same-dependency-artifact");
        sameDependency.setGroupId("com.mock");
        sameDependency.setVersion("1.0");

        Dependency differenceDependency = new Dependency();
        differenceDependency.setArtifactId("difference-dependency-artifact");
        differenceDependency.setGroupId("com.mock");
        differenceDependency.setVersion("1.0");

        DependencyManagement dm = new DependencyManagement();
        dm.setDependencies(Lists.newArrayList(sameDependency, differenceDependency));

        Model pom = new Model();
        pom.setDependencyManagement(dm);

        project.setOriginalModel(pom);
        return project;
    }

    private Log mockLog() {
        Log log = mock(Log.class);
        doNothing().when(log).info(anyString());
        return log;
    }

    private File mockBaseDir() throws URISyntaxException {
        return getResourceFile("baseDir");
    }
}