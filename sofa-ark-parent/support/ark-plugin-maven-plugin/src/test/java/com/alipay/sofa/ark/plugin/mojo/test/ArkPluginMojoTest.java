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
package com.alipay.sofa.ark.plugin.mojo.test;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.plugin.mojo.ArkPluginMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class ArkPluginMojoTest {
    @Test
    public void testArkPluginMojo() throws Exception {
        Artifact artifact = Mockito.mock(Artifact.class);
        MavenProject project = Mockito.mock(MavenProject.class);

        when(artifact.getGroupId()).thenReturn("invalid");
        when(artifact.getArtifactId()).thenReturn("invalid");
        when(project.getArtifact()).thenReturn(artifact);
        when(artifact.getFile()).thenReturn(
            new File(Test.class.getProtectionDomain().getCodeSource().getLocation().getPath()));

        ArkPluginMojo arkPluginMojo = new ArkPluginMojo();
        arkPluginMojo.setProject(project);
        arkPluginMojo.setShades(new LinkedHashSet<>(Collections
            .singleton("com.alipay.sofa:test-demo:1.0.0")));
        final URL url = this.getClass().getClassLoader().getResource("test-demo.jar");

        String path = url.getPath() + ".shaded";
        String shadedUrl = url.toExternalForm() + ".shaded";
        String copyPath = url.getPath() + ".copy";
        File copyFileForTest = new File(copyPath);

        FileInputStream demoJar = new FileInputStream(url.getPath());
        FileUtils.copyInputStreamToFile(demoJar, new File(copyPath));
        demoJar.close();

        arkPluginMojo.shadeJarIntoArkPlugin(new File(path), copyFileForTest,
            Collections.singleton(artifact));

        assertTrue(copyFileForTest.delete());
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { new URL(shadedUrl) }, null);
        assertNotNull(urlClassLoader.loadClass("com.alipay.sofa.support.test.SampleService"));
        assertNotNull(urlClassLoader.loadClass("org.junit.Test"));
    }

    @Test
    public void testShadeJar() {
        ArkPluginMojo arkPluginMojo = new ArkPluginMojo();
        arkPluginMojo.setShades(new LinkedHashSet<>(Collections
            .singleton("com.alipay.sofa:test-demo:1.0.0")));

        MavenProject projectOne = Mockito.mock(MavenProject.class);
        MavenProject projectTwo = Mockito.mock(MavenProject.class);
        Artifact artifact = Mockito.mock(Artifact.class);

        when(projectOne.getGroupId()).thenReturn("com.alipay.sofa");
        when(projectOne.getArtifactId()).thenReturn("test-demo");

        when(projectTwo.getGroupId()).thenReturn("com.alipay.sofa");
        when(projectTwo.getArtifactId()).thenReturn("");

        when(artifact.getGroupId()).thenReturn("com.alipay.sofa");
        when(artifact.getArtifactId()).thenReturn("test-demo");
        when(artifact.getVersion()).thenReturn("1.0.0");

        arkPluginMojo.setProject(projectOne);
        try {
            arkPluginMojo.isShadeJar(artifact);
        } catch (Exception ex) {
            assertTrue(ex.getMessage().equals("Can't shade jar-self."));
        }

        arkPluginMojo.setProject(projectTwo);
        assertTrue(arkPluginMojo.isShadeJar(artifact));
    }
}
