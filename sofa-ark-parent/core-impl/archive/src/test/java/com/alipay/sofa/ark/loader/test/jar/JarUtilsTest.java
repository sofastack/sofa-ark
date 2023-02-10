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

import com.alipay.sofa.ark.loader.jar.JarUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

public class JarUtilsTest {

    @Test
    public void getArtifactIdFromTestClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("sample-biz-withjar.jar");
        String artifactId = JarUtils.getArtifactIdFromLocalClassPath(url.getPath());
        Assert.assertEquals("sofa-ark-archive", artifactId);
    }

    @Test
    public void getArtifactIdFromClassPath() throws IOException {
        URL clazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/loader/jar/JarUtils.class");

        String artifactId = JarUtils.getArtifactIdFromLocalClassPath(clazzURL.getPath());
        Assert.assertEquals("sofa-ark-archive", artifactId);
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
    public void testParseArtifactIdFromJarPom() {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        String artifactId0 = JarUtils.getJarArtifactId(jar.getFile());
        Assert.assertEquals("sofa-ark-sample-springboot-ark", artifactId0);
    }

    @Test
    public void testParseArtifactIdFromJarInJarPom() {
        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");
        String artifactId0 = JarUtils
            .getJarArtifactId(jar.getFile() + "!/lib/slf4j-api-1.7.30.jar");
        Assert.assertEquals("slf4j-api", artifactId0);
    }

    @Test
    public void testParseArtifactIdFromJarPoms() throws Exception {
        Method method = JarUtils.class.getDeclaredMethod("doGetArtifactIdFromJarPom", String.class);
        method.setAccessible(Boolean.TRUE);

        URL jar = JarUtilsTest.class.getResource("/sample-biz-withjar.jar");

        String jarPath = jar.getFile();
        String nestedJarPath = jar.getFile() + "!/lib/slf4j-api-1.7.30.jar";

        String artifactId0 = (String) method.invoke(JarUtils.class, jarPath);
        Assert.assertEquals("sofa-ark-sample-springboot-ark", artifactId0);

        String artifactId1 = (String) method.invoke(JarUtils.class, nestedJarPath);
        Assert.assertEquals("slf4j-api", artifactId1);
    }
}