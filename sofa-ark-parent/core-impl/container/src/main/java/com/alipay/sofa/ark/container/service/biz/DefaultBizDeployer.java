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
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizDeployer;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;

/**
 * Biz Deployer to deploy Biz
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class DefaultBizDeployer implements BizDeployer {

    @ArkInject
    private BizManagerService      bizManagerService;

    private String[]               arguments;

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Override
    public void init(String[] args) {
        this.arguments = args;
    }

    @Override
    public void deploy() {
        for (Biz biz : bizManagerService.getBizInOrder()) {
            try {
                LOGGER.info(String.format("Begin to start biz: %s", biz.getBizName()));
                biz.start(arguments);
                LOGGER.info(String.format("Finish to start biz: %s", biz.getBizName()));
            } catch (Throwable e) {
                LOGGER.error(String.format("Start biz: %s meet error", biz.getBizName()), e);
                throw new ArkException(e);
            }
        }
    }

    @Override
    public void unDeploy() {
        for (Biz biz : bizManagerService.getBizInOrder()) {
            try {
                LOGGER.info(String.format("Begin to stop biz: %s", biz.getBizName()));
                biz.stop();
                LOGGER.info(String.format("Finish to stop biz: %s", biz.getBizName()));
            } catch (Throwable e) {
                LOGGER.error(String.format("stop biz: %s meet error", biz.getBizName()), e);
                throw new ArkException(e);
            }
        }
    }

    @Override
    public String getDesc() {
        return String.format("{name=\'%s\', provider=\'%s\'}", "DefaultBizDeployer",
            "Ark Container");
    }

}