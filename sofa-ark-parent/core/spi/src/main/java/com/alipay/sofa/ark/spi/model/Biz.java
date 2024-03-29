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
package com.alipay.sofa.ark.spi.model;

import com.alipay.sofa.ark.spi.service.PriorityOrdered;

import java.net.URL;
import java.util.Map;

/**
 * Ark Biz Model Interface
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface Biz extends BizInfo, PriorityOrdered {
    /**
     * start Biz
     * @param args
     * @throws Throwable
     */
    void start(String[] args) throws Throwable;

    /**
     * start Biz with args and envs
     * @param args
     * @param envs
     * @throws Throwable
     */
    void start(String[] args, Map<String, String> envs) throws Throwable;

    /**
     * stop Biz
     * @throws Throwable
     */
    void stop() throws Throwable;

    /**
     * check resource whether declared in this biz classLoader.
     * @param url
     */
    boolean isDeclared(URL url, String resourceName);

    /**
     * check whether this biz is declared mode.
     * declared mode means this biz can only delegate declared class and resources
     * in the pom of this biz to other classloader like plugin or master Biz.
     * @return
     */
    boolean isDeclaredMode();

    /**
     * allow to dynamic update biz name
     * @param bizName
     */
    void setCustomBizName(String bizName);
}
