package com.alipay.sofa.ark.springboot;


import com.alipay.sofa.ark.springboot.condition.ConditionalOnArkEnabled;
import com.alipay.sofa.ark.springboot.processor.ArkEventHandlerProcessor;
import com.alipay.sofa.ark.springboot.processor.ArkServiceInjectProcessor;
import com.alipay.sofa.ark.springboot.web.ArkNettyReactiveWebServerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.ReactiveWebServerFactoryAutoConfiguration;
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
@AutoConfigureBefore(ReactiveWebServerFactoryAutoConfiguration.class)
public class ArkReactiveAutoConfigure {
    @Bean
    public static ArkServiceInjectProcessor serviceInjectProcessor() {
        return new ArkServiceInjectProcessor();
    }

    @Bean
    public static ArkEventHandlerProcessor arkEventHandlerProcessor() {
        return new ArkEventHandlerProcessor();
    }



    @Configuration
    @ConditionalOnMissingBean({ReactiveWebServerFactory.class})
    @ConditionalOnClass(value = {HttpServer.class},
            name =  {"com.alipay.sofa.ark.netty.ArkNettyIdentification"})
    static class EmbeddedNetty {
        EmbeddedNetty() {
        }
        @Bean
        @ConditionalOnMissingBean
        ReactorResourceFactory reactorServerResourceFactory() {
            return new ReactorResourceFactory();
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
