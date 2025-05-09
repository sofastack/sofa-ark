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

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.COMMA_SPLIT;
import static com.google.common.collect.Sets.newHashSet;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ParseUtils.java, v 0.1 2024年07月14日 01:24 立蓬 Exp $
 */
public class ParseUtils {
    public static Set<String> getStringSet(Properties prop, String confKey) {
        if (null == prop) {
            return newHashSet();
        }
        String[] values = StringUtils.split(prop.getProperty(confKey), COMMA_SPLIT);
        if (values == null) {
            return newHashSet();
        }
        values = Arrays.stream(values).map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new);
        return newHashSet(values);
    }

    public static Set<String> getStringSet(Map<String, Object> yaml, String confKey) {
        Object value = getValue(yaml, confKey);
        if (null == value) {
            return newHashSet();
        }
        return newHashSet((List<String>) value);
    }

    public static boolean getBooleanWithDefault(Map<String, Object> yaml, String confKey,
                                                boolean defaultValue) {
        Object value = getValue(yaml, confKey);

        if (null == value) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public static boolean getBooleanWithDefault(Properties prop, String confKey,
                                                boolean defaultValue) {
        Object value = prop.getProperty(confKey);
        if (null == value) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public static boolean getBooleanWithDefault(Properties prop, Map<String, Object> yaml,
                                                String confKey, boolean defaultValue) {
        Object valueFromProp = prop.getProperty(confKey);
        Object valueFromYaml = getValue(yaml, confKey);

        if (null == valueFromProp && null == valueFromYaml) {
            return defaultValue;
        }
        return null != valueFromProp ? Boolean.parseBoolean(valueFromProp.toString()) : Boolean
            .parseBoolean(valueFromYaml.toString());
    }

    private static Object getValue(Map<String, Object> yaml, String confKey) {
        if (MapUtils.isEmpty(yaml) || StringUtils.isEmpty(confKey)) {
            return null;
        }

        List<String> keys = Arrays.asList(StringUtils.split(confKey, "."));
        String currentKey = keys.get(0);
        if (yaml.containsKey(currentKey) && null != yaml.get(currentKey) && keys.size() == 1) {
            return yaml.get(currentKey);
        }

        if (yaml.containsKey(currentKey) && null != yaml.get(currentKey) && keys.size() > 1
            && yaml.get(currentKey) instanceof Map) {
            return getValue((Map<String, Object>) yaml.get(currentKey),
                StringUtils.join(keys.subList(1, keys.size()), "."));
        }
        return null;
    }
}
