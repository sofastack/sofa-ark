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

import com.alipay.sofa.ark.web.embed.tomcat.EmbeddedServerServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @author qixiaobo
 * @since 0.6.0
 */
public class ArkTomcatEmbeddedServletContainerTest {

    private ArkTomcatEmbeddedServletContainerFactory arkTomcatEmbeddedServletContainerFactory;

    private ArkTomcatEmbeddedServletContainer        arkTomcatEmbeddedServletContainer;

    @Before
    public void setUp() throws Exception {
        arkTomcatEmbeddedServletContainerFactory = new ArkTomcatEmbeddedServletContainerFactory();
        Field field = ArkTomcatEmbeddedServletContainerFactory.class
            .getDeclaredField("embeddedServerService");
        field.setAccessible(true);
        field.set(arkTomcatEmbeddedServletContainerFactory, new EmbeddedServerServiceImpl());
        arkTomcatEmbeddedServletContainer = (ArkTomcatEmbeddedServletContainer) arkTomcatEmbeddedServletContainerFactory
            .getEmbeddedServletContainer();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetWebServerWithEmbeddedServerServiceNull() {
        //        NOTE: tomcat can not be stopped and restarted due to a Spring context destroy problem.
        //        Spring community will fix this issue in the future, so catch all exception now.
        try {
            arkTomcatEmbeddedServletContainer.stopSilently();
        } catch (Exception e) {
        }
        try {
            arkTomcatEmbeddedServletContainer.stop();
        } catch (Exception e) {
        }
    }
}
