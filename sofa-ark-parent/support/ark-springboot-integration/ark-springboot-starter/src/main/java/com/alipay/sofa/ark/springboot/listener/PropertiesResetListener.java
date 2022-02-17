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
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

/**
 *  Listener to reset system properties
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class PropertiesResetListener implements
                                    ApplicationListener<ApplicationEnvironmentPreparedEvent>,
                                    Ordered {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (ArkConfigs.isEmbedEnable()) {
            System.getProperties().remove("logging.path");
        }
    }

    @Override
    public int getOrder() {
        //after ConfigFileApplicationListener
        return Ordered.HIGHEST_PRECEDENCE + 11;
    }
}
