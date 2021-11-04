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
import com.alipay.sofa.ark.bootstrap.EmbedArkLauncher;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.support.startup.SofaArkBootstrap;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * Ark Spring boot starter when run on ide
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkApplicationStartListener implements ApplicationListener<SpringApplicationEvent> {

    private static final String LAUNCH_CLASSLOADER_NAME = "sun.misc.Launcher$AppClassLoader";
    private static final String APPLICATION_STARTED_EVENT = "org.springframework.boot.context.event.ApplicationStartedEvent";
    private static final String APPLICATION_STARTING_EVENT = "org.springframework.boot.context.event.ApplicationStartingEvent";

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        try {
            if (isEnableEmbed()) {
                handleEmbedArk(event);
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

    public void startUpArk(SpringApplicationEvent event) {
        if (LAUNCH_CLASSLOADER_NAME.equals(this.getClass().getClassLoader().getClass().getName())) {
            SofaArkBootstrap.launch(event.getArgs());
        }
    }

    public boolean isSpringBoot1() {
        return SpringBootVersion.getVersion().startsWith("1");
    }

    public boolean isSpringBoot2() {
        return SpringBootVersion.getVersion().startsWith("2");
    }

    protected void handleEmbedArk(SpringApplicationEvent event) throws Exception {
        if (this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()) {
            if ("true".equals(System.getProperty("spring.application.admin.enabled"))) {
                System.setProperty("spring.application.admin.enabled", "false");
            }
            return;
        }
        if (event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPrepare((ApplicationEnvironmentPreparedEvent) event);
        }
        if (event instanceof ApplicationReadyEvent) {
            onApplicationReady((ApplicationReadyEvent) event);
        }
    }

    protected void onApplicationEnvironmentPrepare(ApplicationEnvironmentPreparedEvent preparedEvent)
            throws Exception {
        Environment environment = preparedEvent.getEnvironment();
        getOrSetDefault(MASTER_BIZ,
                environment.getProperty(MASTER_BIZ, environment.getProperty("spring.application.name")));
        getOrSetDefault(BIZ_CLASS_LOADER_HOOK_DIR,
                environment.getProperty(BIZ_CLASS_LOADER_HOOK_DIR));
        getOrSetDefault(EXPLODED_ENABLE, environment.getProperty(EXPLODED_ENABLE, "true"));
        getOrSetDefault(PLUGIN_EXPORT_CLASS_ENABLE,
                environment.getProperty(PLUGIN_EXPORT_CLASS_ENABLE, "false"));
        EmbedArkLauncher.main(new String[]{});
    }

    public void onApplicationReady(ApplicationReadyEvent event) {
        if (isMasterBizReady()) {
            ArkClient.getEventAdminService().sendEvent(
                    new AfterBizStartupEvent(ArkClient.getMasterBiz()));
        }
    }

    protected boolean isEnableEmbed() {
        return "true".equals(System.getProperty(Constants.EMBED_ENABLE));
    }

    protected boolean isMasterBizReady() {
        return ArkClient.getEventAdminService() != null && ArkClient.getMasterBiz() != null;
    }

    private static void getOrSetDefault(String key, String value) {
        if (System.getProperty(key) == null && value != null) {
            System.setProperty(key, value);
        }
    }
}