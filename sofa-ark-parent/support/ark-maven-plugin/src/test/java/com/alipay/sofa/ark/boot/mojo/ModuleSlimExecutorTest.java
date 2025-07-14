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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alipay.sofa.ark.boot.mojo.ReflectionUtils.setField;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ModuleSlimStrategyTest.java, v 0.1 2024年07月24日 16:33 立蓬 Exp $
 */
public class ModuleSlimExecutorTest {

    @Test
    public void testGetSlimmedArtifacts() throws MojoExecutionException, IOException,
                                         URISyntaxException {
        MavenProject proj = mock(MavenProject.class);
        Artifact a1 = new DefaultArtifact("com.alipay.sofa", "a1", "version", "compile", "jar",
            null, new DefaultArtifactHandler());
        Artifact a2 = new DefaultArtifact("com.alipay.sofa", "a2", "version", "compile", "jar",
            null, new DefaultArtifactHandler());
        Artifact a3 = new DefaultArtifact("com.alipay.sofa", "a3", "version", "compile", "jar",
            null, new DefaultArtifactHandler());
        Artifact a4 = new DefaultArtifact("com.alipay.sofa", "a4", "version", "provided", "jar",
            null, new DefaultArtifactHandler());
        Artifact a5 = new DefaultArtifact("com.alipay.sofa", "a5", "version", "compile", "jar",
            null, new DefaultArtifactHandler());

        Set<Artifact> artifacts = Sets.newHashSet(a1, a2, a3, a4, a5);
        when(proj.getArtifacts()).thenReturn(artifacts);

        ModuleSlimConfig config = new ModuleSlimConfig();

        ModuleSlimExecutor strategy = spy(new ModuleSlimExecutor(proj, null, null, null, config,
            mockBaseDir(), mockLog()));

        doNothing().when(strategy).checkExcludeByParentIdentity(anySet());
        doReturn(Sets.newHashSet(a1)).when(strategy).getArtifactsToFilterByParentIdentity(anySet());
        doReturn(Sets.newHashSet(a2)).when(strategy).getArtifactsToFilterByExcludeConfig(anySet());
        doReturn(Sets.newHashSet(a3, a4)).when(strategy).getArtifactsToFilterByBasePlugin(anySet());
        doReturn(Sets.newHashSet(a4)).when(strategy).getArtifactsToAddByIncludeConfig(anySet());

        assertEquals(2, strategy.getSlimmedArtifacts().size());
        assertEquals(Artifact.SCOPE_COMPILE, a4.getScope());
    }

