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
import com.alipay.sofa.ark.springboot.web.ArkTomcatServletWebServerFactory;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.UpgradeProtocol;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.Servlet;
import java.util.stream.Collectors;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Configuration
@ConditionalOnArkEnabled
@ConditionalOnClass(ServletWebServerFactoryAutoConfiguration.class)
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
public class ArkServletAutoConfiguration {

    @Configuration
    @ConditionalOnClass(value = { Servlet.class, Tomcat.class, UpgradeProtocol.class,
            ServletWebServerFactory.class }, name = { "com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader" })
    @ConditionalOnMissingBean(value = ServletWebServerFactory.class, search = SearchStrategy.CURRENT)
    public static class EmbeddedArkTomcat {

        @Bean
        @ConditionalOnMissingBean(ArkTomcatServletWebServerFactory.class)
        public TomcatServletWebServerFactory tomcatServletWebServerFactory(ObjectProvider<TomcatConnectorCustomizer> connectorCustomizers,
                                                                           ObjectProvider<TomcatContextCustomizer> contextCustomizers,
                                                                           ObjectProvider<TomcatProtocolHandlerCustomizer<?>> protocolHandlerCustomizers) {
            ArkTomcatServletWebServerFactory factory = new ArkTomcatServletWebServerFactory();
            factory.getTomcatConnectorCustomizers().addAll(
                connectorCustomizers.orderedStream().collect(Collectors.toList()));
            factory.getTomcatContextCustomizers().addAll(
                contextCustomizers.orderedStream().collect(Collectors.toList()));
            factory.getTomcatProtocolHandlerCustomizers().addAll(
                protocolHandlerCustomizers.orderedStream().collect(Collectors.toList()));
            return factory;
        }

    }
}
