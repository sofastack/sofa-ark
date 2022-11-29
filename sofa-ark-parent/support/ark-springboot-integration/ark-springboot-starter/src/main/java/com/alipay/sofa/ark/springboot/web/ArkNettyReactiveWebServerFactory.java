package com.alipay.sofa.ark.springboot.web;

import com.alipay.sofa.ark.bootstrap.ClasspathLauncher;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
import org.springframework.boot.web.embedded.netty.*;
import org.springframework.boot.web.server.Shutdown;
import org.springframework.boot.web.server.WebServer;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.resources.LoopResources;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executor;

public class ArkNettyReactiveWebServerFactory extends NettyReactiveWebServerFactory {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private Duration lifecycleTimeout;

    private List<NettyRouteProvider> routeProviders = new ArrayList();
    @ArkInject
    private EmbeddedServerService embeddedNettyService;

    @ArkInject
    private BizManagerService bizManagerService;

    private boolean useForwardHeaders;

    private ReactorResourceFactory resourceFactory;

    private int                           backgroundProcessorDelay;
    private Set<NettyServerCustomizer> serverCustomizers = new LinkedHashSet();

    @Override
    public WebServer getWebServer(HttpHandler httpHandler) {
        if(embeddedNettyService == null){
            return super.getWebServer(httpHandler);
        }else if(embeddedNettyService.getEmbedServer() == null){
            embeddedNettyService.setEmbedServer(initEmbedNetty());
        }
        HttpServer httpServer = (HttpServer) embeddedNettyService.getEmbedServer();
        ReactorHttpHandlerAdapter handlerAdapter = new ReactorHttpHandlerAdapter(httpHandler);
        ArkNettyWebServer webServer = (ArkNettyWebServer) createNettyWebServer(httpServer, handlerAdapter, lifecycleTimeout);
        webServer.setRouteProviders(this.routeProviders);

        return webServer;
    }




    WebServer createNettyWebServer(HttpServer httpServer, ReactorHttpHandlerAdapter handlerAdapter, Duration lifecycleTimeout) {
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
        SslServerCustomizer sslServerCustomizer = new SslServerCustomizer(this.getSsl(), this.getHttp2(), this.getSslStoreProvider());
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

        return (HttpProtocol[])protocols.toArray(new HttpProtocol[0]);
    }

    private HttpServer applyCustomizers(HttpServer server) {
        NettyServerCustomizer customizer;
        for(Iterator var2 = this.serverCustomizers.iterator(); var2.hasNext(); server = (HttpServer)customizer.apply(server)) {
            customizer = (NettyServerCustomizer)var2.next();
        }

        return server;
    }

    private InetSocketAddress getListenAddress() {
        return this.getAddress() != null ? new InetSocketAddress(this.getAddress().getHostAddress(), this.getPort()) : new InetSocketAddress(this.getPort());
    }







}
