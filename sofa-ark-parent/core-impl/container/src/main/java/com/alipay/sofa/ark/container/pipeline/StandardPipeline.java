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

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.pipeline.Pipeline;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard Pipeline Implementation
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class StandardPipeline implements Pipeline {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();
    private List<PipelineStage>    stages = new ArrayList<>();

    public StandardPipeline() {
        initializePipeline();
    }

    private void initializePipeline() {
        addPipelineStage(
            ArkServiceContainerHolder.getContainer().getService(HandleArchiveStage.class))
            .addPipelineStage(
                ArkServiceContainerHolder.getContainer().getService(
                    SystemPropertiesSettingStage.class))
            .addPipelineStage(
                ArkServiceContainerHolder.getContainer().getService(RegisterServiceStage.class))
            .addPipelineStage(
                ArkServiceContainerHolder.getContainer().getService(DeployPluginStage.class))
            .addPipelineStage(
                ArkServiceContainerHolder.getContainer().getService(DeployBizStage.class));
    }

    @Override
    public Pipeline addPipelineStage(PipelineStage pipelineStage) {
        stages.add(pipelineStage);
        return this;
    }

    @Override
    public void process(PipelineContext pipelineContext) throws ArkException {
        for (PipelineStage pipelineStage : stages) {
            try {
                LOGGER.info(String.format("Start to process pipeline stage: %s", pipelineStage
                    .getClass().getName()));
                pipelineStage.process(pipelineContext);
                LOGGER.info(String.format("Finish to process pipeline stage: %s", pipelineStage
                    .getClass().getName()));
            } catch (Throwable e) {
                LOGGER.error(String.format("Process pipeline stage fail: %s", pipelineStage
                    .getClass().getName()), e);
                throw new ArkException(e);
            }
        }
    }
}