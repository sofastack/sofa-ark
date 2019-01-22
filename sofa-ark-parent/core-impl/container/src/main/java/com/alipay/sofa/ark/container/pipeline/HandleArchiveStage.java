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

import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * response to handle executable fat jar, parse plugin model and biz model from it
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Singleton
public class HandleArchiveStage implements PipelineStage {

    @Inject
    private PluginManagerService pluginManagerService;

    @Inject
    private PluginFactoryService pluginFactoryService;

    @Inject
    private BizManagerService    bizManagerService;

    @Inject
    private BizFactoryService    bizFactoryService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkException {
        try {
            ExecutableArchive executableArchive = pipelineContext.getExecutableArchive();

            for (PluginArchive pluginArchive : executableArchive.getPluginArchives()) {
                pluginManagerService.registerPlugin(pluginFactoryService
                    .createPlugin(pluginArchive));
            }

            for (BizArchive bizArchive : executableArchive.getBizArchives()) {
                bizManagerService.registerBiz(bizFactoryService.createBiz(bizArchive));
            }
        } catch (Throwable ex) {
            throw new ArkException(ex.getMessage(), ex);
        }
    }

}