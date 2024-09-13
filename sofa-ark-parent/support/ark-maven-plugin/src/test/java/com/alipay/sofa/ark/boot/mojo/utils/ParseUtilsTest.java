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
package com.alipay.sofa.ark.boot.mojo.utils;

import com.alipay.sofa.ark.boot.mojo.CommonUtils;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static com.alipay.sofa.ark.boot.mojo.utils.ParseUtils.getBooleanWithDefault;
import static com.alipay.sofa.ark.spi.constant.Constants.DECLARED_LIBRARIES_WHITELIST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ParseUtilsTest.java, v 0.1 2024年08月23日 10:55 立蓬 Exp $
 */
public class ParseUtilsTest {
    @Test
    public void testGetStringSetForYaml() throws URISyntaxException {
        Map<String, Object> yml = (Map<String, Object>) loadYaml();
        Set<String> excludes = ParseUtils.getStringSet(yml, "excludes");
        Set<String> whitelist = ParseUtils.getStringSet(yml, DECLARED_LIBRARIES_WHITELIST);

        assertTrue(excludes.contains("org.apache.commons:commons-lang3-yml"));
        assertTrue(excludes.contains("commons-beanutils:commons-beanutils-yml"));

        assertTrue(whitelist.contains("com.biz.yml:biz-common-yml"));
        assertTrue(whitelist.contains("com.ark.yml:ark-common-yml"));
    }

    @Test
    public void testGetBooleanWithDefault() throws URISyntaxException {
        Map<String, Object> yml = (Map<String, Object>) loadYaml();
        assertFalse(getBooleanWithDefault(yml, "excludeWithIndirectDependencies", true));
        assertTrue(getBooleanWithDefault(yml, "aaa", true));
    }

    private Object loadYaml() throws URISyntaxException {
        File yml = CommonUtils.getResourceFile("baseDir/conf/ark/bootstrap.yml");
        try (FileInputStream fis = new FileInputStream(yml)) {
            Yaml yaml = new Yaml();
            return yaml.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}