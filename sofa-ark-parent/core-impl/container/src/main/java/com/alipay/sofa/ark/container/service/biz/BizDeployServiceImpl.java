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
package com.alipay.sofa.ark.container.service.biz;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.biz.BizDeployService;
import com.alipay.sofa.ark.spi.service.biz.BizDeployer;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Service implementation to deploy Biz
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class BizDeployServiceImpl implements BizDeployService {

    @Inject
    private RegistryService registryService;

    private BizDeployer     bizDeployer;

    @Override
    public void deploy(String[] args) throws ArkRuntimeException {
        ServiceReference<BizDeployer> serviceReference = registryService
            .referenceService(BizDeployer.class);
        bizDeployer = serviceReference.getService();

        ArkLoggerFactory.getDefaultLogger().info(
            String.format("BizDeployer=\'%s\' is starting.", bizDeployer.getDesc()));

        bizDeployer.init(args);
        bizDeployer.deploy();
    }

    @Override
    public void unDeploy() throws ArkRuntimeException {
        if (bizDeployer != null) {
            ArkLoggerFactory.getDefaultLogger().info(
                String.format("BizDeployer=\'%s\' is stopping.", bizDeployer.getDesc()));
            bizDeployer.unDeploy();
        }
    }

    @Override
    public void init() throws ArkRuntimeException {
        // no action
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
