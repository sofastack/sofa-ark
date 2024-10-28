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
package com.alipay.sofa.ark.springboot.web;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.biz.BizManagerServiceImpl;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.springboot.web.ArkTomcatServletWebServerFactory.StaticResourceConfigurer;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.Jsp;

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

public class ArkTomcatServletWebServerFactoryTest {

    private ArkTomcatServletWebServerFactory arkTomcatServletWebServerFactory = new ArkTomcatServletWebServerFactory();

    private ClassLoader                      currentThreadContextClassLoader;
    private InjectionService                 injectionService;

    @Before
    public void setUp() {
        injectionService = ArkClient.getInjectionService();
        ArkClient.setInjectionService(null);
        currentThreadContextClassLoader = currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() {
        ArkClient.setInjectionService(injectionService);
        currentThread().setContextClassLoader(currentThreadContextClassLoader);
    }

    @Test
    public void testGetWebServerWithEmbeddedServerServiceNull() {
        assertEquals(TomcatWebServer.class, arkTomcatServletWebServerFactory.getWebServer()
            .getClass());
    }

    @Test
    public void testGetContextPath() throws Exception {

        assertEquals("", arkTomcatServletWebServerFactory.getContextPath());

        BizManagerServiceImpl bizManagerService = new BizManagerServiceImpl();
        Field field = ArkTomcatServletWebServerFactory.class.getDeclaredField("bizManagerService");
        field.setAccessible(true);
        field.set(arkTomcatServletWebServerFactory, bizManagerService);
        assertEquals(ROOT_WEB_CONTEXT_PATH, arkTomcatServletWebServerFactory.getContextPath());

        BizModel biz = new BizModel();
        biz.setBizName("bbb");
        biz.setBizState(RESOLVED);
        biz.setBizVersion("ccc");
        biz.setClassLoader(this.getClass().getClassLoader());
        bizManagerService.registerBiz(biz);
        assertEquals(ROOT_WEB_CONTEXT_PATH, arkTomcatServletWebServerFactory.getContextPath());

        biz.setWebContextPath("/ddd");
        currentThread().setContextClassLoader(biz.getBizClassLoader());
        assertEquals("/ddd", arkTomcatServletWebServerFactory.getContextPath());

        arkTomcatServletWebServerFactory.setContextPath("/aaa");
        assertEquals("/aaa", arkTomcatServletWebServerFactory.getContextPath());
    }

    @Test
    public void testPrepareContext() throws LifecycleException {

        StandardHost host = new StandardHost();
        host.init();

        assertEquals(0, host.getChildren().length);
        arkTomcatServletWebServerFactory.setRegisterDefaultServlet(true);
        currentThread().setContextClassLoader(this.getClass().getClassLoader());
        Jsp jsp = new Jsp();
        jsp.setRegistered(true);
        // Otherwise JSP won't be loaded by ApplicationClassLoader, so JSP-relative initialization code won't be executed.
        jsp.setClassName("com.alipay.sofa.ark.springboot.web.ArkTomcatServletWebServerFactoryTest");
        arkTomcatServletWebServerFactory.setJsp(jsp);
        arkTomcatServletWebServerFactory.prepareContext(host, new ServletContextInitializer[] {});
        assertEquals(1, host.getChildren().length);
    }

    @Test
    public void testOtherMethods() {
        arkTomcatServletWebServerFactory.setBackgroundProcessorDelay(10);
        arkTomcatServletWebServerFactory.setBaseDirectory(null);
        arkTomcatServletWebServerFactory.setProtocol("8888");
    }

    @Test
    public void testStaticResourceConfigurer() throws Exception {

        List<URL> urls = new ArrayList<>();
        urls.add(new URL("file:///aaa.jar!/"));
        urls.add(new URL("jar:file:///aaa.jar!/"));
        urls.add(new URL("file:///aaa"));
        urls.add(new URL("file:///!/aaa!/"));

        Constructor<StaticResourceConfigurer> declaredConstructor = StaticResourceConfigurer.class
            .getDeclaredConstructor(ArkTomcatServletWebServerFactory.class, Context.class);
        declaredConstructor.setAccessible(true);
        StaticResourceConfigurer staticResourceConfigurer = declaredConstructor.newInstance(
            arkTomcatServletWebServerFactory, new StandardContext());

        Method addResourceJars = StaticResourceConfigurer.class.getDeclaredMethod(
            "addResourceJars", List.class);
        addResourceJars.setAccessible(true);
        addResourceJars.invoke(staticResourceConfigurer, urls);
    }
}
