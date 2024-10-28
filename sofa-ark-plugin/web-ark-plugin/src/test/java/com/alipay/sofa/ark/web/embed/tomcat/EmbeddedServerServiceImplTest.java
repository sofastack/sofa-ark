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

import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EmbeddedServerServiceImplTest {

    private EmbeddedServerServiceImpl embeddedServerServiceImpl = new EmbeddedServerServiceImpl();

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testPutEmbedServer() {
        int port = 8080;
        assertEquals(false, embeddedServerServiceImpl.putEmbedServer(port, null));
        assertEquals(null, embeddedServerServiceImpl.getEmbedServer(port));

        Tomcat tomcat = new Tomcat();
        embeddedServerServiceImpl.putEmbedServer(port, tomcat);
        assertEquals(tomcat, embeddedServerServiceImpl.getEmbedServer(port));

        Tomcat tomcat2 = new Tomcat();
        embeddedServerServiceImpl.putEmbedServer(port, tomcat2);
        // should be still old tomcat
        assertEquals(tomcat, embeddedServerServiceImpl.getEmbedServer(port));

        // New test case for edge case
        int newPort = 9090;
        embeddedServerServiceImpl.putEmbedServer(newPort, tomcat2);
        assertEquals(tomcat2, embeddedServerServiceImpl.getEmbedServer(newPort));
        assertEquals(tomcat, embeddedServerServiceImpl.getEmbedServer(port));

    }
}