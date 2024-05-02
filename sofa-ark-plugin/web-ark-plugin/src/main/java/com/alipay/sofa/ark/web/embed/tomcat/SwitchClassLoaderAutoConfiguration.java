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

import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.UpgradeProtocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * switch classloader to bizClassLoader in pre of web request handler
 * fix https://github.com/koupleless/koupleless/issues/212
 *
 * please notice: this AutoConfiguration should been loaded by both base and biz,
 * so the class name should be different in this plugin
 *
 * @author lvjing2
 * @since 2.2.10
 */
@Configuration
public class SwitchClassLoaderAutoConfiguration {

    @Bean(name = "switchClassLoaderFilter")
    @Order(10)
    @ConditionalOnClass(value = { Servlet.class, Tomcat.class, UpgradeProtocol.class }, name = {
            "com.alipay.sofa.ark.springboot2.web.SwitchClassLoaderFilter",
            "com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader",
            "org.springframework.boot.web.servlet.server.ServletWebServerFactory",
            "org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration",
            "org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer" })
    public Filter switchClassLoaderFilter() {
        try {
            Class<?> clazz = Class
                .forName("com.alipay.sofa.ark.springboot2.web.SwitchClassLoaderFilter");
            return (Filter) clazz.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
