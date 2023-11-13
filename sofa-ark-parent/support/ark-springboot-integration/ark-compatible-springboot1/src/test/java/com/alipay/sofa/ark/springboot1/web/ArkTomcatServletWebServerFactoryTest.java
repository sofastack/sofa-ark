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
package com.alipay.sofa.ark.springboot1.web;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.boot.web.servlet.ServletContextInitializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.ROOT_WEB_CONTEXT_PATH;
import static com.alipay.sofa.ark.spi.model.BizState.RESOLVED;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;

/**
 * @author qixiaobo
 * @since 0.6.0
 */
public class ArkTomcatServletWebServerFactoryTest {

    private ArkTomcatEmbeddedServletContainerFactory arkTomcatEmbeddedServletContainerFactory = new ArkTomcatEmbeddedServletContainerFactory();

    private ClassLoader                              currentThreadContextClassLoader;

    @Before
    public void setUp() {
        currentThreadContextClassLoader = currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() {
        currentThread().setContextClassLoader(currentThreadContextClassLoader);
    }

    @Test
    public void testGetWebServerWithEmbeddedServerServiceNull() {
        assertEquals(ArkTomcatEmbeddedServletContainer.class,
            arkTomcatEmbeddedServletContainerFactory.getEmbeddedServletContainer().getClass());
    }

    @Test
    public void testGetContextPath() throws Exception {

        assertEquals("", arkTomcatEmbeddedServletContainerFactory.getContextPath());

        BizManagerServiceImpl bizManagerService = new BizManagerServiceImpl();
        Field field = ArkTomcatEmbeddedServletContainerFactory.class
            .getDeclaredField("bizManagerService");
        field.setAccessible(true);
        field.set(arkTomcatEmbeddedServletContainerFactory, bizManagerService);
        assertEquals(ROOT_WEB_CONTEXT_PATH,
            arkTomcatEmbeddedServletContainerFactory.getContextPath());

        BizModel biz = new BizModel();
        biz.setBizName("bbb");
        biz.setBizState(RESOLVED);
        biz.setBizVersion("ccc");
        biz.setClassLoader(this.getClass().getClassLoader());
        bizManagerService.registerBiz(biz);
        assertEquals(ROOT_WEB_CONTEXT_PATH,
            arkTomcatEmbeddedServletContainerFactory.getContextPath());

        biz.setWebContextPath("/ddd");
        currentThread().setContextClassLoader(biz.getBizClassLoader());
        assertEquals("/ddd", arkTomcatEmbeddedServletContainerFactory.getContextPath());

        arkTomcatEmbeddedServletContainerFactory.setContextPath("/aaa");
        assertEquals("/aaa", arkTomcatEmbeddedServletContainerFactory.getContextPath());
    }

    @Test
    public void testPrepareContext() throws LifecycleException {

        StandardHost host = new StandardHost();
        host.init();

        assertEquals(0, host.getChildren().length);
        arkTomcatEmbeddedServletContainerFactory.setRegisterDefaultServlet(true);
        currentThread().setContextClassLoader(this.getClass().getClassLoader());
        arkTomcatEmbeddedServletContainerFactory.prepareContext(host,
            new ServletContextInitializer[] {});
        assertEquals(1, host.getChildren().length);
    }

    @Test
    public void testOtherMethods() {
        arkTomcatEmbeddedServletContainerFactory.setBackgroundProcessorDelay(10);
        arkTomcatEmbeddedServletContainerFactory.setBaseDirectory(null);
        arkTomcatEmbeddedServletContainerFactory.setProtocol("8888");
    }

    @Test
    public void testStaticResourceConfigurer() throws Exception {

        List<URL> urls = new ArrayList<>();
        urls.add(new URL("file:///aaa.jar!/"));
        urls.add(new URL("jar:file:///aaa.jar!/"));
        urls.add(new URL("file:///aaa"));
        urls.add(new URL("file:///!/aaa!/"));

        Constructor<ArkTomcatEmbeddedServletContainerFactory.StaticResourceConfigurer> declaredConstructor = ArkTomcatEmbeddedServletContainerFactory.StaticResourceConfigurer.class
            .getDeclaredConstructor(ArkTomcatEmbeddedServletContainerFactory.class, Context.class);
        declaredConstructor.setAccessible(true);
        ArkTomcatEmbeddedServletContainerFactory.StaticResourceConfigurer staticResourceConfigurer = declaredConstructor
            .newInstance(arkTomcatEmbeddedServletContainerFactory, new StandardContext());

        Method addResourceJars = ArkTomcatEmbeddedServletContainerFactory.StaticResourceConfigurer.class
            .getDeclaredMethod("addResourceJars", List.class);
        addResourceJars.setAccessible(true);
        addResourceJars.invoke(staticResourceConfigurer, urls);
    }
}
