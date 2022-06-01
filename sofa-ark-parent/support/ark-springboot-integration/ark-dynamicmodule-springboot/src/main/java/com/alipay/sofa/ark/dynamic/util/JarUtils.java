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
package com.alipay.sofa.ark.dynamic.util;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author hanyue
 * @version : JarUtils.java, v 0.1 2022年05月30日 1:05 PM hanyue Exp $
 */
public class JarUtils {
    private static final ArkLogger LOGGER         = ArkLoggerFactory.getDefaultLogger();

    private static final String    MASTER_BIZ_DIR = "masterbiz";
    private static final String    BIZ_DIR        = "target";

    public static File getMasterBizFatJar() throws Exception {
        String projectBaseDir = CommonUtils.getProjectBaseDir();
        File masterBizDir = new File(projectBaseDir, MASTER_BIZ_DIR);
        List<String> allArkJars = findArkJars(masterBizDir, masterJarSuffix());
        String arkFileName = allArkJars.stream().findFirst().get();
        return new File(FileUtil.getPathBasedOn(masterBizDir.getAbsolutePath(), arkFileName));
    }

    public static File getBizFatJar() throws Exception {
        String projectBaseDir = CommonUtils.getProjectBaseDir();
        File bizDir = new File(projectBaseDir, BIZ_DIR);

        List<String> allArkJars = findArkJars(bizDir, bizJarSuffix());
        String arkFileName = allArkJars.stream().findFirst().get();
        File bizJar = new File(FileUtil.getPathBasedOn(bizDir.getAbsolutePath(), arkFileName));

        safeCopyBizJar(bizDir, bizJar);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (bizJar.exists()) {
                bizJar.delete();
            }
        }));
        return bizJar;
    }

    public static List<String> findArkJars(File arkJarDir, String suffix) {
        if (!arkJarDir.exists() || arkJarDir.list() == null) {
            return null;
        }

        List<String> arkFileNames = Stream.of(arkJarDir.list())
                .filter(fileName -> {
                    File file = new File(FileUtil.getPathBasedOn(arkJarDir.getAbsolutePath(), fileName));
                    if (file.isDirectory()) {
                        return false;
                    }
                    return fileName.endsWith(suffix);
                })
                .collect(Collectors.toList());

        return arkFileNames;
    }

    private static void safeCopyBizJar(File baseDirFile, File sourceBizJar) {
        try {
            String sourceBizJarName = sourceBizJar.getName();
            String $v = "$v";
            int version = 1;
            if (sourceBizJarName.startsWith($v)) {
                int rungIndex = sourceBizJarName.indexOf("-");
                version = Integer.valueOf(sourceBizJarName.substring($v.length(), rungIndex)) + 1; // 在此版本号上+1
                sourceBizJarName = sourceBizJarName.substring(rungIndex + 1);
            }
            String targetBizJarName = $v + version + "-" + sourceBizJarName;

            File targetBizJar = new File(FileUtil.getPathBasedOn(baseDirFile.getAbsolutePath(),
                targetBizJarName));
            org.apache.commons.io.FileUtils.copyFile(sourceBizJar, targetBizJar);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static String masterJarSuffix() {
        return PropertiesUtils.getProperty(SofaArkTestConstants.SUFFIX_SOFA_ARK_MASTER_JAR,
            SofaArkTestConstants.DEFAULT_SUFFIX_SOFA_ARK_JAR);
    }

    public static String bizJarSuffix() {
        return PropertiesUtils.getProperty(SofaArkTestConstants.SUFFIX_SOFA_ARK_BIZ_JAR,
            SofaArkTestConstants.DEFAULT_SUFFIX_SOFA_ARK_JAR);
    }
}