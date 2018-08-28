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

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.SpringApplicationEvent;
import com.alipay.sofa.common.log.Constants;
import org.springframework.context.ApplicationListener;

/**
 * Ark Spring boot starter when run on ide
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkApplicationStartListener implements ApplicationListener<SpringApplicationEvent> {

    private static final String LAUNCH_CLASSLOADER_NAME    = "sun.misc.Launcher$AppClassLoader";
    private static final String APPLICATION_STARTED_EVENT  = "org.springframework.boot.context.event.ApplicationStartedEvent";
    private static final String APPLICATION_STARTING_EVENT = "org.springframework.boot.context.event.ApplicationStartingEvent";
    private static final String INCOMPATIBLE_VERSION       = "2.0";

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        try {
            if (isSpringBoot2()
                && APPLICATION_STARTING_EVENT.equals(event.getClass().getCanonicalName())) {
                startUpArk(event);
            }

            if (isSpringBoot1()
                && APPLICATION_STARTED_EVENT.equals(event.getClass().getCanonicalName())) {
                startUpArk(event);
            }
        } catch (Throwable e) {
            throw new RuntimeException("Meet exception when determine whether to start SOFAArk!", e);
        }
    }

    public void startUpArk(SpringApplicationEvent event) {
        EnvironmentUtils.clearProperty(Constants.LOG_PATH);
        if (LAUNCH_CLASSLOADER_NAME.equals(this.getClass().getClassLoader().getClass().getName())) {
            SofaArkBootstrap.launch(event.getArgs());
        }
    }

    public boolean isSpringBoot2() {
        return INCOMPATIBLE_VERSION.compareTo(SpringBootVersion.getVersion()) < 0;
    }

    public boolean isSpringBoot1() {
        return INCOMPATIBLE_VERSION.compareTo(SpringBootVersion.getVersion()) > 0;
    }
}