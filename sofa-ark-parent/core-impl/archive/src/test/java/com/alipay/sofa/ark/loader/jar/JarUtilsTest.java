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
package com.alipay.sofa.ark.loader.jar;

import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import static com.alipay.sofa.ark.loader.jar.JarUtils.getArtifactIdFromLocalClassPath;
import static com.alipay.sofa.ark.loader.jar.JarUtils.searchPomProperties;
import static org.junit.Assert.*;

public class JarUtilsTest {

    @Test
    public void testSearchPomProperties() {

        assertNull(searchPomProperties(null));
        assertNull(searchPomProperties(new File("/not-exists")));

        URL url = this.getClass().getClassLoader().getResource("pom-properties/pom.properties");
        File file = new File(url.getPath());
        assertEquals(file, searchPomProperties(file));

        url = this.getClass().getClassLoader().getResource("./");
        file = new File(url.getPath());
        assertTrue(searchPomProperties(file).getPath().endsWith("pom.properties"));
    }

    @Test
    public void testGetArtifactIdFromLocalClassPath() {
        assertNull(getArtifactIdFromLocalClassPath("/a/target/bbb"));
        URL url = this.getClass().getClassLoader().getResource("");
        assertEquals("sofa-ark-archive", getArtifactIdFromLocalClassPath(url.getPath()));
    }

    @Test
    public void testParseArtifactId() {
        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        String artifactId = JarUtils.parseArtifactId(url.getPath());
        assertEquals("sofa-ark-sample-springboot-ark", artifactId);
    }

    @Test
    public void testParseArtifactId2() {
        URL url = this.getClass().getClassLoader().getResource("xxxxx.jar-unpack");
        String artifactId = JarUtils.parseArtifactId(url.getPath());
        assertEquals("xxxx-test", artifactId);
    }

    @Test
    public void testParseArtifactIdFromUnpackDirNameFallback() {
        File tempRoot = com.alipay.sofa.ark.common.util.FileUtils
            .createTempDir("test-unpack-fallback");
        try {
            File unpackDir = new File(tempRoot, "demo-service-1.0.0.jar-unpack");
            assertTrue(unpackDir.mkdirs());

            String artifactId = JarUtils.parseArtifactId(unpackDir.getAbsolutePath());
            assertEquals("demo-service", artifactId);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempRoot);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackDirNameFallbackWhenArtifactIdMissing()
                                                                                   throws IOException {
        File tempRoot = com.alipay.sofa.ark.common.util.FileUtils
            .createTempDir("test-unpack-fallback-missing-artifact-id");
        try {
            File unpackDir = new File(tempRoot, "demo-service-1.0.0.jar-unpack");
            File mavenArchiverDir = new File(unpackDir, "META-INF/maven-archiver");
            assertTrue(mavenArchiverDir.mkdirs());

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("groupId", "com.test");
                props.setProperty("version", "1.0.0");
                props.store(writer, "Test pom.properties file without artifactId");
            }

            String artifactId = JarUtils.parseArtifactId(unpackDir.getAbsolutePath());
            assertEquals("demo-service", artifactId);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempRoot);
        }
    }

}
