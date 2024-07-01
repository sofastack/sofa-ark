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
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.util.ModifyPathUtils;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.common.utils.StringUtil;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;

public class JarUtils {
    private static final String                        CLASSPATH_ROOT_IDENTITY          = "/target/classes";

    private static final String                        TEST_CLASSPATH_ROOT_IDENTITY     = "/target/test-classes";
    private static final String                        TARGET_ROOT_IDENTITY             = "/target/";

    private static final String                        JAR_POM_PROPERTIES_RELATIVE_PATH = "maven-archiver/pom.properties";
    private static final String                        JAR_ARTIFACT_ID                  = "artifactId";

    private static final String                        JAR_POM_PROPERTIES               = "pom.properties";

    private static final String                        POM_FILE                         = "/pom.xml";

    private static final String                        VERSION_REGEX                    = "^([0-9]+\\.)+.+";

    private static final MavenXpp3Reader               READER                           = new MavenXpp3Reader();

    public static final String                         JAR_SEPARATOR                    = "!/";

    public static final String                         JAR_SUFFIX                       = ".jar";

    private static final Map<String, Optional<String>> artifactIdCacheMap               = new ConcurrentHashMap<>();

    static File searchPomProperties(File dirOrFile) {
        if (dirOrFile == null || !dirOrFile.exists()) {
            return null;
        }
        if (dirOrFile.isFile() && JAR_POM_PROPERTIES.equals(dirOrFile.getName())) {
            return dirOrFile;
        }
        if (dirOrFile.isDirectory()) {
            File[] files = dirOrFile.listFiles();

            if (files != null) {
                for (File file : files) {
                    File result = searchPomProperties(file);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }
        return null;
    }

    static String getArtifactIdFromLocalClassPath(String fileClassPath) {

        String libraryFile = fileClassPath.replace("file:", "");
        // 1. search pom.properties
        int classesRootIndex = libraryFile.endsWith(CLASSPATH_ROOT_IDENTITY) ? libraryFile
            .indexOf(CLASSPATH_ROOT_IDENTITY) : libraryFile.indexOf(CLASSPATH_ROOT_IDENTITY + "/");
        int testClassesRootIndex = libraryFile.endsWith(TEST_CLASSPATH_ROOT_IDENTITY) ? libraryFile
            .indexOf(TEST_CLASSPATH_ROOT_IDENTITY) : libraryFile
            .indexOf(TEST_CLASSPATH_ROOT_IDENTITY + "/");
        String pomPropertiesPath;
        String pomXmlPath = null;
        if (classesRootIndex != -1) {
            pomPropertiesPath = libraryFile.substring(0,
                classesRootIndex + TARGET_ROOT_IDENTITY.length())
                                + JAR_POM_PROPERTIES_RELATIVE_PATH;
            pomXmlPath = libraryFile.substring(0, classesRootIndex) + POM_FILE;
        } else if (testClassesRootIndex != -1) {
            pomPropertiesPath = libraryFile.substring(0, testClassesRootIndex
                                                         + TARGET_ROOT_IDENTITY.length())
                                + JAR_POM_PROPERTIES_RELATIVE_PATH;
            pomXmlPath = libraryFile.substring(0, testClassesRootIndex) + POM_FILE;
        } else {
            // is not from test classpath, for example install uncompressed modules, just return null
            // search for pom.properties
            File pomPropertiesFile = searchPomProperties(FileUtils.file(libraryFile));
            if (pomPropertiesFile != null && pomPropertiesFile.exists()) {
                pomPropertiesPath = pomPropertiesFile.getAbsolutePath();
            } else {
                // not found pom.properties
                pomPropertiesPath = null;
            }
        }

        String artifactId = null;
        if (!StringUtils.isEmpty(pomPropertiesPath)) {
            try (InputStream inputStream = Files.newInputStream(FileUtils.file(pomPropertiesPath)
                .toPath())) {
                Properties properties = new Properties();
                properties.load(inputStream);
                artifactId = properties.getProperty(JAR_ARTIFACT_ID);
            } catch (IOException e) {
                // ignore
            }
        }

        if (StringUtils.isEmpty(artifactId) && !StringUtils.isEmpty(pomXmlPath)) {
            try (FileReader fileReader = new FileReader(pomXmlPath)) {
                Model model = READER.read(fileReader);
                return model.getArtifactId();
            } catch (Exception e) {
                // ignore
            }
        }
        return artifactId;
    }

    public static String parseArtifactId(String jarLocation) {
        // 1. /xxx/xxx/xx.jar!/
        // 2. /xxx/xxx/xx.jar!/xxxx.class
        // 3. /xxx/xxx/xx.jar
        // 4. /xxx/xxx/xxx-bootstrap-1.0.0-ark-biz.jar!/BOOT-INF/lib/spring-boot-2.4.13.jar!/
        // 5. /xxx/xxx-bootstrap-1.0.0-ark-biz.jar!/BOOT-INF/lib/sofa-ark-springboot-starter-2.1.1.jar!/META-INF/spring.factories
        // 6. /xxx/xxx/target/classes/xxxx.jar
        // 7. /xxx/xxx/target/test-classes/yyy/yyy/
        // 8. /xxx/xxx/xxx-starter-1.0.0-SNAPSHOT.jar!/BOOT-INF/lib/xxx2-starter-1.1.4-SNAPSHOT-ark-biz.jar!/lib/xxx3-230605-sofa.jar!/
        // 9. if is ark plugin, then return null to set declared default

        // clean the jar location prefix and suffix
        if (jarLocation.contains(JAR_SUFFIX)) {
            jarLocation = jarLocation.substring(0, jarLocation.lastIndexOf(JAR_SUFFIX) + JAR_SUFFIX.length());
        }
        if (jarLocation.startsWith("file:")) {
            jarLocation = jarLocation.substring("file:".length());
        }

        // modify the path to suit WindowsOS
        jarLocation = ModifyPathUtils.modifyPath(jarLocation);
        String finalJarLocation = jarLocation;
        artifactIdCacheMap.computeIfAbsent(jarLocation, a -> {
            try {
                String artifactId;
                String[] as = a.split(JAR_SEPARATOR, -1);
                if (as.length == 1) {
                    // no '!/'
                    if (a.endsWith(".jar")) {
                        artifactId = parseArtifactIdFromJar(a);
                        if (StringUtils.isEmpty(artifactId)) {
                            artifactId = doGetArtifactIdFromFileName(a);
                        }
                    } else {
                        artifactId = getArtifactIdFromLocalClassPath(a);
                    }
                } else {
                    // contains one '!/' or more
                    artifactId = parseArtifactIdFromJar(a);
                    if (StringUtils.isEmpty(artifactId)) {
                        artifactId = doGetArtifactIdFromFileName(a);
                    }
                }
                return Optional.ofNullable(artifactId);
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to parse artifact id from jar %s.",
                        finalJarLocation), e);
            }

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

    private static String parseArtifactIdFromJar(String jarLocation) throws IOException {
        try (com.alipay.sofa.ark.loader.jar.JarFile jarFile = getNestedRootJarFromJarLocation(jarLocation)) {
            JarFileArchive jarFileArchive = new JarFileArchive(jarFile);
            return jarFileArchive.getPomProperties().getProperty(JAR_ARTIFACT_ID);
        }
    }

    public static com.alipay.sofa.ark.loader.jar.JarFile getNestedRootJarFromJarLocation(String jarLocation)
                                                                                                            throws IOException {
        //  /xxx/xxx/xxx-starter-1.0.0-SNAPSHOT.jar!/BOOT-INF/lib/xxx2-starter-1.1.4-SNAPSHOT-ark-biz.jar!/lib/xxx3-230605-sofa.jar
        String[] js = jarLocation.split(JAR_SEPARATOR, -1);
        com.alipay.sofa.ark.loader.jar.JarFile rJarFile = new com.alipay.sofa.ark.loader.jar.JarFile(
            FileUtils.file(js[0]));
        for (int i = 1; i < js.length; i++) {
            String jPath = js[i];
            if (StringUtils.isEmpty(jPath) || !jPath.endsWith(".jar")) {
                break;
            }
            try {
                JarEntry jarEntry = rJarFile.getJarEntry(jPath);
                rJarFile = rJarFile.getNestedJarFile(jarEntry);
            } catch (NullPointerException e) {
                throw new IOException(
                    String.format("Failed to parse artifact id, jPath: %s", jPath), e);
            }
        }
        return rJarFile;
    }
}
