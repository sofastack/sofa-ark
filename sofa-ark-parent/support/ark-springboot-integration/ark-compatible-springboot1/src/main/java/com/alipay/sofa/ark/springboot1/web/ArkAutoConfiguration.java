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
package com.alipay.sofa.ark.springboot1.web;

import com.alipay.sofa.ark.springboot.condition.ConditionalOnArkEnabled;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.UpgradeProtocol;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.web.EmbeddedServletContainerAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Servlet;

/**
 * @author qixiaobo
 * @since 2.2.4
 */
@Configuration
@ConditionalOnArkEnabled
@ConditionalOnClass(EmbeddedServletContainerAutoConfiguration.class)
@AutoConfigureBefore(EmbeddedServletContainerAutoConfiguration.class)
public class ArkAutoConfiguration {
    @Configuration
    @ConditionalOnClass(value = { Servlet.class, Tomcat.class, UpgradeProtocol.class,
            ArkTomcatEmbeddedServletContainerFactory.class }, name = {
            "com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader",
            "org.springframework.boot.context.embedded.EmbeddedServletContainerFactory" })
    @ConditionalOnMissingBean(value = { EmbeddedServletContainerFactory.class }, search = SearchStrategy.CURRENT)
    public static class EmbeddedArkTomcat {
        @Bean
        @ConditionalOnMissingBean(ArkTomcatEmbeddedServletContainerFactory.class)
        public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
            return new ArkTomcatEmbeddedServletContainerFactory();
        }

    }
}
