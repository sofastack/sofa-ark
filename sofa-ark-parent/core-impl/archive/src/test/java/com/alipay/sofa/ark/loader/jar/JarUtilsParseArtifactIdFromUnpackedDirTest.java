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

import com.alipay.sofa.ark.common.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class JarUtilsParseArtifactIdFromUnpackedDirTest {

    @Test
    public void testParseArtifactIdFromUnpackedDir_StandardLocation() throws IOException {
        File tempDir = FileUtils.createTempDir("test-unpack");
        try {
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", "test-artifact");
                props.store(writer, "Test pom.properties file");
            }

            String result = JarUtils.parseArtifactIdFromUnpackedDir(tempDir.getAbsolutePath());
            assertEquals("test-artifact", result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_NonStandardLocation() throws IOException {
        File tempDir = FileUtils.createTempDir("test-unpack-nonstandard");
        try {
            File nestedDir = new File(tempDir, "some/nested/path");
            nestedDir.mkdirs();

            File pomPropertiesFile = new File(nestedDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", "test-artifact-nested");
                props.store(writer, "Test pom.properties file in nested location");
            }

            String result = JarUtils.parseArtifactIdFromUnpackedDir(tempDir.getAbsolutePath());
            assertEquals("test-artifact-nested", result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_DirectoryDoesNotExist() {
        String result = JarUtils.parseArtifactIdFromUnpackedDir("/non/existent/directory");
        assertNull(result);
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_EmptyPomProperties() throws IOException {
        File tempDir = FileUtils.createTempDir("test-unpack-empty");
        try {
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", "");
                props.store(writer, "Test pom.properties file with empty artifactId");
            }

            String result = JarUtils.parseArtifactIdFromUnpackedDir(tempDir.getAbsolutePath());
            assertNull(result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_MissingArtifactIdProperty() throws IOException {
        File tempDir = FileUtils.createTempDir("test-unpack-missing-key");
        try {
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("groupId", "com.test");
                props.setProperty("version", "1.0.0");
                props.store(writer, "Test pom.properties file without artifactId");
            }

            String result = JarUtils.parseArtifactIdFromUnpackedDir(tempDir.getAbsolutePath());
            assertNull(result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_NoPomProperties() throws IOException {
        File tempDir = FileUtils.createTempDir("test-unpack-nopom");
        try {
            // Directory exists but has no pom.properties at all
            String result = JarUtils.parseArtifactIdFromUnpackedDir(tempDir.getAbsolutePath());
            assertNull(result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactId_UnpackPathFallbackToFileName() throws IOException {
        // When pom.properties is not found, should fallback to parsing from directory name
        String unpackPath = "/tmp/non-existent-dir/my-app-1.0.0-SNAPSHOT.jar-unpack/";
        String artifactId = JarUtils.parseArtifactId(unpackPath);
        assertEquals("my-app", artifactId);
    }

    @Test
    public void testParseArtifactId_UnpackPathWithResourceUrl() {
        // Test with actual test resource
        java.net.URL url = this.getClass().getClassLoader().getResource("xxxxx.jar-unpack");
        assertNotNull(url);
        String artifactId = JarUtils.parseArtifactId(url.getPath());
        assertEquals("xxxx-test", artifactId);
    }

    @Test
    public void testParseArtifactId_UnpackPathNoVersion() {
        // When directory name has no version pattern, should return null from fallback
        String unpackPath = "/tmp/non-existent-dir/some-lib.jar-unpack/";
        String artifactId = JarUtils.parseArtifactId(unpackPath);
        assertNull(artifactId);
    }
}
