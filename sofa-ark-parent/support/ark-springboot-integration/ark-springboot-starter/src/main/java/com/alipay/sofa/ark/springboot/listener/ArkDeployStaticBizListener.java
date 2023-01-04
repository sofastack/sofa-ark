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

public class ArkDeployStaticBizListener implements ApplicationListener<ApplicationContextEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 仅监听基座启动时的Event
        if (this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()) {
            return;
        }
        if (ArkConfigs.isEmbedEnable()) {
            if (event instanceof ContextRefreshedEvent) {
                // 基座启动后，静态合并部署biz
                EmbedSofaArkBootstrap.deployStaticBizAfterEmbedMasterBizStarted();
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 10;
    }
}