    @Test
    public void testGetArtifactsToFilterByParentIdentity() throws URISyntaxException,
                                                          MojoExecutionException {
        ModuleSlimConfig config = (new ModuleSlimConfig())
            .setBaseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0");
        ModuleSlimExecutor strategy = new ModuleSlimExecutor(getMockBootstrapProject(), null, null,
            null, config, mockBaseDir(), null);

        Artifact sameArtifact = mock(Artifact.class);
        when(sameArtifact.getGroupId()).thenReturn("com.mock");
        when(sameArtifact.getArtifactId()).thenReturn("same-dependency-artifact");
        when(sameArtifact.getVersion()).thenReturn("1.0");
        when(sameArtifact.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        when(sameArtifact.getType()).thenReturn("jar");

        Artifact differenceArtifact = mock(Artifact.class);
        when(differenceArtifact.getGroupId()).thenReturn("com.mock");
        when(differenceArtifact.getArtifactId()).thenReturn("difference-dependency-artifact");
        when(differenceArtifact.getVersion()).thenReturn("2.0");
        when(differenceArtifact.getBaseVersion()).thenReturn("2.0-SNAPSHOT");
        when(sameArtifact.getType()).thenReturn("jar");

        // case1: with BaseDependencyParentIdentity
        Set<Artifact> res = strategy.getArtifactsToFilterByParentIdentity(Sets.newHashSet(
            sameArtifact, differenceArtifact));
        assertTrue(res.contains(sameArtifact));
        assertFalse(res.contains(differenceArtifact));

        // case2: without BaseDependencyParentIdentity
        config.setBaseDependencyParentIdentity("");
        res = strategy.getArtifactsToFilterByParentIdentity(Sets.newHashSet(sameArtifact,
            differenceArtifact));
        assertTrue(res.isEmpty());
    }

    @Test
    public void testGetArtifactsToFilterByBasePlugin() throws URISyntaxException {
        ModuleSlimConfig config = new ModuleSlimConfig();
        ModuleSlimExecutor strategy = spy(new ModuleSlimExecutor(getMockBootstrapProject(), null,
            null, null, config, mockBaseDir(), null));

        Artifact sameArtifact = mock(Artifact.class);
        when(sameArtifact.getGroupId()).thenReturn("com.mock");
        when(sameArtifact.getArtifactId()).thenReturn("same-dependency-artifact");
        when(sameArtifact.getVersion()).thenReturn("1.0");
        when(sameArtifact.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        when(sameArtifact.getType()).thenReturn("jar");

        Artifact differenceArtifact = mock(Artifact.class);
        when(differenceArtifact.getGroupId()).thenReturn("com.mock");
        when(differenceArtifact.getArtifactId()).thenReturn("difference-dependency-artifact");
        when(differenceArtifact.getVersion()).thenReturn("2.0");
        when(differenceArtifact.getBaseVersion()).thenReturn("2.0-SNAPSHOT");
        when(sameArtifact.getType()).thenReturn("jar");

        doReturn(mockBasePluginBomModel(Lists.newArrayList(sameArtifact))).when(strategy)
            .resolvePomAsOriginalModel(anyString(), anyString(), anyString());

        // case1: without BaseDependencyParentIdentity
        config.setBaseDependencyParentIdentity("");
        Set<Artifact> res = strategy.getArtifactsToFilterByBasePlugin(Sets.newHashSet(sameArtifact,
            differenceArtifact));
        assertTrue(res.isEmpty());

        // case2: with BaseDependencyParentIdentity
        config.setBaseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0");
        res = strategy.getArtifactsToFilterByBasePlugin(Sets.newHashSet(sameArtifact,
            differenceArtifact));
        assertTrue(res.contains(sameArtifact));
        assertFalse(res.contains(differenceArtifact));
    }

    private Model mockBasePluginBomModel(List<Artifact> artifacts){
        Model model = new Model();
        model.setGroupId("com.mock");
        model.setArtifactId("base-plugin-bom");
        model.setVersion("1.0");
        model.setPackaging("pom");

        Parent parent = new Parent();
        parent.setGroupId("com.mock");
        parent.setArtifactId("base-dependencies-starter");
        parent.setVersion("1.0");
        model.setParent(parent);

        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.setDependencies(artifacts.stream().map(artifact -> {
            Dependency d = new Dependency();
            d.setGroupId(artifact.getGroupId());
            d.setArtifactId(artifact.getArtifactId());
            d.setVersion(artifact.getBaseVersion());
            return d;
        }).collect(Collectors.toList()));
        model.setDependencyManagement(dependencyManagement);
        return model;
    }

    @Test
    public void testCheckExcludeByParentIdentity() throws URISyntaxException,
                                                  MojoExecutionException {
        ModuleSlimConfig config = (new ModuleSlimConfig())
            .setBaseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0");

        Log log = Mockito.mock(Log.class);
        doNothing().when(log).info(anyString());
        doNothing().when(log).error(anyString());

        ModuleSlimExecutor strategy = new ModuleSlimExecutor(getMockBootstrapProject(), null, null,
            null, config, mockBaseDir(), log);

        // 基座和模块都有该依赖，且版本一致
        Artifact sameArtifact = mock(Artifact.class);
        when(sameArtifact.getGroupId()).thenReturn("com.mock");
        when(sameArtifact.getArtifactId()).thenReturn("same-dependency-artifact");
        when(sameArtifact.getVersion()).thenReturn("1.0");
        when(sameArtifact.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        when(sameArtifact.getType()).thenReturn("jar");

        // 模块有，但基座没有的依赖
        Artifact differentArtifact = mock(Artifact.class);
        when(differentArtifact.getGroupId()).thenReturn("com.mock");
        when(differentArtifact.getArtifactId()).thenReturn("different-artifact");
        when(differentArtifact.getVersion()).thenReturn("1.0");
        when(differentArtifact.getBaseVersion()).thenReturn("1.0-SNAPSHOT");
        when(differentArtifact.getType()).thenReturn("jar");

        // 模块和基座都有该依赖，但版本不一致
        Artifact differentVersionArtifact = mock(Artifact.class);
        when(differentVersionArtifact.getGroupId()).thenReturn("com.mock");
        when(differentVersionArtifact.getArtifactId()).thenReturn("difference-dependency-artifact");
        when(differentVersionArtifact.getVersion()).thenReturn("2.0");
        when(differentVersionArtifact.getBaseVersion()).thenReturn("2.0-SNAPSHOT");
        when(differentVersionArtifact.getType()).thenReturn("jar");

        Dependency differenceVersionDependency = new Dependency();
        differenceVersionDependency.setArtifactId("difference-dependency-artifact");
        differenceVersionDependency.setGroupId("com.mock");
        differenceVersionDependency.setVersion("1.0-SNAPSHOT");

        // case1: 排除相同的依赖
        Set<Artifact> toFilterByExclude = Sets.newHashSet(sameArtifact);
        strategy.checkExcludeByParentIdentity(toFilterByExclude);
        verify(log)
            .info(
                eq("check excludeWithBaseDependencyParentIdentity success with base: com.mock:base-dependencies-starter:1.0"));

        // case2: 排除了基座没有的依赖
        toFilterByExclude = Sets.newHashSet(differentArtifact);
        try {
            strategy.checkExcludeByParentIdentity(toFilterByExclude);
        } catch (MojoExecutionException e) {
            // 验证构建失败
            verify(log)
                .error(
                    eq(String
                        .format(
                            "error to exclude package jar: %s because no such jar in base, please keep the jar or add it to base",
                            MavenUtils.getArtifactIdentity(differentArtifact))));

            assertEquals(String.format(
                "check excludeWithBaseDependencyParentIdentity failed with base: %s",
                config.getBaseDependencyParentIdentity()), e.getMessage());
        }

        // case3: 排除了不同版本的依赖
        toFilterByExclude = Sets.newHashSet(differentVersionArtifact);
        strategy.checkExcludeByParentIdentity(toFilterByExclude);
        verify(log)
            .error(
                eq(String
                    .format(
                        "error to exclude package jar: %s because it has different version with: %s in base, please keep the jar or set same version with base",
                        MavenUtils.getArtifactIdentity(differentVersionArtifact),
                        MavenUtils.getDependencyIdentity(differenceVersionDependency))));

        // case4: 配置开关：如果排除的依赖有问题，那么构建报错
        config.setBuildFailWhenExcludeBaseDependencyWithDiffVersion(true);
        try {
            strategy.checkExcludeByParentIdentity(toFilterByExclude);
        } catch (MojoExecutionException e) {
            // 验证构建失败
            assertEquals(String.format(
                "check excludeWithBaseDependencyParentIdentity failed with base: %s",
                config.getBaseDependencyParentIdentity()), e.getMessage());
        }
    }

    @Test
    public void testGetBaseDependencyParentOriginalModel() throws URISyntaxException {
        // find base-dependency-parent by gav identity
        ModuleSlimConfig config = (new ModuleSlimConfig())
            .setBaseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0");
        ModuleSlimExecutor strategy = new ModuleSlimExecutor(getMockBootstrapProject(), null, null,
            null, config, mockBaseDir(), null);
        assertNotNull(strategy.getBaseDependencyParentOriginalModel());

        // find base-dependency-parent by ga identity
        config.setBaseDependencyParentIdentity("com.mock:base-dependencies-starter");
        assertNotNull(strategy.getBaseDependencyParentOriginalModel());
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifactsByDefault() throws URISyntaxException,
                                                                  IOException {
        ModuleSlimConfig config = new ModuleSlimConfig();
        ModuleSlimExecutor strategy = new ModuleSlimExecutor(getMockBootstrapProject(), null, null,
            null, config, mockBaseDir(), mockLog());

        strategy.configExcludeArtifactsByDefault();

        // 验证 ark.properties
        assertTrue(config.getExcludes().contains("commons-beanutils:commons-beanutils"));
        assertTrue(config.getExcludeGroupIds().contains("org.springframework"));
        assertTrue(config.getExcludeArtifactIds().contains("sofa-ark-spi"));
        assertTrue(config.getIncludes().contains("com.alipay.sofa:sofa-ark-all"));
        assertTrue(config.getIncludeGroupIds().contains("com.alipay.sofa"));
        assertTrue(config.getIncludeArtifactIds().contains("sofa-ark-all"));

        // 验证 ark.yml
        assertTrue(config.getExcludes().contains("commons-beanutils:commons-beanutils-yml"));
        assertTrue(config.getExcludeGroupIds().contains("org.springframework-yml"));
        assertTrue(config.getExcludeArtifactIds().contains("sofa-ark-spi-yml"));
        assertTrue(config.getIncludes().contains("com.alipay.sofa:sofa-ark-all-yml"));
        assertTrue(config.getIncludeGroupIds().contains("com.alipay.sofa"));
        assertTrue(config.getIncludeArtifactIds().contains("sofa-ark-all-yml"));
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifacts() throws URISyntaxException {
        ModuleSlimConfig config = new ModuleSlimConfig();
        ModuleSlimExecutor strategy = new ModuleSlimExecutor(null, null, null, null, config,
            mockBaseDir(), mockLog());
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

        ModuleSlimExecutor strategy = new ModuleSlimExecutor(null, null, null, null, null,
            mockBaseDir(), mockLog());
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, true);
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, false);
    }

    @Test
    public void testExtensionExcludeAndIncludeArtifactsFromUrl() throws URISyntaxException, Exception {
        // 准备测试数据
        DefaultArtifact defaultArtifact = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        DefaultArtifact defaultArtifact1 = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(defaultArtifact);
        artifacts.add(defaultArtifact1);

        String packExcludesUrl = "http://mock-server.com/excludes";

        // Mock HTTP响应
        org.apache.http.client.methods.CloseableHttpResponse mockResponse = mock(org.apache.http.client.methods.CloseableHttpResponse.class);
        org.apache.http.StatusLine mockStatusLine = mock(org.apache.http.StatusLine.class);
        org.apache.http.HttpEntity mockEntity = mock(org.apache.http.HttpEntity.class);
        
        // 设置状态码为200
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        
        // 模拟返回的JSON数据
        ModuleSlimExecutor.ExcludeConfig excludeConfig = new ModuleSlimExecutor.ExcludeConfig();
        excludeConfig.setJarBlackGroupIds(Lists.newArrayList("com.test.black"));
        excludeConfig.setJarBlackArtifactIds(Lists.newArrayList("test-artifact-black"));
        excludeConfig.setJarBlackList(Lists.newArrayList("com.test.black:test-artifact-black:1.0"));

        excludeConfig.setJarWhiteGroupIds(Lists.newArrayList("com.test.white"));
        excludeConfig.setJarWhiteArtifactIds(Lists.newArrayList("test-artifact-white"));
        excludeConfig.setJarWhiteList(Lists.newArrayList("com.test.white:test-artifact-white:1.0"));

        excludeConfig.setJarWarnList(Lists.newArrayList("com.test.warn:test-artifact-warn:1.0"));
        excludeConfig.setJarWarnArtifactIds(Lists.newArrayList("test-artifact-warn"));
        excludeConfig.setJarWarnGroupIds(Lists.newArrayList("com.test.warn"));

        ModuleSlimExecutor.ExcludeConfigResponse excludeConfigResponse = new ModuleSlimExecutor.ExcludeConfigResponse();
        excludeConfigResponse.setSuccess(true);
        excludeConfigResponse.setResult(excludeConfig);
        ObjectMapper objectMapper = new ObjectMapper();
        String mockJsonResponse = objectMapper.writeValueAsString(excludeConfigResponse);
        when(mockEntity.getContent()).thenReturn(new java.io.ByteArrayInputStream(mockJsonResponse.getBytes()));

        // Mock HTTP客户端
        org.apache.http.impl.client.CloseableHttpClient mockClient = mock(org.apache.http.impl.client.CloseableHttpClient.class);
        when(mockClient.execute(org.mockito.ArgumentMatchers.any(org.apache.http.client.methods.HttpGet.class)))
            .thenReturn(mockResponse);

        // 使用Mockito的静态方法mock功能
        try (org.mockito.MockedStatic<org.apache.http.impl.client.HttpClients> httpClientsMock = 
                org.mockito.Mockito.mockStatic(org.apache.http.impl.client.HttpClients.class)) {
            
            httpClientsMock.when(org.apache.http.impl.client.HttpClients::createDefault)
                .thenReturn(mockClient);

            ModuleSlimConfig config = new ModuleSlimConfig();
            ModuleSlimExecutor strategy = new ModuleSlimExecutor(null, null, null, null,
                config, mockBaseDir(), mockLog());
            
            // 执行测试方法
            strategy.extensionExcludeArtifactsFromUrl(packExcludesUrl, artifacts);
            
            // 验证配置是否正确设置
            assertTrue(config.getExcludeGroupIds().contains("com.test.black"));
            assertTrue(config.getExcludeArtifactIds().contains("test-artifact-black"));
            assertTrue(config.getExcludes().contains("com.test.black:test-artifact-black:1.0"));
            assertTrue(config.getIncludeGroupIds().contains("com.test.white"));
            assertTrue(config.getIncludeArtifactIds().contains("test-artifact-white"));
            assertTrue(config.getIncludes().contains("com.test.white:test-artifact-white:1.0"));
        }
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

        ModuleSlimExecutor strategy = new ModuleSlimExecutor(null, null, null, null, null,
            mockBaseDir(), mockLog());
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, true);
        strategy.logExcludeMessage(jarGroupIds, jarArtifactIds, jarList, artifacts, false);
    }

    @Test
    public void testExcludeWithoutItsDependencies() throws URISyntaxException {
        MavenProject proj = mock(MavenProject.class);
        Artifact a1 = mockArtifact("com.exclude", "a1", "1.0.0", "jar", null, "compile");
        Artifact a2 = mockArtifact("com.exclude.group.id", "a2", "1.0.0", "jar", null, "compile");
        Artifact a3 = mockArtifact("com.exclude.artifact.id", "a3", "1.0.0", "jar", null, "compile");
        Artifact a4 = mockArtifact("com.include", "a4", "1.0.0", "jar", null, "compile");
        Set<Artifact> artifacts = Sets.newHashSet(a1, a2, a3, a4);

        ModuleSlimConfig moduleSlimConfig = new ModuleSlimConfig();
        moduleSlimConfig.setExcludes(Sets.newLinkedHashSet(Collections
            .singletonList("com.exclude:a1")));
        moduleSlimConfig.setExcludeGroupIds(Sets.newLinkedHashSet(Collections
            .singletonList("com.exclude.group.id")));
        moduleSlimConfig.setExcludeArtifactIds(Sets.newLinkedHashSet(Collections
            .singletonList("a3")));
        moduleSlimConfig.setExcludeWithIndirectDependencies(false);

        ModuleSlimExecutor strategy = spy(new ModuleSlimExecutor(proj, null, null, null,
            moduleSlimConfig, mockBaseDir(), null));
        Set<Artifact> res = strategy.getArtifactsToFilterByExcludeConfig(artifacts);
        assertEquals(3, res.size());
    }

    @Test
    public void testExcludeWithIndirectDependencies() throws URISyntaxException {
        MavenProject proj = mock(MavenProject.class);
        Artifact a1 = mockArtifact("com.exclude", "a1", "1.0.0", "jar", null, "compile");
        Artifact a2 = mockArtifact("com.exclude.group.id", "a2", "1.0.0", "jar", null, "compile");
        Artifact a3 = mockArtifact("com.exclude.artifact.id", "a3", "1.0.0", "jar", null, "compile");
        Artifact a4 = mockArtifact("com.include", "a4", "1.0.0", "jar", null, "compile");
        Artifact d1 = mockArtifact("com.exclude.dependency", "d1", "1.0.0", "jar", null, "compile");
        Artifact d2 = mockArtifact("com.exclude.dependency", "d2", "1.0.0", "jar", null, "compile");
        Artifact d3 = mockArtifact("com.exclude.dependency", "d3", "1.0.0", "jar", null, "compile");
        Artifact d4 = mockArtifact("com.exclude.dependency", "d4", "1.0.0", "jar", null, "compile");
        Artifact d5 = mockArtifact("com.include.dependency", "d5", "1.0.0", "jar", null, "compile");


        Set<Artifact> artifacts = Sets.newHashSet(a1, a2, a3, a4, d1,d2,d3,d4,d5);

        ModuleSlimConfig moduleSlimConfig = new ModuleSlimConfig();
        moduleSlimConfig.setExcludes(Sets.newLinkedHashSet(Collections.singletonList("com.exclude:a1")));
        moduleSlimConfig.setExcludeGroupIds(Sets.newLinkedHashSet(Collections.singletonList("com.exclude.group.id")));
        moduleSlimConfig.setExcludeArtifactIds(Sets.newLinkedHashSet(Collections.singletonList("a3")));
        moduleSlimConfig.setExcludeWithIndirectDependencies(true);

        /*
         * 依赖关系如下：
         *       -> a1 -> d1 -> d2
         *     /
         * root  -> a2 -> a3 -> d3
         *     \            \
         *      \            -> d4
         *       -> a4 -> d5
         * 在此依赖关系下，a1, a2, a3 会因为 exclude 被排除
         * d1, d2, d3, d4 会因为 excludeWithDependencies 被排除
         * a4, d5 不会被排除
         */
        DependencyNode root = mockNode(mockArtifact("com.mock", "root", "1.0", "jar", null, "compile"));
        DependencyNode a1Node = mockNode(a1);
        DependencyNode a2Node = mockNode(a2);
        DependencyNode a3Node = mockNode(a3);
        DependencyNode a4Node = mockNode(a4);
        DependencyNode d1Node = mockNode(d1);
        DependencyNode d2Node = mockNode(d2);
        DependencyNode d3Node = mockNode(d3);
        DependencyNode d4Node = mockNode(d4);
        DependencyNode d5Node = mockNode(d5);

        when(root.getChildren()).thenReturn(Lists.newArrayList(a1Node, a2Node,a4Node));
        when(a1Node.getChildren()).thenReturn(Lists.newArrayList(d1Node));
        when(d1Node.getChildren()).thenReturn(Lists.newArrayList(d2Node));
        when(a2Node.getChildren()).thenReturn(Lists.newArrayList(a3Node));
        when(a3Node.getChildren()).thenReturn(Lists.newArrayList(d3Node,d4Node));
        when(a4Node.getChildren()).thenReturn(Lists.newArrayList(d5Node));
        when(d2Node.getChildren()).thenReturn(Lists.newArrayList());
        when(d3Node.getChildren()).thenReturn(Lists.newArrayList(d4Node));
        when(d4Node.getChildren()).thenReturn(Lists.newArrayList());


        ModuleSlimExecutor strategy = spy(new ModuleSlimExecutor(proj,null,null, root, moduleSlimConfig,
                null, null));
        Set<Artifact> res = strategy.getArtifactsToFilterByExcludeConfig(artifacts);
        assertEquals(7, res.size());

        Set<String> resIdentities = res.stream().map(Artifact::getArtifactId).collect(Collectors.toSet());
        assertTrue(resIdentities.contains("a1"));
        assertTrue(resIdentities.contains("a2"));
        assertTrue(resIdentities.contains("a3"));
        assertTrue(resIdentities.contains("d1"));
        assertTrue(resIdentities.contains("d2"));
        assertTrue(resIdentities.contains("d3"));
        assertTrue(resIdentities.contains("d4"));
    }

    private DependencyNode mockNode(Artifact artifact) {
        DependencyNode node = mock(DependencyNode.class);
        when(node.getArtifact()).thenReturn(artifact);
        return node;
    }

    private Artifact mockArtifact(String groupId, String artifactId, String version, String type,
                                  String classifier, String scope) {
        Artifact artifact = mock(Artifact.class);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getVersion()).thenReturn(version);
        when(artifact.getType()).thenReturn(type);
        when(artifact.getClassifier()).thenReturn(classifier);
        when(artifact.getScope()).thenReturn(scope);
        return artifact;
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

        Model model = new Model();
        model.setGroupId("com.mock");
        model.setArtifactId("base-bootstrap");
        model.setVersion("0.0.1-SNAPSHOT");
        DependencyManagement dependencyManagement = new DependencyManagement();
        Dependency d = new Dependency();
        d.setType("pom");
        d.setScope("import");
        d.setGroupId("com.mock");
        d.setArtifactId("base-plugin-bom");
        d.setVersion("1.0");
        dependencyManagement.setDependencies(Lists.newArrayList(d));
        model.setDependencyManagement(dependencyManagement);
        project.setOriginalModel(model);

        setField("basedir", project, CommonUtils.getResourceFile("baseDir"));
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
        when(artifact.getBaseVersion()).thenReturn("1.0");

        project.setArtifact(artifact);
        project.setParent(null);

        Dependency sameDependency = new Dependency();
        sameDependency.setArtifactId("same-dependency-artifact");
        sameDependency.setGroupId("com.mock");
        sameDependency.setVersion("1.0-SNAPSHOT");

        Dependency differenceDependency = new Dependency();
        differenceDependency.setArtifactId("difference-dependency-artifact");
        differenceDependency.setGroupId("com.mock");
        differenceDependency.setVersion("1.0-SNAPSHOT");

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
        return CommonUtils.getResourceFile("baseDir");
    }
}