package com.alipay.sofa.ark.springboot.web;

import com.alipay.sofa.ark.web.embed.tomcat.EmbeddedServerServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

public class ArkTomcatWebServerTest {

    private ArkTomcatServletWebServerFactory arkTomcatServletWebServerFactory;

    private ArkTomcatWebServer arkTomcatWebServer;

    @Before
    public void setUp() {
        arkTomcatServletWebServerFactory = new ArkTomcatServletWebServerFactory();
        try {
            Field field = ArkTomcatServletWebServerFactory.class.getDeclaredField("embeddedServerService");
            field.setAccessible(true);
            field.set(arkTomcatServletWebServerFactory, new EmbeddedServerServiceImpl());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        arkTomcatWebServer = (ArkTomcatWebServer) arkTomcatServletWebServerFactory.getWebServer();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetWebServerWithEmbeddedServerServiceNull() {
        arkTomcatWebServer.start();
        arkTomcatWebServer.start();
        arkTomcatWebServer.stopSilently();
        arkTomcatWebServer.start();
        arkTomcatWebServer.stop();
        assertEquals(8080, arkTomcatWebServer.getPort());
    }
}
