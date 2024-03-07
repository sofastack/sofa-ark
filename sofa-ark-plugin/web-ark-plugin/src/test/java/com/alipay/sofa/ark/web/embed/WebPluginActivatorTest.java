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
package com.alipay.sofa.ark.web.embed;

import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class WebPluginActivatorTest {

    private WebPluginActivator    webPluginActivator            = new WebPluginActivator();

    private EmbeddedServerService originalEmbeddedServerService = webPluginActivator.embeddedServerService;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
        webPluginActivator.embeddedServerService = originalEmbeddedServerService;
    }

    @Test
    public void testStartAndStop() throws LifecycleException {

        EmbeddedServerService embeddedServerService = mock(EmbeddedServerService.class);
        webPluginActivator.embeddedServerService = embeddedServerService;

        PluginContext pluginContext = mock(PluginContext.class);
        webPluginActivator.start(pluginContext);
        verify(pluginContext, times(1)).publishService(EmbeddedServerService.class,
            embeddedServerService);

        Tomcat tomcat = mock(Tomcat.class);
        when(embeddedServerService.getEmbedServer()).thenReturn(tomcat);

        webPluginActivator.stop(pluginContext);
        verify(embeddedServerService, times(2)).getEmbedServer();
        verify(tomcat, times(1)).destroy();

        doThrow(new LifecycleException()).when(tomcat).destroy();
        webPluginActivator.stop(pluginContext);
        verify(embeddedServerService, times(4)).getEmbedServer();
        verify(tomcat, times(2)).destroy();

        when(embeddedServerService.getEmbedServer()).thenReturn(new Object());
        webPluginActivator.stop(pluginContext);
        verify(embeddedServerService, times(5)).getEmbedServer();
        verify(tomcat, times(2)).destroy();
    }
}
