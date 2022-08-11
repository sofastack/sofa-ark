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
package com.alipay.sofa.ark.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class JarUtils {
    private static final String CLASSPATH_ROOT_IDENTITY          = "/target/classes/";

    private static final String TEST_CLASSPATH_ROOT_IDENTITY     = "/target/test-classes/";
    private static final String TARGET_ROOT_IDENTITY             = "/target/";

    private static final String JAR_POM_PROPERTIES_RELATIVE_PATH = "maven-archiver/pom.properties";
    private static final String JAR_ARTIFACT_ID                  = "artifactId";

    public static String getArtifactIdFromClassPath(String fileClassPath) throws IOException {
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
        }
    }
}