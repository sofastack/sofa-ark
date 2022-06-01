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
package com.alipay.sofa.ark.springboot.util;

import com.alipay.sofa.ark.spi.model.Biz;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hanyue
 * @version : RootApplicationContextUtils.java, v 0.1 2022年05月30日 8:53 PM hanyue Exp $
 */
public class RootApplicationContextUtils implements ApplicationContextAware {

    private static final Map<ClassLoader, ApplicationContext> CACHE = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext)
                                                                                      throws BeansException {
        if (applicationContext.getClassLoader() != null) {
            CACHE.put(applicationContext.getClassLoader(), applicationContext);
        }
    }

    public static ApplicationContext getApplicationContext(Biz biz) {
        return getApplicationContext(biz.getBizClassLoader());
    }

    public static ApplicationContext getApplicationContext(ClassLoader classLoader) {
        return CACHE.get(classLoader);
    }
}