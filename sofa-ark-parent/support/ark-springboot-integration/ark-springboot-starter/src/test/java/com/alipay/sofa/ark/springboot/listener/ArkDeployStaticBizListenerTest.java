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
package com.alipay.sofa.ark.springboot.listener;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

import static org.mockito.Mockito.*;

/**
 * @author gaowh
 * @version 1.0
 * @time 2024/12/30
 */
public class ArkDeployStaticBizListenerTest {

    /**
     * classloader不匹配的场景
     */
    @Test
    public void testDiffClassLoader() {
        ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (MockedStatic<EmbedSofaArkBootstrap> bootstrap = mockStatic(EmbedSofaArkBootstrap.class);
             MockedStatic<ArkConfigs> arkConfigs = mockStatic(ArkConfigs.class)) {
            arkConfigs.when(ArkConfigs::isEmbedEnable).thenReturn(true);
            arkConfigs.when(ArkConfigs::isEmbedStaticBizEnable).thenReturn(true);
            Thread.currentThread().setContextClassLoader(new ClassLoader() {
            });
            listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
            bootstrap.verify(EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted, Mockito.times(0));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * applicationEvent 不是 ContextRefreshedEvent 的场景
     */
    @Test
    public void testNonContextRefreshedEvent() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (MockedStatic<EmbedSofaArkBootstrap> bootstrap = mockStatic(EmbedSofaArkBootstrap.class);
             MockedStatic<ArkConfigs> arkConfigs = mockStatic(ArkConfigs.class)) {
            ClassLoader classLoader = ArkDeployStaticBizListener.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            arkConfigs.when(ArkConfigs::isEmbedEnable).thenReturn(true);
            arkConfigs.when(ArkConfigs::isEmbedStaticBizEnable).thenReturn(true);
            ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
            listener.onApplicationEvent(new ContextStartedEvent(new AnnotationConfigApplicationContext()));
            bootstrap.verify(EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted, Mockito.times(0));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
    *  applicationEvent不是 spring 根上下文的场景
    */
    @Test
    public void testNonSpringRootEvent() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (MockedStatic<EmbedSofaArkBootstrap> bootstrap = mockStatic(EmbedSofaArkBootstrap.class);
            MockedStatic<ArkConfigs> arkConfigs = mockStatic(ArkConfigs.class)) {
            ClassLoader classLoader = ArkDeployStaticBizListener.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            arkConfigs.when(ArkConfigs::isEmbedEnable).thenReturn(true);
            arkConfigs.when(ArkConfigs::isEmbedStaticBizEnable).thenReturn(true);
            ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
            listener.onApplicationEvent(new ContextStartedEvent(new AnnotationConfigServletWebServerApplicationContext()));
            bootstrap.verify(EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted, Mockito.times(0));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * 事件重复发送的场景
     */
    @Test
    public void testDeployed() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try (MockedStatic<EmbedSofaArkBootstrap> bootstrap = mockStatic(EmbedSofaArkBootstrap.class);
             MockedStatic<ArkConfigs> arkConfigs = mockStatic(ArkConfigs.class)) {
            ClassLoader classLoader = ArkDeployStaticBizListener.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            arkConfigs.when(ArkConfigs::isEmbedEnable).thenReturn(true);
            arkConfigs.when(ArkConfigs::isEmbedStaticBizEnable).thenReturn(true);
            ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
            listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
            // 容器刷新事件已经发送过，重复发送不会重复部署
            listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
            bootstrap.verify(EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted, Mockito.times(1));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
}
