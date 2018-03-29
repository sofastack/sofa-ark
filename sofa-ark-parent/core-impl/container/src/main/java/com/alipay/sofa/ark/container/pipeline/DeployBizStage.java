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
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Pipeline Stage to Deploy Biz
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class DeployBizStage implements PipelineStage {

    @Inject
    private BizManagerService      bizManagerService;

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Override
    public void process(PipelineContext pipelineContext) throws ArkException {

        if (pipelineContext.getLaunchCommand().isTestMode()) {
            return;
        }

        for (Biz biz : bizManagerService.getBizsInOrder()) {
            try {
                LOGGER.info(String.format("Begin to start biz: %s", biz.getBizName()));
                biz.start(pipelineContext.getLaunchCommand().getLaunchArgs());
                LOGGER.info(String.format("Finish to start biz: %s", biz.getBizName()));
            } catch (Exception e) {
                LOGGER.error(String.format("Start biz: %s meet error", biz.getBizName()), e);
                throw new ArkException(e);
            }
        }
    }
}