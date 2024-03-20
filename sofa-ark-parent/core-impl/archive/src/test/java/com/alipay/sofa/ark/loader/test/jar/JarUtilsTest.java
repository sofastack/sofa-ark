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
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.alipay.sofa.ark.loader.jar.JarUtils.parseArtifactId;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JarUtilsTest {

    @Test
    public void getArtifactIdFromTestClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("sample-biz-withjar.jar");
        String artifactId = parseArtifactId(url.getPath());
        assertEquals("sofa-ark-sample-springboot-ark", artifactId);
    }

    @Test
    public void getArtifactIdFromTestClassPath1() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("SampleClass.class");
        String artifactId = parseArtifactId(url.getPath());
        assertEquals("sofa-ark-archive", artifactId);
    }

    @Test
    public void getArtifactIdFromClassPath() throws IOException, URISyntaxException {

        URL clazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/loader/jar/JarUtils.class");
        String artifactId = parseArtifactId(clazzURL.getPath());
        assertEquals("sofa-ark-archive", artifactId);

        URL testClazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/loader/test/jar/JarUtilsTest.class");
        artifactId = parseArtifactId(testClazzURL.getPath());
        assertEquals("sofa-ark-archive", artifactId);

        String path = this.getClass().getClassLoader().getResource("example-jarinjarinjar.jar")
            .getPath();
        String artifactId1 = parseArtifactId(path + "!/lib");
        assertEquals("example-client", artifactId1);

        URL fatJarURL = this.getClass().getClassLoader().getResource("sample-springboot-fat-biz.jar");
        List<URL> urls = extractResourceURLs(fatJarURL.getPath(), "BOOT-INF/classes/");
        assertEquals(1, urls.size());
        urls.forEach(url -> assertEquals("sofa-ark-sample-springboot-ark", parseArtifactId(url.getPath())));
    }

    private List<URL> extractResourceURLs(String pathToFatJar, String resourcePattern) {
        List<URL> resourceUrls = new ArrayList<>();
        try (JarFile jarFile = new JarFile(new File(pathToFatJar))) {

            Enumeration<JarEntry> entryEnumeration = jarFile.entries();
            while (entryEnumeration.hasMoreElements()) {
                JarEntry entry = entryEnumeration.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(resourcePattern)) { // 检查资源模式匹配
                    // 构建jar内资源的URL
                    URL entryUrl = new URL("jar:file:" + pathToFatJar + "!/" + entryName);
                    resourceUrls.add(entryUrl);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract resource URLs from " + pathToFatJar, e);
        }

        return resourceUrls;
    }

    @Test
    public void testParseArtifactIdFromJarName() throws Exception {

        Method method = JarUtils.class.getDeclaredMethod("doGetArtifactIdFromFileName",
            String.class);
        method.setAccessible(Boolean.TRUE);

        String filePathPrefix = "file:///home/admin/xxx/xxx/%s.jar";
        String artifactId0 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "dafdfa-2-dafdfad"));
        assertNull(artifactId0);

        String artifactId2 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "dfadfa-dfadfa-3.0"));
        assertEquals(artifactId2, "dfadfa-dfadfa");

        String artifactId3 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "hessian-4.0.7.bugfix12-tuning3"));
        assertEquals(artifactId3, "hessian");

        String artifactId4 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "hessian-4.0.7"));
        assertEquals(artifactId4, "hessian");
    }

    @Test
    public void testParseArtifactIdFromJarInJarName() throws Exception {

        Method method = JarUtils.class.getDeclaredMethod("doGetArtifactIdFromFileName",
            String.class);
        method.setAccessible(Boolean.TRUE);

        String filePathPrefix = "file:///home/admin/xxx/xxx/bootstrap-executable.jar!/META-INF/lib/%s.jar";
        String artifactId0 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "dafdfa-2-dafdfad"));
        assertNull(artifactId0);

        String artifactId1 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "jar-2-version-suffix"));
        assertNull(artifactId1);

        String artifactId2 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "dfadfa-dfadfa-3.0"));
        assertEquals(artifactId2, "dfadfa-dfadfa");

        String artifactId3 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "hessian-4.0.7.bugfix12-tuning3"));
        assertEquals(artifactId3, "hessian");

        String artifactId4 = (String) method.invoke(JarUtils.class,
            format(filePathPrefix, "hessian-4.0.7"));
        assertEquals(artifactId4, "hessian");
    }

    @Test
    public void testParseArtifactIdFromJarInJar() throws Exception {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        Method method = JarUtils.class.getDeclaredMethod("parseArtifactIdFromJar", String.class);
        method.setAccessible(Boolean.TRUE);
        assertEquals("slf4j-api",
            method.invoke(JarUtils.class, jar.getFile() + "!/lib/slf4j-api-1.7.30.jar"));
    }

    @Test
    public void testParseArtifactIdFromJarInJarPom() {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        String artifactId0 = parseArtifactId(jar.getFile() + "!/lib/slf4j-api-1.7.30.jar!/");
        assertEquals("slf4j-api", artifactId0);
    }

    @Test
    public void testParseArtifactIdFromJarWithBlankPath() throws Exception {

        URL jar = JarUtilsTest.class.getResource("/junit-4.12.jar");
        URL root = JarUtilsTest.class.getResource("/");
        String fullPath = root.getPath() + "space directory";
        String jarLocation = fullPath + "/junit-4.12.jar";
        FileUtils.mkdir(fullPath);
        Files.copy(FileUtils.file(jar.getFile()), FileUtils.file(jarLocation));

        URL url = JarUtilsTest.class.getResource("/space directory/junit-4.12.jar");
        Method method = JarUtils.class.getDeclaredMethod("parseArtifactIdFromJar", String.class);
        method.setAccessible(Boolean.TRUE);
        assertNull(method.invoke(JarUtils.class, url.getPath()));
    }

    @Test
    public void testParseArtifactIdFromJarInJarInJarMore() {
        URL jar = JarUtilsTest.class.getResource("/example-jarinjarinjar.jar");
        String artifactId0 = parseArtifactId(jar.getFile()
                                             + "!/BOOT-INF/lib/example-client-2.0.0.jar!/BOOT-INF/lib/sofa-ark-spring-guides-230525-SOFA.jar!/");
        assertEquals("sofa-ark-spring-guides", artifactId0);
        String artifactId1 = parseArtifactId(jar.getFile()
                                             + "!/BOOT-INF/lib/example-client-2.0.0.jar!/BOOT-INF/lib/example-client-3.0.0.jar!/");
        assertEquals("example-client", artifactId1);
    }
}
