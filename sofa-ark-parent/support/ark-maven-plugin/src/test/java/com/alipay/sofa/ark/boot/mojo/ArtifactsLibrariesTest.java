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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.monitor.logging.DefaultLog;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ArtifactsLibrariesTest {

    public ArtifactsLibrariesTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testDoWithLibraries() throws IOException {

        Set<Artifact> artifacts = new HashSet<>();
        DefaultArtifact artifact = new DefaultArtifact("group1", "artifact1", "1.0", "compile", "", null, new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group1", "artifact1", "2.0", "provided", "", null, new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group1", "artifact2", "2.0", "runtime", "", null, new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group2", "artifact2", "2.0", "compile", "", null, new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group2", "artifact3", "2.0", "provided", "", "clsf", new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group3", "artifact3", "2.0", "runtime", "", "clsf", new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        artifact = new DefaultArtifact("group3", "artifact4", "2.0", null, "", "clsf", new DefaultArtifactHandler());
        artifact.setFile(new File("./"));
        artifacts.add(artifact);

        List<Dependency> dependencies = new ArrayList<>();
        Dependency dependency = new Dependency();
        dependency.setGroupId("group3");
        dependency.setArtifactId("artifact3");
        dependencies.add(dependency);

        AtomicInteger atomicInteger = new AtomicInteger(0);
        new ArtifactsLibraries(artifacts, dependencies, new DefaultLog(new ConsoleLogger())).doWithLibraries(
                library -> atomicInteger.incrementAndGet());

        assertEquals(artifacts.size() - 1, atomicInteger.get());
    }
}
