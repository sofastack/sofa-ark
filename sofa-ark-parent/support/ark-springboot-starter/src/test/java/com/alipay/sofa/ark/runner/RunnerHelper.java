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
package com.alipay.sofa.ark.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class RunnerHelper {

    public static AtomicBoolean isHelped = new AtomicBoolean(false);

    public static void prepare() {
        try {
            if (isHelped.compareAndSet(false, true)) {
                String shPath = RunnerHelper.class.getClassLoader()
                    .getResource("prepareForTest.sh").getPath();
                String workDir = System.getProperty("user.dir");
                int index = workDir.indexOf("sofa-ark-parent");
                workDir = (index == -1) ? workDir : workDir.substring(0, index);
                String assemblyDir = workDir + File.separator + "sofa-ark-parent/assembly";
                String command = String.format("sh %s %s %s", shPath, assemblyDir, workDir);
                Process process = Runtime.getRuntime().exec(command);
                fixNoResponse(process);
                if (process.waitFor() != 0) {
                    throw new RuntimeException(" shell script: 'mvn package' failed to execute!");
                }
                addArkContainerURL(String.format("%s%starget%ssofa-ark-all.jar", assemblyDir,
                    File.separator, File.separator));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void fixNoResponse(Process process) throws IOException {
        BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        while ((line = stdout.readLine()) != null) {
            // do nothing
        }
        while ((line = stderr.readLine()) != null) {
            // do nothing
        }
    }

    /**
     * Add URL of sofa-ark-all.jar to classpath
     *
     * @param arkContainerPath
     */
    private static void addArkContainerURL(String arkContainerPath) {
        try {
            URLClassLoader urlClassLoader = (URLClassLoader) RunnerHelper.class.getClassLoader();
            Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            addURL.setAccessible(true);
            addURL.invoke(urlClassLoader, new File(arkContainerPath).toURI().toURL());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}