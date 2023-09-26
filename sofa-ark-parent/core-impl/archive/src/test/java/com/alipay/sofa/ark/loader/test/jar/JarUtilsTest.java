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
package com.alipay.sofa.ark.loader.test.jar;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.loader.jar.JarUtils;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class JarUtilsTest {

    @Test
    public void getArtifactIdFromTestClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("sample-biz-withjar.jar");
        String artifactId = JarUtils.parseArtifactId(url.getPath());
        Assert.assertEquals("sofa-ark-sample-springboot-ark", artifactId);
    }

    @Test
    public void getArtifactIdFromTestClassPath1() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("SampleClass.class");
        String artifactId = JarUtils.parseArtifactId(url.getPath());
        Assert.assertEquals("sofa-ark-archive", artifactId);
    }

    @Test
    public void getArtifactIdFromClassPath() throws IOException, URISyntaxException {
        URL clazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/loader/jar/JarUtils.class");

        String artifactId = JarUtils.parseArtifactId(clazzURL.getPath());
        Assert.assertEquals("sofa-ark-archive", artifactId);

        URL testClazzURL = this.getClass().getClassLoader()
                .getResource("com/alipay/sofa/ark/loader/test/jar/JarUtilsTest.class");

        artifactId = JarUtils.parseArtifactId(testClazzURL.getPath());
        Assert.assertEquals("sofa-ark-archive", artifactId);

        URI classPathRoot = this.getClass().getClassLoader().getResource("").toURI();
        String classPath = Paths.get(classPathRoot).getParent().toFile().getAbsolutePath();
        String artifactId1 = JarUtils.parseArtifactId(classPath);
        Assert.assertNotNull(artifactId1);
    }

    @Test
    public void testParseArtifactIdFromJarName() throws Exception {
        Method method = JarUtils.class.getDeclaredMethod("doGetArtifactIdFromFileName",
            String.class);
        method.setAccessible(Boolean.TRUE);

        String filePathPrefix = "file:///home/admin/xxx/xxx/%s.jar";

        String artifactId0 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "dafdfa-2-dafdfad"));
        Assert.assertNull(artifactId0);

        String artifactId2 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "dfadfa-dfadfa-3.0"));
        Assert.assertEquals(artifactId2, "dfadfa-dfadfa");

        String artifactId3 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "hessian-4.0.7.bugfix12-tuning3"));
        Assert.assertEquals(artifactId3, "hessian");

        String artifactId4 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "hessian-4.0.7"));
        Assert.assertEquals(artifactId4, "hessian");
    }

    @Test
    public void testParseArtifactIdFromJarInJarName() throws Exception {
        Method method = JarUtils.class.getDeclaredMethod("doGetArtifactIdFromFileName",
            String.class);
        method.setAccessible(Boolean.TRUE);

        String filePathPrefix = "file:///home/admin/xxx/xxx/bootstrap-executable.jar!/META-INF/lib/%s.jar";

        String artifactId0 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "dafdfa-2-dafdfad"));
        Assert.assertNull(artifactId0);

        String artifactId1 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "jar-2-version-suffix"));
        Assert.assertNull(artifactId1);

        String artifactId2 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "dfadfa-dfadfa-3.0"));
        Assert.assertEquals(artifactId2, "dfadfa-dfadfa");

        String artifactId3 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "hessian-4.0.7.bugfix12-tuning3"));
        Assert.assertEquals(artifactId3, "hessian");

        String artifactId4 = (String) method.invoke(JarUtils.class,
            String.format(filePathPrefix, "hessian-4.0.7"));
        Assert.assertEquals(artifactId4, "hessian");
    }

    @Test
    public void testParseArtifactIdFromJarInJar() throws Exception {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        Method method = JarUtils.class.getDeclaredMethod("parseArtifactIdFromJarInJar",
            String.class);
        method.setAccessible(Boolean.TRUE);
        Assert.assertEquals("slf4j-api",
            method.invoke(JarUtils.class, jar.getFile() + "!/lib/slf4j-api-1.7.30.jar"));
    }

    @Test
    public void testParseArtifactIdFromJarInJarPom() {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        String artifactId0 = JarUtils.parseArtifactId(jar.getFile()
                                                      + "!/lib/slf4j-api-1.7.30.jar!/");
        Assert.assertEquals("slf4j-api", artifactId0);
    }

    @Test
    public void testParseArtifactIdFromJarWithBlankPath() throws Exception {
        URL jar = JarUtilsTest.class.getResource("/junit-4.12.jar");
        URL root = JarUtilsTest.class.getResource("/");
        String fullPath = root.getPath() + "space directory";
        String jarLocation = fullPath + "/junit-4.12.jar";
        FileUtils.mkdir(fullPath);
        Files.copy(new File(jar.getFile()), new File(jarLocation));

        URL url = JarUtilsTest.class.getResource("/space directory/junit-4.12.jar");
        Method method = JarUtils.class.getDeclaredMethod("parseArtifactIdFromJar", String.class);
        method.setAccessible(Boolean.TRUE);
        Assert.assertNull(method.invoke(JarUtils.class, url.getPath()));
    }

    @Test
    public void testParseArtifactIdFromJarInJarInJarMore() {
        URL jar = JarUtilsTest.class.getResource("/example-jarinjarinjar.jar");
        String artifactId0 = JarUtils
            .parseArtifactId(jar.getFile()
                             + "!/BOOT-INF/lib/example-client-2.0.0.jar!/BOOT-INF/lib/sofa-ark-spring-guides-230525-SOFA.jar!/");
        Assert.assertEquals("sofa-ark-spring-guides", artifactId0);

        String artifactId1 = JarUtils
            .parseArtifactId(jar.getFile()
                             + "!/BOOT-IN/lib/example-client-2.0.0.jar!/BOOT-INF/lib/example-client-3.0.0.jar!/");
        Assert.assertEquals("example-client", artifactId1);
    }
}
