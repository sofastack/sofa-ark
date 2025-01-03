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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AfterFinishDeployEvent;
import com.alipay.sofa.ark.spi.event.AfterFinishStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.support.common.MasterBizEnvironmentHolder;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * Ark Spring boot starter when run on ide
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkApplicationStartListener implements ApplicationListener<SpringApplicationEvent> {

    private static final String LAUNCH_CLASSLOADER_NAME    = "sun.misc.Launcher$AppClassLoader";
    private static final String SPRING_BOOT_LOADER         = "org.springframework.boot.loader.LaunchedURLClassLoader";
    //SpringBoot 3.2.0 support
    private static final String SPRING_BOOT_NEW_LOADER     = "org.springframework.boot.loader.launch.LaunchedClassLoader";
    private static final String APPLICATION_STARTED_EVENT  = "org.springframework.boot.context.event.ApplicationStartedEvent";
    private static final String APPLICATION_STARTING_EVENT = "org.springframework.boot.context.event.ApplicationStartingEvent";
    private static Class<?>     SPRING_BOOT_LOADER_CLASS;
    private static Class<?>     SPRING_BOOT_NEW_LOADER_CLASS;

    static {
        try {
            SPRING_BOOT_LOADER_CLASS = ApplicationListener.class.getClassLoader().loadClass(
                SPRING_BOOT_LOADER);
        } catch (Throwable t) {
            // ignore
        }
        try {
            SPRING_BOOT_NEW_LOADER_CLASS = ApplicationListener.class.getClassLoader().loadClass(
                SPRING_BOOT_NEW_LOADER);
        } catch (Throwable t) {
            // ignore
        }
    }

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        try {
            if (isEmbedEnable()) {
                ArkConfigs.setEmbedEnable(true);
                startUpArkEmbed(event);
                return;
            }
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

    private boolean isEmbedEnable() {
        if (ArkConfigs.isEmbedEnable()) {
            return true;
        }
        if (SPRING_BOOT_LOADER_CLASS != null
            && SPRING_BOOT_LOADER_CLASS.isAssignableFrom(this.getClass().getClassLoader()
                .getClass())) {
            return true;
        }
        if (SPRING_BOOT_NEW_LOADER_CLASS != null
            && SPRING_BOOT_NEW_LOADER_CLASS.isAssignableFrom(this.getClass().getClassLoader()
                .getClass())) {
            return true;
        }
        return false;
    }

    public void startUpArk(SpringApplicationEvent event) {
        if (LAUNCH_CLASSLOADER_NAME.equals(this.getClass().getClassLoader().getClass().getName())) {
            SofaArkBootstrap.launch(event.getArgs());
        }
    }

    public boolean isSpringBoot1() {
        String version = SpringBootVersion.getVersion();
        return null == version ? false : version.startsWith("1");
    }

    public boolean isSpringBoot2() {
        String version = SpringBootVersion.getVersion();
        return null == version ? false : version.startsWith("2");
    }

    protected void startUpArkEmbed(SpringApplicationEvent event) {
        // 仅监听基座启动时的Event
        if (this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()) {
            return;
        }
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            ApplicationEnvironmentPreparedEvent preparedEvent = (ApplicationEnvironmentPreparedEvent) event;
            MasterBizEnvironmentHolder.setEnvironment(preparedEvent.getEnvironment());
            EmbedSofaArkBootstrap.launch(preparedEvent.getEnvironment());
        }
        if (event instanceof ApplicationReadyEvent) {
            // 基座启动后+静态合并部署的biz启动后，发送事件
            sendEventAfterArkEmbedStartupFinish();
        }
    }

    protected void sendEventAfterArkEmbedStartupFinish() {
        if (ArkClient.getEventAdminService() != null && ArkClient.getMasterBiz() != null) {
            ArkClient.getEventAdminService().sendEvent(
                new AfterBizStartupEvent(ArkClient.getMasterBiz()));
            ArkClient.getEventAdminService().sendEvent(new AfterFinishDeployEvent());
            ArkClient.getEventAdminService().sendEvent(new AfterFinishStartupEvent());
        }
    }
}