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
package com.alipay.sofa.ark.container.pipeline;

import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.biz.BizCommandProvider;
import com.alipay.sofa.ark.container.service.biz.DefaultBizDeployer;
import com.alipay.sofa.ark.container.service.plugin.PluginCommandProvider;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.biz.BizDeployer;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_COMMAND_UNIQUE_ID;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_COMMAND_UNIQUE_ID;

/**
 * Handle service contained in {@link com.alipay.sofa.ark.spi.service.registry.RegistryService},
 * mainly including registering service provided by ark container and service initialization
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class RegisterServiceStage implements PipelineStage {

    @Inject
    private RegistryService registryService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkRuntimeException {
        registryDefaultService();
    }

    /**
     * Registry some default service
     */
    private void registryDefaultService() {
        /**
         * some basic container service is not allowed to be override,  they are only published
         * to be referenced by plugin and biz, even depended by other container service.
         */
        registryService.publishService(BizManagerService.class, ArkServiceContainerHolder
            .getContainer().getService(BizManagerService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(BizFactoryService.class, ArkServiceContainerHolder
            .getContainer().getService(BizFactoryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginManagerService.class, ArkServiceContainerHolder
            .getContainer().getService(PluginManagerService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginFactoryService.class, ArkServiceContainerHolder
            .getContainer().getService(PluginFactoryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(EventAdminService.class, ArkServiceContainerHolder
            .getContainer().getService(EventAdminService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(RegistryService.class, ArkServiceContainerHolder
            .getContainer().getService(RegistryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));

        /**
         * some container service which may depends on other basic container service.
         */
        registryService.publishService(BizDeployer.class, new DefaultBizDeployer(),
            new ContainerServiceProvider());
        registryService.publishService(CommandProvider.class, new PluginCommandProvider(),
            PLUGIN_COMMAND_UNIQUE_ID, new ContainerServiceProvider());
        registryService.publishService(CommandProvider.class, new BizCommandProvider(),
            BIZ_COMMAND_UNIQUE_ID, new ContainerServiceProvider());
    }

}