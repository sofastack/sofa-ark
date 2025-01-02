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
package com.alipay.sofa.ark.springboot.listener;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicBoolean;

public class ArkDeployStaticBizListener implements ApplicationListener<ApplicationContextEvent>,
                                       Ordered {

    private final AtomicBoolean deployed = new AtomicBoolean(false);

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // Only listen to the event when the master biz is started
        if (this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()) {
            return;
        }
        if (ArkConfigs.isEmbedEnable() && ArkConfigs.isEmbedStaticBizEnable()) {
            if (event instanceof ContextRefreshedEvent && deployed.compareAndSet(false, true)) {
                // After the master biz is started, statically deploy the other biz from classpath
                EmbedSofaArkBootstrap.deployStaticBizAfterEmbedMasterBizStarted();
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 10;
    }
}
