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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.spi.model.PluginContext;

import java.util.Deque;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ConfigProcessor {
    private Deque<String>    configDeque;
    private CommonThreadPool commonThreadPool;
    private PluginContext    pluginContext;

    public static ConfigProcessor createConfigProcessor(PluginContext pluginContext,
                                                        Deque<String> deque, String processorName) {
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

    class ConfigTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                String config = configDeque.poll();
                if (config == null) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    continue;
                }
                OperationProcessor.process(ConfigUtils.transformToBizOperation(config,
                    pluginContext));
            }
        }

    }
}