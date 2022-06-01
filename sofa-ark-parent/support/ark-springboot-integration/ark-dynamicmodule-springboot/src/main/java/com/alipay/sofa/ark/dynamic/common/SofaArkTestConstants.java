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
package com.alipay.sofa.ark.dynamic.common;

/**
 * @author hanyue
 * @version : SofaArkTestConstants.java, v 0.1 2022年05月25日 上午10:23 hanyue Exp $
 */
public class SofaArkTestConstants {

    /**
     * Workspace
     */
    public static final String WORKSPACE                          = System.getProperty("user.dir");

    /**
     * System Attribute
     */
    public static final String PROJECT_BASE_DIR                   = "project.base.dir";
    public static final String DEFAULT_SUFFIX_SOFA_ARK_JAR        = "-ark-biz.jar";

    /**
     * Master Biz Attribute
     */
    public static final String MASTER_FAT_JAR                     = "sofa.ark.jar.master";
    public static final String SUFFIX_SOFA_ARK_MASTER_JAR         = "sofa.ark.jar.master.suffix";

    public static final String MASTER_FAT_JAR_GIT                 = "sofa.ark.jar.master.git";
    public static final String MASTER_FAT_JAR_BRANCH              = "sofa.ark.jar.master.branch";
    public static final String MASTER_FAT_JAR_RELATIVE            = "sofa.ark.jar.master.relative";

    /**
     * Test Biz Attribute
     */
    public static final String BIZ_FAT_JAR                        = "sofa.ark.jar.biz";
    public static final String SUFFIX_SOFA_ARK_BIZ_JAR            = "sofa.ark.jar.biz.suffix";

    /**
     * ClassPath Attribute
     */
    public static final String SOFAARK_CONFIG_PROPERTIES          = "config/sofaark-config.properties";

    /**
     * schell script timeout
     */
    public static final String SCHELL_SCRIPT_WAIT_TIMEOUT         = "sofa.ark.script.timeout";
    public static final String DEFAULT_SCHELL_SCRIPT_WAIT_TIMEOUT = "30";                              // MINUTES

    /**
     * maven optiosn
     */
    public static final String MAVEN_OPTIONS                      = "sofa.ark.script.maven_options";
}