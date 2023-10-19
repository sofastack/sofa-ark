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

import com.alipay.sofa.ark.tools.ArtifactItem;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * @author guolei.sgl (guolei.sgl@antfin.com) 2020/12/16 2:25 下午
 * @since
 **/
public class RepackageMojoTest {

    @Test
    public void testRepackageMojo() throws NoSuchMethodException, InvocationTargetException,
                                   IllegalAccessException, NoSuchFieldException {
        RepackageMojo repackageMojo = new RepackageMojo();
        Method extensionExcludeArtifacts = repackageMojo.getClass().getDeclaredMethod(
            "extensionExcludeArtifacts", String.class);

        extensionExcludeArtifacts.setAccessible(true);
        URL resource = this.getClass().getClassLoader().getResource("excludes.txt");
        extensionExcludeArtifacts.invoke(repackageMojo, resource.getPath());
        Field excludes = repackageMojo.getClass().getDeclaredField("excludes");
        Field excludeGroupIds = repackageMojo.getClass().getDeclaredField("excludeGroupIds");
        Field excludeArtifactIds = repackageMojo.getClass().getDeclaredField("excludeArtifactIds");

        excludes.setAccessible(true);
        excludeGroupIds.setAccessible(true);
        excludeArtifactIds.setAccessible(true);

        Object excludesResult = excludes.get(repackageMojo);
        Object excludeGroupIdResult = excludeGroupIds.get(repackageMojo);
        Object excludeArtifactIdsResult = excludeArtifactIds.get(repackageMojo);
        Assert.assertTrue(excludesResult instanceof LinkedHashSet
                          && excludeGroupIdResult instanceof LinkedHashSet
                          && excludeArtifactIdsResult instanceof LinkedHashSet);
        Assert.assertTrue(((LinkedHashSet) excludesResult).contains("tracer-core:3.0.10")
                          && ((LinkedHashSet) excludesResult).contains("tracer-core:3.0.11"));
    }

    /**
     * 测试依赖解析
     *
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Test
    public void testParseArtifactItems() throws NoSuchMethodException, InvocationTargetException,
                                        IllegalAccessException {

        /**
         *  构建依赖图
         *
         *                   biz-child1-child1
         *                 /
         *        biz-child1
         *       /         \
         *     /            biz-child1-child2
         *  biz
         *     \            biz-child2-child1
         *      \          /
         *      biz-child2
         *                 \
         *                  biz-child2-child1
         *
         */
        DefaultDependencyNode bizNode = buildDependencyNode(null, "com.alipay.sofa", "biz", "1.0.0");
        DefaultDependencyNode bizChild1 = buildDependencyNode(bizNode, "com.alipay.sofa",
            "biz-child1", "1.0.0");
        DefaultDependencyNode bizChild2 = buildDependencyNode(bizNode, "com.alipay.sofa",
            "biz-child2", "1.0.0");
        buildDependencyNode(bizChild1, "com.alipay.sofa", "biz-child1-child1", "1.0.0");
        buildDependencyNode(bizChild1, "com.alipay.sofa", "biz-child1-child2", "1.0.0");
        buildDependencyNode(bizChild2, "com.alipay.sofa", "biz-child2-child1", "1.0.0");
        buildDependencyNode(bizChild2, "com.alipay.sofa", "biz-child2-child2", "1.0.0");

