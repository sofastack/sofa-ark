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

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.selector.ContextSelector;
import ch.qos.logback.classic.util.ContextSelectorStaticBinder;
import ch.qos.logback.core.CoreConstants;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author:     yuanyuan
 * @date:    2023/12/12 5:02 下午
 */
public class ArkLogbackContextSelectorTest {

    @Test
    public void testContextSelector() throws NoSuchMethodException, InvocationTargetException,
                                     IllegalAccessException {
        System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR,
            "com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector");
        Logger logger = LoggerFactory.getLogger(ArkLogbackContextSelectorTest.class);
        System.clearProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR);

        ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
        LoggerContext loggerContext = (LoggerContext) iLoggerFactory;
        ContextSelectorStaticBinder selectorStaticBinder = ContextSelectorStaticBinder
            .getSingleton();
        ContextSelector contextSelector = selectorStaticBinder.getContextSelector();
        Assert.assertTrue(contextSelector instanceof ArkLogbackContextSelector);
        Assert.assertEquals(loggerContext, contextSelector.getDefaultLoggerContext());

        URL url = ArkLogbackContextSelectorTest.class.getClassLoader().getResource("");
        URLClassLoader loader = new URLClassLoader(new URL[] { url }, null);
        String contextName = CoreConstants.DEFAULT_CONTEXT_NAME;

        Method getContext = ArkLogbackContextSelector.class.getDeclaredMethod("getContext",
            ClassLoader.class);
        getContext.setAccessible(true);
        Object invoke = getContext.invoke(contextSelector, loader);
        Assert.assertNotNull(invoke);
        Assert.assertEquals(invoke, contextSelector.getLoggerContext(contextName));
        Assert.assertTrue(contextSelector.getContextNames().contains(contextName));

    }
}
