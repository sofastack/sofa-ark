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
package com.alipay.sofa.ark.dynamic.launcher;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import com.alipay.sofa.ark.dynamic.util.CommonUtils;
import com.alipay.sofa.ark.dynamic.util.FileUtil;
import com.alipay.sofa.ark.dynamic.util.JarUtils;
import com.alipay.sofa.ark.dynamic.util.PropertiesUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.CollectionUtils;
import org.testng.util.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants.WORKSPACE;

/**
 * The type Prepare launcher.
 *
 * @author hanyue
 * @version : PrepareLauncher.java, v 0.1 2022年05月28日 上午6:09 hanyue Exp $
 */
public class PrepareLauncher {

    private static final ArkLogger LOGGER                   = ArkLoggerFactory.getDefaultLogger();

    private static final String    SOURCE_BIZ_PACKAGE_SH    = "/bin/biz_package.sh";
    public static final String     TARGET_BIZ_PACKAGE_SH    = WORKSPACE + "/biz_package.sh";

    private static final String    SOURCE_MASTER_PACKAGE_SH = "/bin/master_package.sh";
    public static final String     TARGET_MASTER_PACKAGE_SH = WORKSPACE + "/master_package.sh";

    private static final String    MASTER_BIZ_DIR           = "masterbiz";
    public static final String     DEFAULT_MASTERT_BIZ_NAME = "testBase-ark-biz.jar";

    private static final String    BIZ_DIR                  = "target";
    public static final String     DEFAULT_BIZ_NAME         = "testBiz-ark-biz.jar";

    /**
     * Check.
     *
     * @throws Exception the exception
     */
    public static void check() throws Exception {
        // Make sure the script can be executed
        checkAndCopySh();

        // Make sure masterFatJar is available before startup
        checkMasterFatJAr();

        // Make sure bizFatJar is available before startup
        checkBizFatJar();
    }

