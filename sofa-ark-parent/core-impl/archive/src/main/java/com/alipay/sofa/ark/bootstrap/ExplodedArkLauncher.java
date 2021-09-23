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
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * {@link AbstractLauncher} for JAR based archives. This launcher assumes that dependency jars are
 * included inside a {@code /SOFA-ARK/container/lib} directory and that application fat jar
 * are included inside a {@code /SOFA-ARK/biz/} directory and that ark plugin fat jar are included
 * inside a {@code /SOFA-ARK/plugin/} directory
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class ExplodedArkLauncher extends ArkLauncher {
    public final String SOFA_ARK_MAIN = "com.alipay.sofa.ark.container.ArkContainer";

    public static void main(String[] args) throws Exception {
        initEnv();
        new ExplodedArkLauncher().launch(args);
    }

    public static void initEnv() throws IOException {
        String workingDirectory = System.getProperty("working_directory",
            new File("").getAbsolutePath());
        if (workingDirectory.endsWith("/")) {
            workingDirectory = workingDirectory.substring(0, workingDirectory.length() - 1);
        }
        String explodedDirectory = workingDirectory + "-exploded";
        System.setProperty(Constants.ENABLE_EXPLODED, "true");
        System.setProperty("runtime_path", explodedDirectory);
        copyConf(workingDirectory, explodedDirectory);
        unzip(workingDirectory, explodedDirectory);
    }

    protected static void copyConf(String workingDirectory, String explodedDirectory)
                                                                                     throws IOException {
        File file = new File(workingDirectory + "/" + Constants.ARK_CONF_BASE_DIR + "/"
                             + Constants.ARK_CONF_FILE);
        if (!file.exists()) {
            return;
        }
        File target = new File(explodedDirectory + "/" + Constants.ARK_CONF_BASE_DIR + "/"
                               + Constants.ARK_CONF_FILE);
        if (target.exists()) {
            target.delete();
        }
        org.apache.commons.io.FileUtils.copyFile(file, target);
    }

    private static void unzip(String workingDirectory, String explodedDirectory) throws IOException {
        System.setProperty("enable_exploded", "true");
        File file = new File(workingDirectory + "/SOFA-ARK");
        File exploded = new File(explodedDirectory + "/SOFA-ARK");
        if (!exploded.exists()) {
            File[] subDirs = file.listFiles();
            for (File subDir : subDirs) {
                File[] fatJarFiles = subDir.listFiles(f -> f.getName().contains(".jar"));
                for (File fatJarFile : fatJarFiles) {
                    String targetDir = fatJarFile.getAbsolutePath().replace(workingDirectory, explodedDirectory).replace(".jar", "");
                    FileUtils.unzip(fatJarFile, targetDir);
                    System.out.println("unzip " + fatJarFile.getAbsolutePath() + " to " + targetDir);
                }
            }
        }
    }

    public ExplodedArkLauncher() {
    }

    public ExplodedArkLauncher(ExecutableArchive executableArchive) {
        super(executableArchive);
    }

    @Override
    protected String getMainClass() {
        return SOFA_ARK_MAIN;
    }
}