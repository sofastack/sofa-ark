/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.spi.model.PluginContext;

import java.util.Deque;

/**
 * @author qilong.zql
 * @sicne 0.6.0
 */
public class ConfigProcessor {
    private Deque<String> configDeque;
    private CommonThreadPool commonThreadPool;
    private PluginContext pluginContext;

    public static ConfigProcessor createConfigProcessor(PluginContext pluginContext, Deque<String> deque, String processorName) {
        return new ConfigProcessor(pluginContext, deque, processorName);
    }

    public ConfigProcessor(PluginContext pluginContext, Deque configDeque, String processorName) {
        this.pluginContext = pluginContext;
        this.configDeque = configDeque;
        this.commonThreadPool = new CommonThreadPool().setThreadPoolName(processorName)
                                .setCorePoolSize(1).setMaximumPoolSize(1).setDaemon(true);
    }

    public void start() {
        commonThreadPool.getExecutor().execute(new ConfigTask());
    }

    class ConfigTask implements Runnable{
        @Override
        public void run() {
            while(true) {
                String config = configDeque.poll();
                if (config == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    continue;
                }
                handleConfig(config);
            }
        }

        protected void handleConfig(String config) {

        }
    }
}