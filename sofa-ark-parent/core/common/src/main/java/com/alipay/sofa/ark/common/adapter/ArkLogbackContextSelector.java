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
package com.alipay.sofa.ark.common.adapter;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.core.CoreConstants;
import com.alipay.sofa.ark.common.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ArkLogbackContextSelector implements ContextSelector {

    private static final Map<ClassLoader, LoggerContext> CLASS_LOADER_LOGGER_CONTEXT = new HashMap<>();

    private static final String                          BIZ_CLASS_LOADER            = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";
    private static final String                          CONTAINER_CLASS_LOADER      = "com.alipay.sofa.ark.bootstrap.ContainerClassLoader";

    private LoggerContext                                defaultLoggerContext;

    public ArkLogbackContextSelector(LoggerContext loggerContext) {
        this.defaultLoggerContext = loggerContext;
    }

    @Override
    public LoggerContext getLoggerContext() {
        ClassLoader classLoader = this.findClassLoader();
        if (classLoader == null) {
            return defaultLoggerContext;
        }
        return getContext(classLoader);
    }

    private ClassLoader findClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null && CONTAINER_CLASS_LOADER.equals(classLoader.getClass().getName())) {
            return null;
        }
        if (classLoader != null && BIZ_CLASS_LOADER.equals(classLoader.getClass().getName())) {
            return classLoader;
        }

        Class<?>[] context = new SecurityManager() {
            @Override
            public Class<?>[] getClassContext() {
                return super.getClassContext();
            }
        }.getClassContext();
        if (context == null || context.length == 0) {
            return null;
        }
        for (Class<?> cls : context) {
            if (cls.getClassLoader() != null
                && BIZ_CLASS_LOADER.equals(cls.getClassLoader().getClass().getName())) {
                return cls.getClassLoader();
            }
        }

        return null;
    }

    private LoggerContext getContext(ClassLoader cls) {
        LoggerContext loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
        if (null == loggerContext) {
            synchronized (ArkLogbackContextSelector.class) {
                loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
                if (null == loggerContext) {
                    loggerContext = new LoggerContext();
                    loggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
                    CLASS_LOADER_LOGGER_CONTEXT.put(cls, loggerContext);
                }
            }
        }
        return loggerContext;
    }

    @Override
    public LoggerContext getLoggerContext(String name) {
        if (StringUtils.isEmpty(name)) {
            return defaultLoggerContext;
        }
        for (ClassLoader classLoader : CLASS_LOADER_LOGGER_CONTEXT.keySet()) {
            LoggerContext loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(classLoader);
            if (name.equals(loggerContext.getName())) {
                return loggerContext;
            }
        }
        return defaultLoggerContext;
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultLoggerContext;
    }

    @Override
    public LoggerContext detachLoggerContext(String loggerContextName) {
        if (StringUtils.isEmpty(loggerContextName)) {
            return null;
        }
        for (ClassLoader classLoader : CLASS_LOADER_LOGGER_CONTEXT.keySet()) {
            LoggerContext loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(classLoader);
            if (loggerContextName.equals(loggerContext.getName())) {
                return removeContext(classLoader);
            }
        }
        return null;
    }

    public LoggerContext removeContext(ClassLoader cls) {
        if (cls == null) {
            return null;
        }
        return CLASS_LOADER_LOGGER_CONTEXT.remove(cls);
    }

    @Override
    public List<String> getContextNames() {
        return CLASS_LOADER_LOGGER_CONTEXT.values().stream().map(LoggerContext::getName).collect(Collectors.toList());
    }
}
