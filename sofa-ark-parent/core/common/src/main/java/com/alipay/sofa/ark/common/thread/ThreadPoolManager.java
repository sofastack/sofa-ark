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