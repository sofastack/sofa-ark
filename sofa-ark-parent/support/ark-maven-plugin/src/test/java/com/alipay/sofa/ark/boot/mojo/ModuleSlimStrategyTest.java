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
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anySet;
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
    public void testGetSlimmedArtifacts() throws MojoExecutionException {
        MavenProject proj = mock(MavenProject.class);
        Artifact a1 = mock(Artifact.class);
        Artifact a2 = mock(Artifact.class);
        Artifact a3 = mock(Artifact.class);

        Set<Artifact> artifacts = Sets.newHashSet(a1, a2, a3);
        when(proj.getArtifacts()).thenReturn(artifacts);

        ModuleSlimStrategy strategy = spy(new ModuleSlimStrategy(proj, null, null));
        doReturn(Sets.newHashSet(a1)).when(strategy).getArtifactsToFilterByParentIdentity(anySet());
        doReturn(Sets.newHashSet(a2)).when(strategy).getArtifactsToFilterByExcludeConfig(anySet());

        assertEquals(1, strategy.getSlimmedArtifacts().size());
    }

    @Test
    public void testGetArtifactsToFilterByParentIdentity() throws URISyntaxException,
                                                          MojoExecutionException {
        ModuleSlimConfig config = ModuleSlimConfig.builder()
            .baseDependencyParentIdentity("com.mock:base-dependencies-starter:1.0").build();
        ModuleSlimStrategy strategy = new ModuleSlimStrategy(getMockBootstrapProject(), config,
            null);

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
        return project;
    }

    private MavenProject getRootProject() throws URISyntaxException {
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
}