    /**
     * Check and copy sh.
     *
     * @throws IOException the io exception
     */
    public static void checkAndCopySh() throws IOException {
        InputStream masterfatJar_inputStream = PrepareLauncher.class.getResource(SOURCE_MASTER_PACKAGE_SH).openStream();
        OutputStream masterfatJar_outputStream = new FileOutputStream(TARGET_MASTER_PACKAGE_SH);
        IOUtils.copy(masterfatJar_inputStream, masterfatJar_outputStream);

        InputStream recompile_inputStream = PrepareLauncher.class.getResource(SOURCE_BIZ_PACKAGE_SH).openStream();
        OutputStream recompile_outputStream = new FileOutputStream(TARGET_BIZ_PACKAGE_SH);
        IOUtils.copy(recompile_inputStream, recompile_outputStream);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            new File(TARGET_MASTER_PACKAGE_SH).deleteOnExit();
            new File(TARGET_BIZ_PACKAGE_SH).deleteOnExit();
        }));
    }

    /**
     * Check master fat j ar.
     *
     * @throws Exception the exception
     */
    public static void checkMasterFatJAr() throws Exception {
        String projectBaseDir = CommonUtils.getProjectBaseDir();

        File masterBizDir = new File(projectBaseDir, MASTER_BIZ_DIR);
        if (!masterBizDir.exists()) {
            masterBizDir.mkdirs();
        }

        // find local
        List<String> allArkJars = JarUtils.findArkJars(masterBizDir, JarUtils.masterJarSuffix());
        if (CollectionUtils.isEmpty(allArkJars)) {
            // acquired config
            String config = PropertiesUtils.getProperty(SofaArkTestConstants.MASTER_FAT_JAR);
            if (!Strings.isNullOrEmpty(config)) {
                URL url = new URL(config);
                String protocol = url.getProtocol();
                if (!Strings.isNullOrEmpty(protocol)) {
                    if (protocol.startsWith("file")) {
                        // find local
                        org.apache.commons.io.FileUtils.copyFile(FileUtil.toFile(url), new File(masterBizDir, DEFAULT_MASTERT_BIZ_NAME));
                    } else if (protocol.startsWith("http")) {
                        // find remote
                        downloadFromURL(url, new File(masterBizDir, DEFAULT_MASTERT_BIZ_NAME));
                    }
                }
            }
        }

        // again find local
        allArkJars = JarUtils.findArkJars(masterBizDir, JarUtils.masterJarSuffix());
        if (CollectionUtils.isEmpty(allArkJars)) {
            String masterFatJarGit = PropertiesUtils.getProperty(SofaArkTestConstants.MASTER_FAT_JAR_GIT);
            if (!Strings.isNullOrEmpty(masterFatJarGit)) {
                String branch = PropertiesUtils.getProperty(SofaArkTestConstants.MASTER_FAT_JAR_BRANCH, "master");
                String projectName = getNameFromGitAddress(masterFatJarGit);
                String target = PropertiesUtils.getProperty(SofaArkTestConstants.MASTER_FAT_JAR_RELATIVE, "target");
                File projectFile = new File(projectBaseDir, projectName);
                File projectTargetDir = new File(projectFile, target);

                // find project local
                allArkJars = JarUtils.findArkJars(projectTargetDir, JarUtils.masterJarSuffix());
                if (CollectionUtils.isEmpty(allArkJars)) {
                    long start = System.currentTimeMillis();
                    System.out.println("Start compile masterFatJar, please wait 10 ~ 15 minutes");
                    executeShellScript("sh", TARGET_MASTER_PACKAGE_SH, projectBaseDir, mavenOptions(), masterFatJarGit, branch,
                            projectName);
                    System.out.println(String.format("End compile masterFatJar, cost: %s", System.currentTimeMillis() - start));

                    // again find project local
                    allArkJars = JarUtils.findArkJars(projectTargetDir, JarUtils.masterJarSuffix());
                }

                if (!CollectionUtils.isEmpty(allArkJars)) {
                    String arkFileName = allArkJars.stream().findFirst().get();
                    org.apache.commons.io.FileUtils.copyFile(
                            new File(projectTargetDir, arkFileName),
                            new File(masterBizDir, DEFAULT_MASTERT_BIZ_NAME));
                }

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    projectFile.deleteOnExit();
                }));
            }
        }

        if (CollectionUtils.isEmpty(allArkJars)) {
            throw new UnsupportedOperationException("The MasterFatJar in the directory cannot be empty");
        }

        if (allArkJars.size() > 1) {
            LOGGER.warn("{} exists multiple arkJar", projectBaseDir);
        }
    }

    /**
     * Check biz fat jar.
     *
     * @throws Exception the exception
     */
    public static void checkBizFatJar() throws Exception {
        String projectBaseDir = CommonUtils.getProjectBaseDir();
        File bizDir = new File(projectBaseDir, BIZ_DIR);

        // find local
        List<String> allArkJars = JarUtils.findArkJars(bizDir, JarUtils.bizJarSuffix());
        if (CollectionUtils.isEmpty(allArkJars)) {
            // Try local compile
            long start = System.currentTimeMillis();
            System.out.println("Start compile bizFatJar, please wait 1 ~ 3 minutes");
            executeShellScript("sh", TARGET_BIZ_PACKAGE_SH, projectBaseDir, mavenOptions());
            System.out.println(String.format("End compile bizFatJar, cost: %s",
                System.currentTimeMillis() - start));

            // again find local
            allArkJars = JarUtils.findArkJars(bizDir, JarUtils.bizJarSuffix());
            if (CollectionUtils.isEmpty(allArkJars)) {
                // acquired config
                String config = PropertiesUtils.getProperty(SofaArkTestConstants.BIZ_FAT_JAR);
                if (!Strings.isNullOrEmpty(config)) {
                    URL url = new URL(config);
                    String protocol = url.getProtocol();
                    if (!Strings.isNullOrEmpty(protocol)) {
                        if (protocol.startsWith("file")) {
                            org.apache.commons.io.FileUtils.copyFile(FileUtil.toFile(url),
                                new File(bizDir, DEFAULT_BIZ_NAME));
                        } else if (protocol.startsWith("http")) {
                            downloadFromURL(url, new File(bizDir, DEFAULT_BIZ_NAME));
                        }
                    }
                }
            }

            // again find local
            allArkJars = JarUtils.findArkJars(bizDir, JarUtils.bizJarSuffix());
            if (CollectionUtils.isEmpty(allArkJars)) {
                throw new UnsupportedOperationException(
                    "The biz in the Target directory cannot be empty");
            }

            if (allArkJars.size() > 1) {
                LOGGER.warn("{} exists multiple arkJar", projectBaseDir);
            }
        }
    }

    private static void executeShellScript(String... command) throws IOException,
                                                             InterruptedException {
        String schellScriptTimeout = PropertiesUtils.getProperty(
            SofaArkTestConstants.SCHELL_SCRIPT_WAIT_TIMEOUT,
            SofaArkTestConstants.DEFAULT_SCHELL_SCRIPT_WAIT_TIMEOUT);
        new ProcessBuilder(command).start().waitFor(Integer.valueOf(schellScriptTimeout),
            TimeUnit.MINUTES);
    }

    /**
     * Maven options string.
     *
     * @return the string
     */
    public static String mavenOptions() {
        return EnvironmentUtils.getProperty(SofaArkTestConstants.MAVEN_OPTIONS, "");
    }

    /**
     * Download from url.
     *
     * @param url    the url
     * @param target the target
     * @throws Exception the exception
     */
    public static void downloadFromURL(URL url, File target) throws Exception {
        long start = System.currentTimeMillis();
        LOGGER.info("Start from url={} download resource", url);
        org.apache.commons.io.FileUtils.copyInputStreamToFile(url.openStream(), target);
        LOGGER.info("End from url={} download resource, cost={}, target={}", url,
            System.currentTimeMillis() - start, target);
    }

    /**
     * Gets name from git address.
     *
     * @param git the git
     * @return the name from git address
     */
    public static String getNameFromGitAddress(String git) {
        if (Strings.isNullOrEmpty(git)) {
            return null;
        }
        if (git.endsWith(".git")) {
            git = FileUtils.removeEnd(git, ".git");
        }
        String[] split = git.split("\\/");
        return split[split.length - 1];
    }
}