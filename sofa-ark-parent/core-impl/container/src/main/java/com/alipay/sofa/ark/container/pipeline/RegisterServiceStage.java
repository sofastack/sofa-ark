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
import com.alipay.sofa.ark.container.service.biz.DefaultBizDeployer;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizDeployer;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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
    public void process(PipelineContext pipelineContext) throws ArkException {
        registryDefaultService();
    }

    /**
     * Registry some default service
     */
    private void registryDefaultService() {
        registryService.publishService(BizDeployer.class, new DefaultBizDeployer(),
            new ContainerServiceProvider());
    }

}