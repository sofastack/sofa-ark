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
package com.alipay.sofa.ark.common.thread;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread Pool Manager
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class ThreadPoolManager {

    /**
     * Manager group of {@see CommonThreadPool}
     */
    private static ConcurrentHashMap<String, CommonThreadPool> threadPoolMap = null;

    /**
     * Register a thread pool for a unique name.
     *
     * @param threadPoolName thread pool name
     * @param commonThreadPool CommonThreadPool
     */
    public static synchronized void registerThreadPool(String threadPoolName,
                                                       CommonThreadPool commonThreadPool) {
        if (threadPoolMap == null) {
            threadPoolMap = new ConcurrentHashMap<>(16);
        }
        threadPoolMap.putIfAbsent(threadPoolName, commonThreadPool);
    }

    /**
     * un-register a thread pool for a given name
     *
     * @param threadPoolName thread pool name
     */
    public static synchronized void unRegisterUserThread(String threadPoolName) {
        if (threadPoolMap != null) {
            threadPoolMap.remove(threadPoolName);
        }
    }

    /**
     * Retrieve a thread pool for a given name
     *
     * @param threadPoolName thread pool name
     * @return
     */
    public static CommonThreadPool getThreadPool(String threadPoolName) {
        return threadPoolMap == null ? null : threadPoolMap.get(threadPoolName);
    }
}