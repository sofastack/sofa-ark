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
package com.alipay.sofa.ark.springboot;

import com.alipay.sofa.ark.ide.startup.SofaArkBootstrap;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

/**
 *
 * Ark Spring boot starter when run on ide
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkApplicationStartListener implements ApplicationListener<ApplicationStartedEvent> {

    private static final String LAUNCH_CLASSLOADER_NAME = "sun.misc.Launcher$AppClassLoader";

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            if (shouldStartArk()) {
                SofaArkBootstrap.launch(event.getArgs());
            }
        } catch (Exception e) {
            throw new RuntimeException("Meet exception when determine whether to start sofa ark!",
                e);
        }

    }

    private boolean shouldStartArk() throws Exception {
        return LAUNCH_CLASSLOADER_NAME
            .equals(this.getClass().getClassLoader().getClass().getName());
    }
}