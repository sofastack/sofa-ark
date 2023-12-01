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

import com.alipay.sofa.ark.exception.ArkRuntimeException;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassLoader Util
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ClassLoaderUtils {

    private static final String   JAVA_AGENT_MARK        = "-javaagent:";

    private static final String   JAVA_AGENT_OPTION_MARK = "=";

    private static final String   SKYWALKING_AGENT_JAR   = "skywalking-agent.jar";

    private static final String[] SKYWALKING_MOUNT_DIR   = { "plugins", "activations" };

    /**
     * push ContextClassLoader
     *
     * @param newClassLoader new classLoader
     * @return old classloader
     */
    public static ClassLoader pushContextClassLoader(ClassLoader newClassLoader) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(newClassLoader);
        return oldClassLoader;
    }

    /**
     * set ContextClassLoader back
     *
     * @param oldClassLoader old classLoader
     */
    public static void popContextClassLoader(ClassLoader oldClassLoader) {
        Thread.currentThread().setContextClassLoader(oldClassLoader);
    }

    public static URL[] getAgentClassPath() {
        List<String> inputArguments = AccessController
            .doPrivileged(new PrivilegedAction<List<String>>() {
                @Override
                public List<String> run() {
                    return ManagementFactory.getRuntimeMXBean().getInputArguments();
                }
            });

        List<URL> agentPaths = new ArrayList<>();
        for (String argument : inputArguments) {
            if (!argument.startsWith(JAVA_AGENT_MARK)) {
                continue;
            }
            argument = argument.substring(JAVA_AGENT_MARK.length());
            try {
                String path = argument.split(JAVA_AGENT_OPTION_MARK)[0];
                URL url = FileUtils.file(path).getCanonicalFile().toURI().toURL();
                agentPaths.add(url);
                processSkyWalking(path, agentPaths);
            } catch (Throwable e) {
                throw new ArkRuntimeException("Failed to create java agent classloader", e);
            }
        }
        return agentPaths.toArray(new URL[] {});

    }

    /**
     * process skywalking agent plugins/activations
     * @param path
     * @param agentPaths
     * @throws MalformedURLException
     */
    public static void processSkyWalking(final String path, final List<URL> agentPaths) throws MalformedURLException, IOException {
        if (path.contains(SKYWALKING_AGENT_JAR)) {
            for (String mountFolder : SKYWALKING_MOUNT_DIR) {
                File folder = new File(FileUtils.file(path).getCanonicalFile().getParentFile(), mountFolder);
                if (folder.exists() && folder.isDirectory()) {
                    String[] jarFileNames = folder.list((dir, name) -> name.endsWith(".jar"));
                    for (String fileName: jarFileNames) {
                        File jarFile = new File(folder, fileName);
                        agentPaths.add(jarFile.toURI().toURL());
                    }
                }
            }
        }
    }

    @SuppressWarnings({ "restriction", "unchecked" })
    public static URL[] getURLs(ClassLoader classLoader) {
        // https://stackoverflow.com/questions/46519092/how-to-get-all-jars-loaded-by-a-java-application-in-java9
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        }

        // support jdk9+
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(System.getProperty("path.separator"));
        List<URL> classpathURLs = new ArrayList<>();
        for (String classpathEntry : classpathEntries) {
            URL url = null;
            try {
                url = FileUtils.file(classpathEntry).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new ArkRuntimeException("Failed to get urls from " + classLoader, e);
            }
            classpathURLs.add(url);
        }

        return classpathURLs.toArray(new URL[0]);
    }

}