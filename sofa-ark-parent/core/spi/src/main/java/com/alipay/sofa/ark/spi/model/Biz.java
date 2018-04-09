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

import com.alipay.sofa.ark.exception.ArkException;

import java.net.URL;
import java.util.Set;

/**
 * Ark Biz Model Interface
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface Biz {

    /**
     * get Biz Name
     * @return biz name
     */
    String getBizName();

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
     * get biz startup priority, priority is higher as the number is smaller
     *
     * @return biz startup priority
     */
    int getPriority();

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
     * @throws ArkException
     */
    void start(String[] args) throws ArkException;

    /**
     * stop Biz
     * @throws ArkException
     */
    void stop() throws ArkException;
}