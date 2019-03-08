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

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.config.util.OperationTransformer;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;

import java.util.Deque;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ConfigProcessor {
    private final static ArkLogger LOGGER = ArkLoggerFactory.getLogger(ConfigProcessor.class);

    private Deque<String>          configDeque;
    private CommonThreadPool       commonThreadPool;
    private PluginContext          pluginContext;

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

    public boolean isReadyProcessConfig() {
        BizManagerService bizManagerService = pluginContext.referenceService(
            BizManagerService.class).getService();
        for (Biz biz : bizManagerService.getBizInOrder()) {
            if (biz.getBizState() != BizState.ACTIVATED
                && biz.getBizState() != BizState.DEACTIVATED) {
                return false;
            }
        }
        return true;
    }

    class ConfigTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (!isReadyProcessConfig()) {
                    sleep(200);
                    continue;
                }
                String config = configDeque.poll();
                if (config == null) {
                    sleep(200);
                    continue;
                }
                try {
                    LOGGER.info("ConfigTask: {} start to process config: {}",
                        commonThreadPool.getThreadPoolName(), config);
                    OperationProcessor.process(OperationTransformer.transformToBizOperation(config,
                        pluginContext));
                } catch (Throwable throwable) {
                    LOGGER.error(String.format("ConfigTask: %s failed to process config: %s",
                        commonThreadPool.getThreadPoolName(), config), throwable);
                }
            }
        }

        private void sleep(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}