package com.alipay.sofa.ark.common.thread;

import com.alipay.sofa.ark.common.util.ThreadPoolUtils;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execute tasks triggered by ark container and ark plugin.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class CommonThreadPool {

    /**
     * Core size of thread pool
     *
     * @see ThreadPoolExecutor#corePoolSize
     */
    private int                           corePoolSize           = 10;

    /**
     * Maximum size of thread pool
     *
     * @see ThreadPoolExecutor#corePoolSize
     */
    private int                           maximumPoolSize        = 100;

    /**
     * @see ThreadPoolExecutor#keepAliveTime
     */
    private int                           keepAliveTime          = 300000;

    /**
     * @see ThreadPoolExecutor#getQueue
     */
    private int                           queueSize              = 0;

    /**
     * @see ThreadPoolExecutor#threadFactory#threadPoolName
     */
    private String                        threadPoolName         = "CommonProcessor";

    /**
     * @see ThreadPoolExecutor#threadFactory#isDaemon
     */
    private boolean                       isDaemon               = false;

    /**
     * @see ThreadPoolExecutor#allowCoreThreadTimeOut
     */
    private boolean                       allowCoreThreadTimeOut = false;

    /**
     * @see ThreadPoolExecutor#prestartAllCoreThreads
     */
    private boolean                       prestartAllCoreThreads = false;

    /**
     * ThreadPoolExecutor
     */
    transient volatile ThreadPoolExecutor executor;

    private void init() {
        executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
            TimeUnit.MILLISECONDS, ThreadPoolUtils.buildQueue(queueSize), new NamedThreadFactory(
                threadPoolName, isDaemon));
        if (allowCoreThreadTimeOut) {
            executor.allowCoreThreadTimeOut(true);
        }
        if (prestartAllCoreThreads) {
            executor.prestartAllCoreThreads();
        }
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public CommonThreadPool setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
        return this;
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public CommonThreadPool setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public CommonThreadPool setKeepAliveTime(int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        return this;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public CommonThreadPool setQueueSize(int queueSize) {
        this.queueSize = queueSize;
        return this;
    }

    public String getThreadPoolName() {
        return threadPoolName;
    }

    public CommonThreadPool setThreadPoolName(String threadPoolName) {
        this.threadPoolName = threadPoolName;
        return this;
    }

    public boolean isDaemon() {
        return isDaemon;
    }

    public CommonThreadPool setDaemon(boolean daemon) {
        isDaemon = daemon;
        return this;
    }

    public boolean isAllowCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    public CommonThreadPool setAllowCoreThreadTimeOut(boolean allowCoreThreadTimeOut) {
        this.allowCoreThreadTimeOut = allowCoreThreadTimeOut;
        return this;
    }

    public boolean isPrestartAllCoreThreads() {
        return prestartAllCoreThreads;
    }

    public CommonThreadPool setPrestartAllCoreThreads(boolean prestartAllCoreThreads) {
        this.prestartAllCoreThreads = prestartAllCoreThreads;
        return this;
    }

    /**
     * Gets executor
     *
     * @return the executor
     */
    public ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            synchronized (this) {
                if (executor == null) {
                    init();
                }
            }
        }
        return executor;
    }
}