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
package com.alipay.sofa.ark.boot.mojo.model;

import lombok.SneakyThrows;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_BASE_DIR;
import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_FILE;
import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_YAML_FILE;
import static com.google.common.collect.Maps.newHashMap;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArkConfigHolder.java, v 0.1 2024年06月26日 13:45 立蓬 Exp $
 */
public class ArkConfigHolder {
    private static Properties          arkProperties;

    private static Map<String, Object> arkYaml;

    private static SystemStreamLog     log = new SystemStreamLog();

    public static Properties getArkProperties(String baseDir) {
        return arkProperties == null ? initArkProperties(baseDir) : arkProperties;
    }

    public static Map<String, Object> getArkYaml(String baseDir) {
        return arkYaml == null ? initArkYaml(baseDir) : arkYaml;
    }

    @SneakyThrows
    private static Map<String, Object> initArkYaml(String baseDir) {
        String configPath = baseDir + File.separator + ARK_CONF_BASE_DIR + File.separator
                            + ARK_CONF_YAML_FILE;
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            log.info(String.format(
                "sofa-ark-maven-plugin: extension-config %s not found, will not config it",
                configPath));
            return newHashMap();
        }

        log.info(String.format(
            "sofa-ark-maven-plugin: find extension-config %s and will config it",
            configPath));

        try (FileInputStream fis = new FileInputStream(configPath)) {
            Yaml yaml = new Yaml();
            arkYaml = yaml.load(fis);
            return arkYaml;
        } catch (IOException ex) {
            log.error(String.format("failed to parse excludes artifacts from %s.", configPath), ex);
            throw ex;
        }
    }

    @SneakyThrows
    private static Properties initArkProperties(String baseDir) {
        String configPath = baseDir + File.separator + ARK_CONF_BASE_DIR + File.separator
                            + ARK_CONF_FILE;
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            log.info(String.format(
                "sofa-ark-maven-plugin: extension-config %s not found, will not config it",
                configPath));
            return new Properties();
        }

        log.info(String.format(
            "sofa-ark-maven-plugin: find extension-config %s and will config it",
            configPath));

        Properties prop = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            prop.load(fis);
            arkProperties = prop;
            return prop;
        } catch (IOException ex) {
            log.error(String.format(
                "sofa-ark-maven-plugin: failed to read extension-config from %s.",
                configPath), ex);
            throw ex;
        }
    }
}