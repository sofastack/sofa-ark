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

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.extension.ExtensionLoaderService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Singleton
public class ExtensionLoaderStage implements PipelineStage {

    @Inject
    private ExtensionLoaderService extensionLoaderService;

    @Inject
    private BizManagerService      bizManagerService;

    @Inject
    private PluginManagerService   pluginManagerService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkRuntimeException {

        ArkServiceLoader.setExtensionLoaderService(extensionLoaderService);

        List<Biz> bizList = bizManagerService.getBizInOrder();
        for (Biz biz : bizList) {
            ArkServiceLoader.loadExtension(biz.getIdentity(), ClassLoaderHook.class);
        }

        List<Plugin> pluginList = pluginManagerService.getPluginsInOrder();
        for (Plugin plugin : pluginList) {
            ArkServiceLoader.loadExtension(plugin.getPluginName(), ClassLoaderHook.class);
        }
    }
}