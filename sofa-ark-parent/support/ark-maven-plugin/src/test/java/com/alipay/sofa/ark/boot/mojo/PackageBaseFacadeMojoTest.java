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

import cn.hutool.core.util.RuntimeUtil;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static com.alipay.sofa.ark.boot.mojo.PackageBaseFacadeMojo.JVMFileTypeEnum.JAVA;
import static com.alipay.sofa.ark.boot.mojo.PackageBaseFacadeMojo.JVMFileTypeEnum.KOTLIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: PackageBaseFacadeMojoTest.java, v 0.1 2024年06月06日 21:03 立蓬 Exp $
 */
public class PackageBaseFacadeMojoTest {
    private MavenProject bootstrapProject = getMockBootstrapProject();

    public PackageBaseFacadeMojoTest() throws URISyntaxException {
    }

    @Test
    public void testExecute() throws Exception {

        //String baseRootAbsPath = CommonUtils.getResourceFile("baseRoot").getAbsolutePath();
        //String commandForMavenInstall = "cd " + baseRootAbsPath
        //                                + ";mvn clean install -Dmaven.test.skip=true";
        //Process process = RuntimeUtil.exec("/bin/sh", "-c", "-l", commandForMavenInstall);
        //if (process.waitFor() == 0) {
        //    System.out.println("execute success: " + commandForMavenInstall);
        //} else {
        //    throw new Exception("execute failed: " + commandForMavenInstall);
        //}
        //
        //PackageBaseFacadeMojo mojo = new PackageBaseFacadeMojo();
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "mavenProject", bootstrapProject);
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "artifactId",
        //    "base-all-dependencies-facade");
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "version", "1.0.0");
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "baseDir", bootstrapProject.getBasedir());
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "cleanAfterPackage", "true");
        //
        //MavenSession mavenSession = mock(MavenSession.class);
        //doReturn(new Settings()).when(mavenSession).getSettings();
        //doReturn(new DefaultMavenExecutionRequest()).when(mavenSession).getRequest();
        //setPrivateField(PackageBaseFacadeMojo.class, mojo, "mavenSession", mavenSession);
        //
        //String oldMavenHome = System.getProperty("maven.home");
        //try {
        //    String commandForMavenHome = "mvn --version | grep 'Maven home' |sed 's/^Maven home: //g'";
        //    String mavenHome = RuntimeUtil.execForStr("/bin/sh", "-c", "-l", commandForMavenHome)
        //        .trim();
        //    System.setProperty("maven.home", mavenHome);
        //    mojo.execute();
        //    assertTrue(CommonUtils
        //        .resourceExists("baseRoot/base-bootstrap/outputs/base-all-dependencies-facade-1.0.0.jar"));
        //} finally {
        //    if (null == oldMavenHome) {
        //        System.clearProperty("maven.home");
        //    } else {
        //        System.setProperty("maven.home", oldMavenHome);
        //    }
        //}
    }

    private void setPrivateField(Class clazz, Object instance, String fieldName, Object value)
                                                                                              throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(instance, value);
    }

    @Test
    public void testGetSupportedJVMFiles() throws URISyntaxException {
        List<File> files = PackageBaseFacadeMojo.getSupportedJVMFiles(CommonUtils
            .getResourceFile("baseRoot"));
        assertEquals(3, files.size());
    }

    @Test
    public void testParseFullClassName() throws URISyntaxException {
        assertEquals(
            "com.mock.base.bootstrap.BootstrapModel",
            JAVA.parseFullClassName(CommonUtils
                .getResourceFile("baseRoot/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertEquals(
            "com.mock.base.facade.ModuleDescriptionInfo",
            KOTLIN.parseFullClassName(CommonUtils
                .getResourceFile("baseRoot/base-facade/src/main/kotlin/com/mock/base/facade/ModuleDescriptionInfo.kt")));
    }

    @Test
    public void testMatches() throws URISyntaxException {
        assertTrue(JAVA
            .matches(CommonUtils
                .getResourceFile("baseRoot/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertTrue(KOTLIN
            .matches(CommonUtils
                .getResourceFile("baseRoot/base-facade/src/main/kotlin/com/mock/base/facade/ModuleDescriptionInfo.kt")));

        assertFalse(KOTLIN
            .matches(CommonUtils
                .getResourceFile("baseRoot/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertFalse(JAVA.matches(CommonUtils
            .getResourceFile("baseRoot/base-bootstrap/src/main/resources/BootstrapModel.java")));
    }

    @Test
    public void testParseRelativePath() throws URISyntaxException {
        assertEquals(
            "src/main/java/com/mock/base/bootstrap/BootstrapModel.java",
            JAVA.parseRelativePath(CommonUtils
                .getResourceFile("baseRoot/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
        assertNull(KOTLIN
            .parseRelativePath(CommonUtils
                .getResourceFile("baseRoot/base-bootstrap/src/main/java/com/mock/base/bootstrap/BootstrapModel.java")));
    }

    @Test
    public void testGetBaseModuleArtifactIds() throws Exception {
        PackageBaseFacadeMojo mojo = new PackageBaseFacadeMojo();
        Field field = PackageBaseFacadeMojo.class.getDeclaredField("mavenProject");
        field.setAccessible(true);
        field.set(mojo, bootstrapProject);

        Method method = PackageBaseFacadeMojo.class.getDeclaredMethod("getBaseModuleArtifactIds");
        method.setAccessible(true);
        Set<String> moduleArtifactIds = (Set<String>) method.invoke(mojo);

        assertTrue(moduleArtifactIds.contains("base-bootstrap"));
        assertTrue(moduleArtifactIds.contains("base-facade"));
    }

    private MavenProject getMockBootstrapProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base-bootstrap");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("jar");
        project.setFile(CommonUtils.getResourceFile("baseRoot/base-bootstrap/pom.xml"));
        project.setParent(getRootProject());
        project.setProjectBuildingRequest(new DefaultProjectBuildingRequest());
        return project;
    }

    private MavenProject getRootProject() throws URISyntaxException {
        MavenProject project = new MavenProject();
        project.setArtifactId("base");
        project.setGroupId("com.mock");
        project.setVersion("0.0.1-SNAPSHOT");
        project.setPackaging("pom");
        project.setFile(CommonUtils.getResourceFile("baseRoot/pom.xml"));
        project.setParent(null);
        project.setModel(MavenUtils.buildPomModel(project.getFile()));
        return project;
    }
}