        RepackageMojo repackageMojo = new RepackageMojo();
        Method parseArtifactItems = repackageMojo.getClass().getDeclaredMethod(
            "parseArtifactItems", DependencyNode.class, Set.class);
        parseArtifactItems.setAccessible(true);
        Set<ArtifactItem> artifactItems = new HashSet<>();
        parseArtifactItems.invoke(repackageMojo, bizNode, artifactItems);
        Assert.assertTrue(artifactItems.size() == 7);
    }

    private DefaultDependencyNode buildDependencyNode(DefaultDependencyNode parent, String groupId,
                                                      String artifactId, String version) {
        DefaultDependencyNode dependencyNode = new DefaultDependencyNode(parent,
            new DefaultArtifact(groupId, artifactId, version, "provided", "jar", "biz-jar", null),
            null, null, null);
        if (parent != null) {
            if (CollectionUtils.isEmpty(parent.getChildren())) {
                parent.setChildren(Lists.newArrayList());
            }
            parent.getChildren().add(dependencyNode);
        }
        return dependencyNode;
    }

    @Test
    public void testSetSettingsLocation() throws Exception {
        String userSettingsFilePath = System.getProperty("user.home") + File.separator
                                      + "user-settings-test.xml";
        String globalSettingsFilePath = System.getProperty("user.home") + File.separator
                                        + "global-settings-test.xml";
        File userSettingsFile = new File(userSettingsFilePath);
        File globalSettingsFile = new File(globalSettingsFilePath);
        InvocationRequest request = new DefaultInvocationRequest();
        invokeSetSettingsLocation(request, userSettingsFilePath, globalSettingsFilePath);
        Assert.assertNull(request.getUserSettingsFile());
        Assert.assertNull(request.getGlobalSettingsFile());

        Files.touch(globalSettingsFile);
        invokeSetSettingsLocation(request, userSettingsFilePath, globalSettingsFilePath);
        Assert.assertNull(request.getUserSettingsFile());
        Assert.assertNotNull(request.getGlobalSettingsFile());

        Files.touch(userSettingsFile);
        invokeSetSettingsLocation(request, userSettingsFilePath, globalSettingsFilePath);
        Assert.assertNotNull(request.getUserSettingsFile());
        Assert.assertNotNull(request.getGlobalSettingsFile());

        FileUtils.deleteQuietly(userSettingsFile);
        FileUtils.deleteQuietly(globalSettingsFile);
    }

    private void invokeSetSettingsLocation(InvocationRequest request, String userSettingsFilePath,
                                           String globalSettingsFilePath) throws Exception {
        RepackageMojo repackageMojo = new RepackageMojo();
        MavenExecutionRequest executionRequest = new DefaultMavenExecutionRequest();
        executionRequest.setUserSettingsFile(new File(userSettingsFilePath));
        executionRequest.setGlobalSettingsFile(new File(globalSettingsFilePath));
        // 构造对象
        MavenSession mavenSession = new MavenSession(null, executionRequest, null,
            new ArrayList<>());
        Field mavenSessionField = repackageMojo.getClass().getDeclaredField("mavenSession");
        mavenSessionField.setAccessible(true);
        mavenSessionField.set(repackageMojo, mavenSession);

        Method setSettingsLocation = repackageMojo.getClass().getDeclaredMethod(
            "setSettingsLocation", InvocationRequest.class);
        setSettingsLocation.setAccessible(true);
        setSettingsLocation.invoke(repackageMojo, request);
    }

    @Test
    public void testExtensionExcludeArtifactsFromUrl() throws NoSuchMethodException,
                                                      InvocationTargetException,
                                                      IllegalAccessException {
        RepackageMojo repackageMojo = new RepackageMojo();
        Method extensionExcludeArtifactsFromUrl = repackageMojo.getClass().getDeclaredMethod(
            "extensionExcludeArtifactsFromUrl", String.class, Set.class);
        extensionExcludeArtifactsFromUrl.setAccessible(true);

        DefaultArtifact defaultArtifact = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        DefaultArtifact defaultArtifact1 = new DefaultArtifact("groupId", "artifactId", "version",
            "provided", "jar", null, new DefaultArtifactHandler());
        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(defaultArtifact);
        artifacts.add(defaultArtifact1);

        // NOTE: Access github to run unit test, need vpn maybe.
        String packExcludesUrl = "https://github.com/sofastack/sofa-ark";
        extensionExcludeArtifactsFromUrl.invoke(repackageMojo, packExcludesUrl, artifacts);
    }

    @Test
    public void testLogExcludeMessage() throws NoSuchMethodException, InvocationTargetException,
                                       IllegalAccessException {
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

        RepackageMojo repackageMojo = new RepackageMojo();
        Method logExcludeMessage = repackageMojo.getClass().getDeclaredMethod("logExcludeMessage",
            List.class, List.class, List.class, Set.class, boolean.class);
        logExcludeMessage.setAccessible(true);
        logExcludeMessage.invoke(repackageMojo, jarGroupIds, jarArtifactIds, jarList, artifacts,
            true);

        logExcludeMessage.invoke(repackageMojo, jarGroupIds, jarArtifactIds, jarList, artifacts,
            false);
    }

    @Test
    public void testIsSameWithVersion() {
        ArtifactItem artifactItem = new ArtifactItem();
        artifactItem.setGroupId("groupId1");
        artifactItem.setArtifactId("artifactId");
        artifactItem.setVersion("1.1.1");
        Assert.assertFalse(artifactItem.isSameWithVersion(null));
        ArtifactItem artifactItem1 = new ArtifactItem();
        artifactItem1.setGroupId("groupId1");
        artifactItem1.setArtifactId("artifactId");
        artifactItem1.setVersion("1.1.1");
        Assert.assertTrue(artifactItem.isSameWithVersion(artifactItem1));
        artifactItem1.setVersion("2.2.2");
        Assert.assertFalse(artifactItem.isSameWithVersion(artifactItem1));
        artifactItem1.setVersion("*");
        Assert.assertTrue(artifactItem.isSameWithVersion(artifactItem1));
    }

}
