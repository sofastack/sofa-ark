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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.config.util.ConfigUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.service.biz.BizFileGenerator;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qilong.zq
 * @since 0.6.0
 */
public class OperationProcessor {

    private final static ArkLogger LOGGER = ArkLoggerFactory.getLogger(OperationProcessor.class);

    public static List<ClientResponse> process(List<BizOperation> bizOperations) {
        List<ClientResponse> clientResponses = new ArrayList<>();
        try {
            for (BizOperation bizOperation : bizOperations) {
                LOGGER.info("Execute biz operation: {} {}:{}", bizOperation.getOperationType()
                    .name(), bizOperation.getBizName(), bizOperation.getBizVersion());
                switch (bizOperation.getOperationType()) {
                    case INSTALL:
                        clientResponses.add(installOperation(bizOperation));
                        break;
                    case UNINSTALL:
                        clientResponses.add(uninstallOperation(bizOperation));
                        break;
                    case SWITCH:
                        clientResponses.add(switchOperation(bizOperation));
                        break;
                    case CHECK:
                        clientResponses.add(checkOperation(bizOperation));
                        break;
                    default:
                        throw new ArkRuntimeException(String.format("Don't support operation: %s.",
                            bizOperation.getOperationType()));
                }
            }
        } catch (Throwable throwable) {
            throw new ArkRuntimeException("Failed to execute biz operations.", throwable);
        }
        return clientResponses;
    }

    public static ClientResponse installOperation(BizOperation bizOperation) throws Throwable {
        AssertUtils.isTrue(
            BizOperation.OperationType.INSTALL.equals(bizOperation.getOperationType()),
            "Operation type must be install");
        File bizFile = null;
        if (bizOperation.getParameters().get(Constants.CONFIG_BIZ_URL) != null) {
            URL url = new URL(bizOperation.getParameters().get(Constants.CONFIG_BIZ_URL));
            bizFile = ConfigUtils.createBizSaveFile(bizOperation.getBizName(),
                bizOperation.getBizVersion());
            FileUtils.copyInputStreamToFile(url.openStream(), bizFile);
        }
        for (BizFileGenerator bizFileGenerator : ArkServiceLoader
            .loadExtension(BizFileGenerator.class)) {
            bizFile = bizFileGenerator.createBizFile(bizOperation.getBizName(),
                bizOperation.getBizVersion());
            if (bizFile != null && bizFile.exists()) {
                break;
            }
        }
        return ArkClient.installBiz(bizFile);
    }

    public static ClientResponse uninstallOperation(BizOperation bizOperation) throws Throwable {
        AssertUtils.isTrue(
            BizOperation.OperationType.UNINSTALL.equals(bizOperation.getOperationType()),
            "Operation type must be uninstall");
        return ArkClient.uninstallBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

    public static ClientResponse switchOperation(BizOperation bizOperation) {
        AssertUtils.isTrue(
            BizOperation.OperationType.SWITCH.equals(bizOperation.getOperationType()),
            "Operation type must be switch");
        return ArkClient.switchBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

    public static ClientResponse checkOperation(BizOperation bizOperation) {
        AssertUtils.isTrue(
            BizOperation.OperationType.SWITCH.equals(bizOperation.getOperationType()),
            "Operation type must be switch");
        return ArkClient.checkBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

}