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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ConfigUtils {
    private final static File bizInstallDir;
    static {
        File workDir = FileUtils.createTempDir("sofa-ark");
        String configDir = ArkConfigs.getStringValue(Constants.CONFIG_INSTALL_BIZ_DIR);
        if (!StringUtils.isEmpty(configDir)) {
            if (!configDir.endsWith(File.separator)) {
                configDir += File.separator;
            }
            workDir = new File(configDir);
            if (!workDir.exists()) {
                workDir.mkdir();
            }
        }
        bizInstallDir = workDir;
    }

    public static File createBizSaveFile(String bizName, String bizVersion) {
        return new File(bizInstallDir,
            bizName + "-" + bizVersion + "-"
                    + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
    }
}