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
package com.alipay.sofa.ark.springboot2.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SwitchClassLoaderFilter implements Filter {

    /**
     * using user logger not ArkLogger, to print this log into user log dir
     */
    private Logger logger = LoggerFactory.getLogger(SwitchClassLoaderFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            if ("com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader"
                .equals(oldClassloader.getClass().getName())) {
                ClassLoader bizClassLoader = oldClassloader.getParent();
                if (bizClassLoader != null) {
                    logger.debug("switch web classLoader from {} to {}", oldClassloader,
                        bizClassLoader);
                    Thread.currentThread().setContextClassLoader(bizClassLoader);
                }
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
