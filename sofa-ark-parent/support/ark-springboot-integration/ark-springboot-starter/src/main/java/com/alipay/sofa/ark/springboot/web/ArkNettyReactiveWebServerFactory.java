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
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import org.springframework.boot.ssl.SslBundle;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.alipay.sofa.ark.spi.constant.Constants.ROOT_WEB_CONTEXT_PATH;

public class ArkNettyReactiveWebServerFactory extends NettyReactiveWebServerFactory {
    private static final Charset                         DEFAULT_CHARSET   = StandardCharsets.UTF_8;
    private Duration                                     lifecycleTimeout;

    private List<NettyRouteProvider>                     routeProviders    = new ArrayList();
    @ArkInject
    private EmbeddedServerService                        embeddedNettyService;

    @ArkInject
    private BizManagerService                            bizManagerService;

    private boolean                                      useForwardHeaders;

    private ReactorResourceFactory                       resourceFactory;

    private int                                          backgroundProcessorDelay;
    private Set<NettyServerCustomizer>                   serverCustomizers = new LinkedHashSet();

    private static ArkCompositeReactorHttpHandlerAdapter adapter;

    public ArkNettyReactiveWebServerFactory() {
    }

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        if (embeddedNettyService == null && ArkClient.getInjectionService() != null) {
            // 非应用上下文 (例如: Spring Management Context) 没有经历 Start 生命周期, 不会被注入 ArkServiceInjectProcessor,
            // 因此 @ArkInject 没有被处理, 需要手动处理
            ArkClient.getInjectionService().inject(this);
        }
        if (embeddedNettyService == null) {
            // 原有的逻辑中也有这个空值判断, 不确定注入后是否还会有用例会导致 embeddedNettyService 为空
            // 因此仍保留此 if 空值判断
            return super.getWebServer(httpHandler);
        }
        if (embeddedNettyService.getEmbedServer(getPort()) == null) {
            embeddedNettyService.putEmbedServer(getPort(), initEmbedNetty());
        }

        String contextPath = getContextPath();
        Map<String, HttpHandler> handlerMap = new HashMap<>();
        handlerMap.put(contextPath, httpHandler);
        ContextPathCompositeHandler contextHandler = new ContextPathCompositeHandler(handlerMap);

        if (adapter == null) {
            adapter = new ArkCompositeReactorHttpHandlerAdapter(contextHandler);
        } else {
            adapter.registerBizReactorHttpHandlerAdapter(contextPath,
                new ReactorHttpHandlerAdapter(contextHandler));
        }

        HttpServer httpServer = (HttpServer) embeddedNettyService.getEmbedServer(getPort());
        ArkNettyWebServer webServer = (ArkNettyWebServer) createNettyWebServer(contextPath,
            httpServer, adapter, lifecycleTimeout);
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
            contextPath = biz.getWebContextPath();
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            return contextPath;
        } else {
            return ROOT_WEB_CONTEXT_PATH;
        }
    }

    WebServer createNettyWebServer(String contextPath, HttpServer httpServer,
                                   ReactorHttpHandlerAdapter handlerAdapter,
                                   Duration lifecycleTimeout) {
        return new ArkNettyWebServer(contextPath, httpServer, handlerAdapter, lifecycleTimeout);
    }

    private HttpServer  initEmbedNetty(){
        HttpServer server = HttpServer.create();
        if (this.resourceFactory != null) {
            LoopResources resources = this.resourceFactory.getLoopResources();
            Assert.notNull(resources, "No LoopResources: is ReactorResourceFactory not initialized yet?");
            server = server.tcpConfiguration((tcpServer) -> tcpServer.runOn(resources)).bindAddress(this::getListenAddress);
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

        SslServerCustomizer customizer = new SslServerCustomizer(this.getHttp2(), this.getSsl().getClientAuth(), this.getSslBundle());
        String bundleName = this.getSsl().getBundle();
        if (StringUtils.hasText(bundleName)) {
            try{
                // 找到类 SslServerCustomizer 的updateSslBundle方法
                Method updateSslBundleMethod = SslServerCustomizer.class.getDeclaredMethod("updateSslBundle", SslBundle.class);
                updateSslBundleMethod.setAccessible(true);

                // 使用反射创建Consumer<SslBundle>
                Consumer<SslBundle> consumer = sslBundle -> {
                    try {
                        // 通过反射调用updateSslBundle方法
                        updateSslBundleMethod.invoke(customizer, sslBundle);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
                this.getSslBundles().addBundleUpdateHandler(bundleName, consumer);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        return customizer.apply(httpServer);
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
