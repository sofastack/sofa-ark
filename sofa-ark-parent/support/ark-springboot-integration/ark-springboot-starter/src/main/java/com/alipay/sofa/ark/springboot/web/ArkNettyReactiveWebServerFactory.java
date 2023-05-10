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

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyRouteProvider;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.server.reactive.ContextPathCompositeHandler;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.LoopResources;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static com.alipay.sofa.ark.spi.constant.Constants.ROOT_WEB_CONTEXT_PATH;

public class ArkNettyReactiveWebServerFactory extends NettyReactiveWebServerFactory {
    private static final Charset       DEFAULT_CHARSET   = StandardCharsets.UTF_8;
    private Duration                   lifecycleTimeout;

    private List<NettyRouteProvider>   routeProviders    = new ArrayList();
    @ArkInject
    private EmbeddedServerService      embeddedNettyService;

    @ArkInject
    private BizManagerService          bizManagerService;

    private boolean                    useForwardHeaders;

    private ReactorResourceFactory     resourceFactory;

    private int                        backgroundProcessorDelay;
    private Set<NettyServerCustomizer> serverCustomizers = new LinkedHashSet();

    public ArkNettyReactiveWebServerFactory() {
    }

    public ArkNettyReactiveWebServerFactory(int port) {
        super(port);
    }

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        String contextPath = getContextPath();
        Map<String, HttpHandler> handlerMap = new HashMap<>();
        handlerMap.put(contextPath, httpHandler);
        ContextPathCompositeHandler contextHandler = new ContextPathCompositeHandler(handlerMap);

        if (embeddedNettyService == null) {
            return super.getWebServer(contextHandler);
        } else if (embeddedNettyService.getEmbedServer() == null) {
            embeddedNettyService.setEmbedServer(initEmbedNetty());
        }
        HttpServer httpServer = (HttpServer) embeddedNettyService.getEmbedServer();
        ReactorHttpHandlerAdapter handlerAdapter = new ReactorHttpHandlerAdapter(contextHandler);
        ArkNettyWebServer webServer = (ArkNettyWebServer) createNettyWebServer(httpServer,
            handlerAdapter, lifecycleTimeout);
        webServer.setRouteProviders(this.routeProviders);

        return webServer;
    }

    public String getContextPath() {
        String contextPath = "";
        if (bizManagerService == null) {
            return contextPath;
        }
        Biz biz = bizManagerService.getBizByClassLoader(Thread.currentThread()
            .getContextClassLoader());

        if (!StringUtils.isEmpty(contextPath)) {
            return contextPath;
        } else if (biz != null) {
            if (StringUtils.isEmpty(biz.getWebContextPath())) {
                return ROOT_WEB_CONTEXT_PATH;
            }
            return biz.getWebContextPath();
        } else {
            return ROOT_WEB_CONTEXT_PATH;
        }
    }

    WebServer createNettyWebServer(HttpServer httpServer, ReactorHttpHandlerAdapter handlerAdapter,
                                   Duration lifecycleTimeout) {
        return new ArkNettyWebServer(httpServer, handlerAdapter, lifecycleTimeout);
    }

    private HttpServer  initEmbedNetty(){
        HttpServer server = HttpServer.create();
        if (this.resourceFactory != null) {
            LoopResources resources = this.resourceFactory.getLoopResources();
            Assert.notNull(resources, "No LoopResources: is ReactorResourceFactory not initialized yet?");
            server = ((HttpServer)server.runOn(resources)).bindAddress(this::getListenAddress);
        } else {
            server = server.bindAddress(this::getListenAddress);
        }

        if (this.getSsl() != null && this.getSsl().isEnabled()) {
            server = this.customizeSslConfiguration(server);
        }


        server = server.protocol(this.listProtocols()).forwarded(this.useForwardHeaders);
        return applyCustomizers(server);
    }

    private HttpServer customizeSslConfiguration(HttpServer httpServer) {
        SslServerCustomizer sslServerCustomizer = new SslServerCustomizer(this.getSsl(),
            this.getHttp2(), this.getSslStoreProvider());
        return sslServerCustomizer.apply(httpServer);
    }

    private HttpProtocol[] listProtocols() {
        List<HttpProtocol> protocols = new ArrayList();
        protocols.add(HttpProtocol.HTTP11);
        if (this.getHttp2() != null && this.getHttp2().isEnabled()) {
            if (this.getSsl() != null && this.getSsl().isEnabled()) {
                protocols.add(HttpProtocol.H2);
            } else {
                protocols.add(HttpProtocol.H2C);
            }
        }

        return (HttpProtocol[]) protocols.toArray(new HttpProtocol[0]);
    }

    private HttpServer applyCustomizers(HttpServer server) {
        NettyServerCustomizer customizer;
        for (Iterator var2 = this.serverCustomizers.iterator(); var2.hasNext(); server = (HttpServer) customizer
            .apply(server)) {
            customizer = (NettyServerCustomizer) var2.next();
        }

        return server;
    }

    private InetSocketAddress getListenAddress() {
        return this.getAddress() != null ? new InetSocketAddress(
            this.getAddress().getHostAddress(), this.getPort()) : new InetSocketAddress(
            this.getPort());
    }

}
