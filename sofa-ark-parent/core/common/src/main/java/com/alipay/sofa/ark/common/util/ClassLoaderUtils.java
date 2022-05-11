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
import sun.misc.Unsafe;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
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

    private static final String JAVA_AGENT_MARK        = "-javaagent:";

    private static final String JAVA_AGENT_OPTION_MARK = "=";

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
                URL url = new File(path).toURI().toURL();
                agentPaths.add(url);
            } catch (Throwable e) {
                throw new ArkRuntimeException("Failed to create java agent classloader", e);
            }
        }
        return agentPaths.toArray(new URL[] {});
    }

    @SuppressWarnings({ "restriction", "unchecked" })
    public static URL[] getURLs(ClassLoader classLoader) {
        // https://stackoverflow.com/questions/46519092/how-to-get-all-jars-loaded-by-a-java-application-in-java9
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        }

        // support jdk9+

        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Unsafe unsafe = (Unsafe) field.get(null);

            // jdk.internal.loader.ClassLoaders.AppClassLoader.ucp
            Field ucpField = classLoader.getClass().getDeclaredField("ucp");
            long ucpFieldOffset = unsafe.objectFieldOffset(ucpField);
            Object ucpObject = unsafe.getObject(classLoader, ucpFieldOffset);

            // jdk.internal.loader.URLClassPath.path
            Field pathField = ucpField.getType().getDeclaredField("path");
            long pathFieldOffset = unsafe.objectFieldOffset(pathField);
            ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucpObject, pathFieldOffset);

            return path.toArray(new URL[path.size()]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}