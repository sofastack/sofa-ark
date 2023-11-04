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

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author zsk
 * @version $Id: MockApolloConfig.java, v 0.1 2023年10月11日 20:26 zsk Exp $
 */
public class MockApolloConfig implements Config {
    private final List<ConfigChangeListener> m_listeners = Lists.newCopyOnWriteArrayList();

    @Override
    public String getProperty(String key, String defaultValue) {
        return null;
    }

    @Override
    public Integer getIntProperty(String key, Integer defaultValue) {
        return null;
    }

    @Override
    public Long getLongProperty(String key, Long defaultValue) {
        return null;
    }

    @Override
    public Short getShortProperty(String key, Short defaultValue) {
        return null;
    }

    @Override
    public Float getFloatProperty(String key, Float defaultValue) {
        return null;
    }

    @Override
    public Double getDoubleProperty(String key, Double defaultValue) {
        return null;
    }

    @Override
    public Byte getByteProperty(String key, Byte defaultValue) {
        return null;
    }

    @Override
    public Boolean getBooleanProperty(String key, Boolean defaultValue) {
        return null;
    }

    @Override
    public String[] getArrayProperty(String key, String delimiter, String[] defaultValue) {
        return new String[0];
    }

    @Override
    public Date getDateProperty(String key, Date defaultValue) {
        return null;
    }

    @Override
    public Date getDateProperty(String key, String format, Date defaultValue) {
        return null;
    }

    @Override
    public Date getDateProperty(String key, String format, Locale locale, Date defaultValue) {
        return null;
    }

    @Override
    public <T extends Enum<T>> T getEnumProperty(String key, Class<T> enumType, T defaultValue) {
        return null;
    }

    @Override
    public long getDurationProperty(String key, long defaultValue) {
        return 0;
    }

    public List<ConfigChangeListener> getListeners() {
        return m_listeners;
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        addChangeListener(listener, null);
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys) {
        addChangeListener(listener, interestedKeys, null);
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener, Set<String> interestedKeys,
                                  Set<String> interestedKeyPrefixes) {
        if (!m_listeners.contains(listener)) {
            m_listeners.add(listener);
        }
    }

    @Override
    public boolean removeChangeListener(ConfigChangeListener listener) {
        return m_listeners.remove(listener);
    }

    @Override
    public Set<String> getPropertyNames() {
        return null;
    }

    @Override
    public <T> T getProperty(String key, Function<String, T> function, T defaultValue) {
        return null;
    }

    @Override
    public ConfigSourceType getSourceType() {
        return null;
    }
}
