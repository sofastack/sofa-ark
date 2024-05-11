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
package com.alipay.sofa.ark.springboot;

import com.alipay.sofa.ark.springboot.condition.ConditionalOnArkEnabled;
import com.alipay.sofa.ark.springboot.web.ArkNettyReactiveWebServerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyRouteProvider;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.reactive.server.ReactiveWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import reactor.netty.http.server.HttpServer;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnArkEnabled
@AutoConfigureBefore(name = { "org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration" })
public class ArkReactiveAutoConfiguration {

    @Configuration
    @ConditionalOnMissingBean({ ReactiveWebServerFactory.class })
    @ConditionalOnClass(value = { HttpServer.class }, name = { "com.alipay.sofa.ark.netty.ArkNettyIdentification" })
    static class EmbeddedNetty {
        EmbeddedNetty() {
        }

        @Bean
        @ConditionalOnMissingBean
        ReactorResourceFactory reactorServerResourceFactory() {
            ReactorResourceFactory reactorResourceFactory = new ReactorResourceFactory();
            reactorResourceFactory.setUseGlobalResources(false);
            return reactorResourceFactory;
        }

        @Bean
        NettyReactiveWebServerFactory nettyReactiveWebServerFactory(ReactorResourceFactory resourceFactory, ObjectProvider<NettyRouteProvider> routes, ObjectProvider<NettyServerCustomizer> serverCustomizers) {
            NettyReactiveWebServerFactory serverFactory = new ArkNettyReactiveWebServerFactory();
            serverFactory.setResourceFactory(resourceFactory);
            routes.orderedStream().forEach((xva$0) -> {
                serverFactory.addRouteProviders(new NettyRouteProvider[]{xva$0});
            });
            serverFactory.getServerCustomizers().addAll((Collection)serverCustomizers.orderedStream().collect(Collectors.toList()));
            return serverFactory;
        }
    }
}
