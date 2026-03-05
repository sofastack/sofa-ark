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
import java.util.Properties;

import static org.junit.Assert.*;

public class JarUtilsParseArtifactIdFromUnpackedDirTest {

    @Test
    public void testParseArtifactIdFromUnpackedDir_StandardLocation() throws IOException {
        // Create a temporary directory to simulate unpacked jar
        File tempDir = com.alipay.sofa.ark.common.util.FileUtils.createTempDir("test-unpack");
        try {
            // Create the standard maven-archiver/pom.properties location
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", "test-artifact");
                props.store(writer, "Test pom.properties file");
            }

            // Test the method
            String result = JarUtilsTestHelper.parseArtifactIdFromUnpackedDir(tempDir
                .getAbsolutePath());
            assertEquals("test-artifact", result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_NonStandardLocation() throws IOException {
        // Create a temporary directory to simulate unpacked jar
        File tempDir = com.alipay.sofa.ark.common.util.FileUtils
            .createTempDir("test-unpack-nonstandard");
        try {
            // Create pom.properties in a nested directory (non-standard location)
            File nestedDir = new File(tempDir, "some/nested/path");
            nestedDir.mkdirs();

            File pomPropertiesFile = new File(nestedDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", "test-artifact-nested");
                props.store(writer, "Test pom.properties file in nested location");
            }

            // Test the method - should find the pom.properties via recursive search
            String result = JarUtilsTestHelper.parseArtifactIdFromUnpackedDir(tempDir
                .getAbsolutePath());
            assertEquals("test-artifact-nested", result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_DirectoryDoesNotExist() {
        // Test with non-existent directory
        String result = JarUtilsTestHelper
            .parseArtifactIdFromUnpackedDir("/non/existent/directory");
        assertNull(result);
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_EmptyPomProperties() throws IOException {
        // Create a temporary directory with empty pom.properties
        File tempDir = com.alipay.sofa.ark.common.util.FileUtils.createTempDir("test-unpack-empty");
        try {
            // Create the standard maven-archiver/pom.properties location with empty artifactId
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("artifactId", ""); // Empty artifactId
                props.store(writer, "Test pom.properties file with empty artifactId");
            }

            // Test the method - should return null for empty artifactId
            String result = JarUtilsTestHelper.parseArtifactIdFromUnpackedDir(tempDir
                .getAbsolutePath());
            assertNull(result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testParseArtifactIdFromUnpackedDir_MissingArtifactIdProperty() throws IOException {
        // Create a temporary directory with pom.properties that doesn't have artifactId
        File tempDir = com.alipay.sofa.ark.common.util.FileUtils
            .createTempDir("test-unpack-missing-key");
        try {
            // Create the standard maven-archiver/pom.properties location without artifactId
            File mavenArchiverDir = new File(tempDir, "META-INF/maven-archiver");
            mavenArchiverDir.mkdirs();

            File pomPropertiesFile = new File(mavenArchiverDir, "pom.properties");
            try (FileWriter writer = new FileWriter(pomPropertiesFile)) {
                Properties props = new Properties();
                props.setProperty("groupId", "com.test"); // Different property
                props.setProperty("version", "1.0.0");
                props.store(writer, "Test pom.properties file without artifactId");
            }

            // Test the method - should return null when artifactId property is missing
            String result = JarUtilsTestHelper.parseArtifactIdFromUnpackedDir(tempDir
                .getAbsolutePath());
            assertNull(result);
        } finally {
            org.apache.commons.io.FileUtils.deleteQuietly(tempDir);
        }
    }
}