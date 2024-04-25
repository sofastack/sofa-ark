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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 *
 * biz start in BizClassLoader, but web handler is resigtered in ArkTomcatEmbeddedWebappClassLoader
 * NOTE: fix https://github.com/koupleless/koupleless/issues/212
 *
 * @author lvjing2
 * @since 2.2.10
 */
public class SwitchClassLoaderFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader bizClassLoader = oldClassloader.getParent();
            if (bizClassLoader != null) {
                Thread.currentThread().setContextClassLoader(bizClassLoader);
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
