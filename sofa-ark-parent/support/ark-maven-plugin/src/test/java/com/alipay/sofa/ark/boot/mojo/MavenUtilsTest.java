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
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static com.alipay.sofa.ark.boot.mojo.MavenUtils.*;
import static org.junit.Assert.assertEquals;

public class MavenUtilsTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsRootProject() {

        assertEquals(true, isRootProject(null));
        MavenProject parentMavenProject = new MavenProject();
        parentMavenProject.setFile(new File("./a"));
        MavenProject mavenProject = new MavenProject();
        mavenProject.setParent(parentMavenProject);
        assertEquals(false, isRootProject(mavenProject));
        parentMavenProject.setFile(null);
        assertEquals(true, isRootProject(mavenProject));
        assertEquals(null, getRootProject(null));
    }

    @Test
    public void testConvert() {

        assertEquals(new HashSet<>(), convert(""));
        assertEquals(new HashSet<>(), convert("\n"));
        assertEquals(new HashSet<>(), convert("\n\r"));
        assertEquals(new HashSet<>(), convert("\n\r"));
        assertEquals(new HashSet<>(), convert("a\na"));

        Set<ArtifactItem> artifactItems = new HashSet<>();
        ArtifactItem artifactItem = new ArtifactItem();
        artifactItem.setGroupId("org.springframework.boot");
        artifactItem.setArtifactId("spring-boot");
        artifactItem.setType("jar");
        artifactItem.setVersion("2.7.14");
        artifactItem.setScope("provided");
        artifactItems.add(artifactItem);
        artifactItem = new ArtifactItem();
        artifactItem.setGroupId("org.springframework");
        artifactItem.setArtifactId("spring-jcl");
        artifactItem.setType("jar");
        artifactItem.setVersion("5.3.29");
        artifactItem.setScope("provided");
        artifactItem.setClassifier("ark-biz");
        artifactItems.add(artifactItem);

        assertEquals(
            artifactItems,
            convert("[INFO] com.alipay.sofa:sofa-ark-springboot-starter:jar:2.2.4-SNAPSHOT\n"
                    + "[INFO] +- org.springframework.boot:spring-boot:jar:2.7.14:provided\n"
                    + "[INFO] |  |  \\- org.springframework:spring-jcl:jar:ark-biz:5.3.29:provided\n"
                    + "[INFO] |  \\- org.springframework:spring-context:jar:5.3.29"));
    }
}
