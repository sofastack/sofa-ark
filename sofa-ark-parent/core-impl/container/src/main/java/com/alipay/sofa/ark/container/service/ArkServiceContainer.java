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
package com.alipay.sofa.ark.container.service;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.guice.AbstractArkGuiceModule;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.service.ArkService;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ark Service Container
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkServiceContainer {

    private Injector         injector;

    private List<ArkService> arkServiceList = new ArrayList<>();

    private AtomicBoolean    started        = new AtomicBoolean(false);
    private AtomicBoolean    stopped        = new AtomicBoolean(false);

    private final String[]   arguments;

    public ArkServiceContainer(String[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Start Ark Service Container
     * @throws ArkRuntimeException
     * @since 0.1.0
     */
    public void start() throws ArkRuntimeException {
        if (started.compareAndSet(false, true)) {
            ClassLoader oldClassLoader = ClassLoaderUtils.pushContextClassLoader(getClass()
                .getClassLoader());
            try {
                ArkLoggerFactory.getDefaultLogger().info("Begin to start ArkServiceContainer");

                injector = Guice.createInjector(findServiceModules());
                for (Binding<ArkService> binding : injector
                    .findBindingsByType(new TypeLiteral<ArkService>() {
                    })) {
                    arkServiceList.add(binding.getProvider().get());
                }
                Collections.sort(arkServiceList, new OrderComparator());

                for (ArkService arkService : arkServiceList) {
                    ArkLoggerFactory.getDefaultLogger().info(
                        String.format("Init Service: %s", arkService.getClass().getName()));
                    arkService.init();
                }

                ArkServiceContainerHolder.setContainer(this);
                ArkClient.setBizFactoryService(getService(BizFactoryService.class));
                ArkClient.setBizManagerService(getService(BizManagerService.class));
                ArkClient.setInjectionService(getService(InjectionService.class));
                ArkClient.setEventAdminService(getService(EventAdminService.class));
                ArkClient.setPluginManagerService(getService(PluginManagerService.class));
                ArkClient.setPluginFactoryService(getService(PluginFactoryService.class));
                ArkClient.setArguments(arguments);
                ArkLoggerFactory.getDefaultLogger().info("Finish to start ArkServiceContainer");
            } finally {
                ClassLoaderUtils.popContextClassLoader(oldClassLoader);
            }

        }

    }

    private List<AbstractArkGuiceModule> findServiceModules() throws ArkRuntimeException {
        try {
            List<AbstractArkGuiceModule> modules = new ArrayList<>();
            for (AbstractArkGuiceModule module : ServiceLoader.load(AbstractArkGuiceModule.class)) {
                modules.add(module);
            }
            return modules;
        } catch (Throwable e) {
            throw new ArkRuntimeException(e);
        }
    }

    /**
     * Get Service from ArkService Container
     * @param clazz
     * @param <T>
     * @return
     * @since 0.1.0
     */
    public <T> T getService(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    /**
     * Stop Ark Service Container
     * @throws ArkRuntimeException
     * @since 0.1.0
     */
    public void stop() throws ArkRuntimeException {
        if (stopped.compareAndSet(false, true)) {
            ArkLoggerFactory.getDefaultLogger().info("Begin to stop ArkServiceContainer");

            ClassLoader oldClassLoader = ClassLoaderUtils.pushContextClassLoader(getClass()
                .getClassLoader());
            try {
                Collections.reverse(arkServiceList);
                for (ArkService arkService : arkServiceList) {
                    ArkLoggerFactory.getDefaultLogger().info(
                        String.format("Dispose service: %s", arkService.getClass().getName()));
                    arkService.dispose();
                }
                ArkLoggerFactory.getDefaultLogger().info("Finish to stop ArkServiceContainer");
            } finally {
                ClassLoaderUtils.popContextClassLoader(oldClassLoader);
            }
        }
    }

    /**
     * Whether Ark Service Container is started or not
     * @return
     * @since 0.1.0
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * Whether Ark Service Container is running or not
     * @return
     * @since 0.1.0
     */
    public boolean isRunning() {
        return isStarted() && !stopped.get();
    }

}
