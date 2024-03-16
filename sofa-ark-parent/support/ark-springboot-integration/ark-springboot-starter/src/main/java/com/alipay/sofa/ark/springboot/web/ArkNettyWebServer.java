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

import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.unix.Errors;
import io.netty.util.concurrent.DefaultEventExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.boot.web.embedded.netty.NettyRouteProvider;
import org.springframework.boot.web.server.PortInUseException;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.util.Assert;
import reactor.netty.ChannelBindException;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ArkNettyWebServer implements WebServer {
    private static final Predicate<HttpServerRequest> ALWAYS = (request) -> {
        return true;
    };
    private static HttpServer arkHttpServer;
    private static final Log logger = LogFactory.getLog(ArkNettyWebServer.class);
    private final HttpServer httpServer;
    private final BiFunction<? super HttpServerRequest, ? super HttpServerResponse, ? extends Publisher<Void>> handler;
    private final Duration lifecycleTimeout;
    private List<NettyRouteProvider> routeProviders = Collections.emptyList();
    private static volatile DisposableServer disposableServer;
    private Thread awaitThread;
    private String contextPath;

    public ArkNettyWebServer(String contextPath, HttpServer httpServer, ReactorHttpHandlerAdapter handlerAdapter, Duration lifecycleTimeout) {
        Assert.notNull(httpServer, "HttpServer must not be null");
        Assert.notNull(handlerAdapter, "HandlerAdapter must not be null");
        this.contextPath = contextPath;
        this.lifecycleTimeout = lifecycleTimeout;
        this.handler = handlerAdapter;
        this.httpServer = httpServer.channelGroup(new DefaultChannelGroup(new DefaultEventExecutor()));
        if (arkHttpServer == null) {
            arkHttpServer = this.httpServer;
        }
    }

    public void setRouteProviders(List<NettyRouteProvider> routeProviders) {
        this.routeProviders = routeProviders;
    }

    @Override
    public void start() throws WebServerException {
        if (disposableServer == null) {
            try {
                disposableServer = this.startHttpServer();
            } catch (Exception var2) {
                PortInUseException.ifCausedBy(var2, ChannelBindException.class, (bindException) -> {
                    if (bindException.localPort() > 0 && !this.isPermissionDenied(bindException.getCause())) {
                        throw new PortInUseException(bindException.localPort(), var2);
                    }
                });
                throw new WebServerException("Unable to start Netty", var2);
            }

            this.startDaemonAwaitThread(disposableServer);
        }

        if (disposableServer != null) {
            logger.info("Netty started" + this.getStartedOnMessage(disposableServer) + " with context path " + contextPath);
        }
    }

    @Override
    public void stop() throws WebServerException {
        if (!(this.handler instanceof ArkCompositeReactorHttpHandlerAdapter)) {
            return;
        }

        ((ArkCompositeReactorHttpHandlerAdapter) this.handler).unregisterBizReactorHttpHandlerAdapter(contextPath);

        if (disposableServer != null && this.httpServer == arkHttpServer) {
            try {
                if (this.lifecycleTimeout != null) {
                    disposableServer.disposeNow(this.lifecycleTimeout);
                } else {
                    disposableServer.disposeNow();
                }
                awaitThread.stop();
            } catch (IllegalStateException ignore) {

            }

            logger.info("Netty stoped" + this.getStartedOnMessage(disposableServer));

            disposableServer = null;
        }

    }

    @Override
    public int getPort() {
        if (disposableServer != null) {
            try {
                return disposableServer.port();
            } catch (UnsupportedOperationException var2) {
                return -1;
            }
        } else {
            return -1;
        }
    }


    private String getStartedOnMessage(DisposableServer server) {
        StringBuilder message = new StringBuilder();
        this.tryAppend(message, "port %s", server::port);
        this.tryAppend(message, "host %s", server::host);
        return message.length() > 0 ? " on " + message : "";
    }

    private void tryAppend(StringBuilder message, String format, Supplier<Object> supplier) {
        try {
            Object value = supplier.get();
            message.append(message.length() != 0 ? " " : "");
            message.append(String.format(format, value));
        } catch (UnsupportedOperationException var5) {
        }

    }

    DisposableServer startHttpServer() {
        HttpServer server = this.httpServer;
        if (this.routeProviders.isEmpty()) {
            server = server.handle(this.handler);
        } else {
            server = server.route(this::applyRouteProviders);
        }

        return this.lifecycleTimeout != null ? server.bindNow(this.lifecycleTimeout) : server.bindNow();
    }

    private boolean isPermissionDenied(Throwable bindExceptionCause) {
        try {
            if (bindExceptionCause instanceof Errors.NativeIoException) {
                return ((Errors.NativeIoException)bindExceptionCause).expectedErr() == -13;
            }
        } catch (Throwable var3) {
        }

        return false;
    }

    private void applyRouteProviders(HttpServerRoutes routes) {
        NettyRouteProvider provider;
        for(Iterator var2 = this.routeProviders.iterator(); var2.hasNext(); routes = (HttpServerRoutes)provider.apply(routes)) {
            provider = (NettyRouteProvider)var2.next();
        }

        routes.route(ALWAYS, this.handler);
    }

    private void startDaemonAwaitThread(DisposableServer disposableServer) {
        awaitThread = new Thread("server") {
            public void run() {
                disposableServer.onDispose().block();
            }
        };
        awaitThread.setContextClassLoader(this.getClass().getClassLoader());
        awaitThread.setDaemon(false);
        awaitThread.start();
    }
}
