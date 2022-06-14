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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION;

public class JarUtils {
    private static final String              JAR_POM_PROPERTIES = "pom.properties";
    private static final String              JAR_VERSION        = "version";

    private static final Map<String, String> jarVersionMap      = new HashMap<>();

    public static String getJarVersion(JarFile jarFile) throws IOException {
        return jarVersionMap.computeIfAbsent(jarFile.getName(), k -> {
            try {
                return doGetJarVersion(jarFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String doGetJarVersion(JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();
        Attributes attributes = manifest.getMainAttributes();
        String version = attributes.getValue(IMPLEMENTATION_VERSION);
        if (!StringUtils.isEmpty(version)) {
            return version;
        }
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(JAR_POM_PROPERTIES)) {
                InputStream is = jarFile.getInputStream(entry);
                Properties p = new Properties();
                p.load(is);
                return p.getProperty(JAR_VERSION);
            }
        }
        return null;
    }
}
