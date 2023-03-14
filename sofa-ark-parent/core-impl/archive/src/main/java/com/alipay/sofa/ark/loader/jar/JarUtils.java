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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtils {
    private static final String                        CLASSPATH_ROOT_IDENTITY          = "/target/classes/";

    private static final String                        TEST_CLASSPATH_ROOT_IDENTITY     = "/target/test-classes/";
    private static final String                        TARGET_ROOT_IDENTITY             = "/target/";

    private static final String                        JAR_POM_PROPERTIES_RELATIVE_PATH = "maven-archiver/pom.properties";
    private static final String                        JAR_ARTIFACT_ID                  = "artifactId";

    private static final String                        JAR_POM_PROPERTIES               = "pom.properties";

    private static final String                        VERSION_REGEX                    = "^([0-9]+\\.)+.+";

    private static final Map<String, Optional<String>> artifactIdCacheMap               = new ConcurrentHashMap<>();

    public static String getArtifactIdFromLocalClassPath(String fileClassPath) {
        // file:/Users/youji.zzl/Documents/workspace/iexpprodbase/app/bootstrap/target/classes/spring/
        String libraryFile = fileClassPath.replace("file:", "");
        // 1. search pom.properties
        int classesRootIndex = libraryFile.indexOf(CLASSPATH_ROOT_IDENTITY);
        int testClassesRootIndex = libraryFile.indexOf(TEST_CLASSPATH_ROOT_IDENTITY);
        int rootIndex = -1;
        if (classesRootIndex != -1) {
            rootIndex = classesRootIndex;
            libraryFile.substring(0, classesRootIndex + TARGET_ROOT_IDENTITY.length());
        } else if (testClassesRootIndex != -1) {
            rootIndex = testClassesRootIndex;
        } else {
            return null;
        }
        String classPathRootPath = libraryFile.substring(0,
            rootIndex + TARGET_ROOT_IDENTITY.length());
        String pomPropertiesPath = classPathRootPath + JAR_POM_PROPERTIES_RELATIVE_PATH;

        try (InputStream inputStream = Files.newInputStream(Paths.get(pomPropertiesPath))) {
            Properties properties = new Properties();
            properties.load(inputStream);
            return properties.getProperty(JAR_ARTIFACT_ID);
        } catch (IOException e) {
            return null;
        }
    }

    public static String getJarArtifactId(String jarLocation) {
        artifactIdCacheMap.computeIfAbsent(jarLocation, a -> {
            String artifactId = doGetArtifactIdFromFileName(a);
            if (StringUtils.isEmpty(artifactId)) {
                artifactId = doGetArtifactIdFromJarPom(a);
            }
            return Optional.ofNullable(artifactId);
        });
        return artifactIdCacheMap.get(jarLocation).orElse(null);
    }

    private static String doGetArtifactIdFromFileName(String jarLocation) {
        String[] jarInfos = jarLocation.split("/");
        if (jarInfos.length == 0) {
            return null;
        }
        String artifactVersion = jarInfos[jarInfos.length - 1];
        String[] artifactVersionInfos = artifactVersion.split("-");
        List<String> artifactInfos = new ArrayList<>();
        boolean getVersion = false;
        for (String info : artifactVersionInfos) {
            if (!StringUtils.isEmpty(info) && info.matches(VERSION_REGEX)) {
                getVersion = true;
                break;
            }
            artifactInfos.add(info);
        }
        if (getVersion) {
            return String.join("-", artifactInfos);
        }
        // if can't find any version from jar name, then we just return null to paas the declared check
        return null;
    }

    private static String doGetArtifactIdFromJarPom(String jarLocation) {
        try {
            if (jarLocation.contains("!/")) {
                // in nested jar
                return parseArtifactIdFromJarInJar(jarLocation);
            } else {
                try (JarFile jarFile = new JarFile(jarLocation)) {
                    return parseArtifactIdFromJar(jarFile);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to parse artifact id from jar %s.",
                jarLocation), e);
        }
    }

    private static String parseArtifactIdFromJarInJar(String jarLocation) throws IOException {
        String rootPath = jarLocation.substring(0, jarLocation.lastIndexOf("!/"));
        String subNestedPath =  jarLocation.substring(jarLocation.lastIndexOf("!/") + 2);
        com.alipay.sofa.ark.loader.jar.JarFile jarFile = new com.alipay.sofa.ark.loader.jar.JarFile(new File(rootPath));
        JarFileArchive jarFileArchive = new JarFileArchive(jarFile);
        List<Archive> archives = jarFileArchive.getNestedArchives(entry -> !StringUtils.isEmpty(entry.getName()) && entry.getName().equals(subNestedPath));

        if (archives.size() != 1) {
            return null;
        }
        Archive archive = archives.get(0);
        Properties properties = ((JarFileArchive) archive).getPomProperties();
        return properties.getProperty(JAR_ARTIFACT_ID);
    }

    private static String parseArtifactIdFromJar(JarFile jarFile) throws IOException {
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(JAR_POM_PROPERTIES)) {
                try (InputStream is = jarFile.getInputStream(entry)) {
                    Properties p = new Properties();
                    p.load(is);
                    return p.getProperty(JAR_ARTIFACT_ID);
                }
            }
        }
        return null;
    }
}