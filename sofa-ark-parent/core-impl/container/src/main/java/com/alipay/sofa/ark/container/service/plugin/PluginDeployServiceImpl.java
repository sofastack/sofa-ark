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
package com.alipay.sofa.ark.container.service.plugin;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.container.model.PluginContextImpl;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Service Implementation to deploy ark plugin
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class PluginDeployServiceImpl implements PluginDeployService {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Inject
    PluginManagerService           pluginManagerService;

    @Override
    public void deploy() throws ArkRuntimeException {
        for (Plugin plugin : pluginManagerService.getPluginsInOrder()) {
            try {
                deployPlugin(plugin);
            } catch (ArkRuntimeException e) {
                LOGGER.error(String.format("Deploy plugin: %s meet error", plugin.getPluginName()),
                    e);
                throw e;
            }
        }
        if ("true".equals(System.getProperty(Constants.CONTAINER_EMBED_ENABLE))) {
            startActivator();
        }
    }

    private void deployPlugin(Plugin plugin) throws ArkRuntimeException {
        try {
            LOGGER.info(String.format("Start to deploy plugin: %s", plugin.getPluginName()));
            plugin.start();
            LOGGER.info(String.format("Finish to deploy plugin: %s", plugin.getPluginName()));
        } catch (ArkRuntimeException e) {
            LOGGER.error(String.format("Start plugin: %s meet error", plugin.getPluginName()), e);
            throw e;
        }
    }

    private void startActivator() throws ArkRuntimeException {
        ServiceLoader<PluginActivator> pluginActivators = ServiceLoader.load(PluginActivator.class,
            ArkClient.getMasterBiz().getBizClassLoader());
        if (!pluginActivators.iterator().hasNext()) {
            return;
        }
        for (PluginActivator pluginActivator : pluginActivators) {
            PluginModel plugin = new PluginModel().setPluginName(pluginActivator.getClass()
                .getName());
            PluginContext context = new PluginContextImpl(plugin);
            plugin.setPluginContext(context);
            try {
                LOGGER.info(String.format("Start to deploy mock plugin"));
                pluginActivator.start(context);
                LOGGER.info(String.format("Finish to deploy plugin: %s", plugin.getPluginName()));
            } catch (ArkRuntimeException e) {
                LOGGER.error(String.format("Start plugin: %s meet error", plugin.getPluginName()),
                    e);
                throw e;
            }
        }
    }

    @Override
    public void unDeploy() throws ArkRuntimeException {
        List<Plugin> pluginsInOrder = pluginManagerService.getPluginsInOrder();
        Collections.reverse(pluginsInOrder);
        for (Plugin plugin : pluginsInOrder) {
            try {
                unDeployPlugin(plugin);
            } catch (ArkRuntimeException e) {
                LOGGER.error(
                    String.format("UnDeploy plugin: %s meet error", plugin.getPluginName()), e);
                throw e;
            }
        }
    }

    private void unDeployPlugin(Plugin plugin) throws ArkRuntimeException {
        try {
            LOGGER.info(String.format("Start to unDeploy plugin: %s", plugin.getPluginName())
                        + plugin.getPluginName());
            plugin.stop();
            LOGGER.info(String.format("Stop to unDeploy plugin: %s", plugin.getPluginName())
                        + plugin.getPluginName());
        } catch (ArkRuntimeException e) {
            LOGGER.error(String.format("Stop plugin: %s meet error", plugin.getPluginName()), e);
            throw e;
        }
    }

    @Override
    public void init() throws ArkRuntimeException {

    }

    @Override
    public void dispose() throws ArkRuntimeException {
        unDeploy();
    }

    @Override
    public int getPriority() {
        return DEFAULT_PRECEDENCE;
    }
}