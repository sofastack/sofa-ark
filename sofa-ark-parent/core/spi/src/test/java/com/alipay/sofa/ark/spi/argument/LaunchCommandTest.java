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
package com.alipay.sofa.ark.spi.argument;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author qilong.zql 18/3/9
 */
public class LaunchCommandTest {

    public static List<String> arkCommand = new ArrayList();
    public static int          count;

    String                     classpath;
    Method                     method;
    URL                        fatJarUrl;

    @Before
    public void init() {

        try {

            classpath = getClasspath(Objects.requireNonNull(getURLs(this.getClass()
                .getClassLoader())));
            method = MainClass.class.getMethod("main", String[].class);
            fatJarUrl = this.getClass().getClassLoader().getResource("test 2.jar");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        arkCommand.add(String.format("%s%s=%s", CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK,
            CommandArgument.FAT_JAR_ARGUMENT_KEY, fatJarUrl));
        arkCommand.add(String.format("%s%s=%s", CommandArgument.ARK_CONTAINER_ARGUMENTS_MARK,
            CommandArgument.CLASSPATH_ARGUMENT_KEY, classpath));
        arkCommand.add(String.format("%s%s=%s", CommandArgument.ARK_BIZ_ARGUMENTS_MARK,
            CommandArgument.ENTRY_CLASS_NAME_ARGUMENT_KEY, method.getDeclaringClass().getName()));
        arkCommand.add(String.format("%s%s=%s", CommandArgument.ARK_BIZ_ARGUMENTS_MARK,
            CommandArgument.ENTRY_METHOD_NAME_ARGUMENT_KEY, method.getName()));
        LaunchCommandTest.count = 0;
    }

    @Test
    public void testCommandParser() {
        try {
            List<String> args = new ArrayList<>();
            args.addAll(arkCommand);
            args.add("p1");
            args.add("p2");
            LaunchCommand launchCommand = LaunchCommand.parse(args.toArray(new String[] {}));

            Assert.assertTrue(launchCommand.getEntryClassName().equals(
                method.getDeclaringClass().getName()));
            Assert.assertTrue(launchCommand.getEntryMethodName().equals(method.getName()));
            Assert.assertTrue(launchCommand.getExecutableArkBizJar().equals(fatJarUrl));
            Assert.assertTrue(launchCommand.getClasspath().length == classpath
                .split(CommandArgument.CLASSPATH_SPLIT).length);
            for (URL url : launchCommand.getClasspath()) {
                Assert.assertTrue(classpath.contains(url.toExternalForm()));
            }
            Assert.assertTrue(2 == launchCommand.getLaunchArgs().length);
        } catch (Exception ex) {
            Assert.assertNull(ex);
        }
    }

    @Test
    public void testEncodedURL() {
        File file = new File(fatJarUrl.getFile());
        Assert.assertFalse(file.exists());
        file = new File(URLDecoder.decode(fatJarUrl.getFile()));
        Assert.assertTrue(file.exists());
    }

    public static class MainClass {

        public static void main(String[] args) {
            if (args.length > 0) {
                LaunchCommandTest.count += Integer.valueOf(args[0]);
            }
        }

    }

    private String getClasspath(URL[] urls) {

        StringBuilder sb = new StringBuilder();
        for (URL url : urls) {
            sb.append(url.toExternalForm()).append(CommandArgument.CLASSPATH_SPLIT);
        }

        return sb.toString();
    }

    private URL[] getURLs(ClassLoader classLoader) {
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
                url = new File(classpathEntry).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                throw new ArkRuntimeException("Failed to get urls from " + classLoader, e);
            }
            classpathURLs.add(url);
        }

        return classpathURLs.toArray(new URL[0]);

    }
}