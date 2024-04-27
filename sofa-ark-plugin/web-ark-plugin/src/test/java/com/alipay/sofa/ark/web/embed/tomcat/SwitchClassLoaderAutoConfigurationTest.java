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
package com.alipay.sofa.ark.web.embed.tomcat;

import com.alipay.sofa.ark.spi.model.Biz;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SwitchClassLoaderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                                                             .withConfiguration(AutoConfigurations
                                                                 .of(SwitchClassLoaderAutoConfiguration.class));

    @Before
    public void prepare() {
        Biz biz = mock(Biz.class);
        when(biz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test() {
        ClassLoader webHandlerClassLoader = new URLClassLoader(new URL[0]);
        ClassLoader classLoader = new ArkTomcatEmbeddedWebappClassLoader(webHandlerClassLoader);

        FilterChain filterChain = (servletRequest, servletResponse) -> Assert.assertEquals(webHandlerClassLoader, Thread.currentThread().getContextClassLoader());

        contextRunner.withClassLoader(classLoader).run(context -> {
            Assertions.assertThat(context).hasSingleBean(
                    SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter.class);
            SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter switchClassLoaderFilter = (SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter) context.getBean(
                    "switchClassLoaderFilter");
            switchClassLoaderFilter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);
        });
    }

    @Test
    public void switchClassLoaderFilter() {
    }
}
