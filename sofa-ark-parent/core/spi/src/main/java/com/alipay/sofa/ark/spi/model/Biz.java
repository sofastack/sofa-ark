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
import java.util.Set;

/**
 * Ark Biz Model Interface
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface Biz extends PriorityOrdered {

    /**
     * get Biz Name
     * @return biz name
     */
    String getBizName();

    /**
     * get Biz Version
     */
    String getBizVersion();

    /**
     * get identity id in runtime, an unique-id of ark biz
     * @return
     */
    String getIdentity();

    /**
     * get Biz Main Entry Class Name
     * @return main class name
     */
    String getMainClass();

    /**
     * get Biz Class Path
     * @return biz classpath
     */
    URL[] getClassPath();

    /**
     * get denied imported packages
     * @return
     */
    Set<String> getDenyImportPackages();

    /**
     * get denied imported classes
     * @return
     */
    Set<String> getDenyImportClasses();

    /**
     * get denied imported resources
     * @return
     */
    Set<String> getDenyImportResources();

    /**
     * get Biz Classloader
     * @return biz classloader
     */
    ClassLoader getBizClassLoader();

    /**
     * start Biz
     * @throws Throwable
     */
    void start(String[] args) throws Throwable;

    /**
     * stop Biz
     * @throws Throwable
     */
    void stop() throws Throwable;

    /**
     * get Biz State
     */
    BizState getBizState();
}