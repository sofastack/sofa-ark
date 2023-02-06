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

import org.apache.commons.collections.CollectionUtils;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.alipay.sofa.ark.tools.ArtifactItem;
import com.google.common.collect.Lists;

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
}
