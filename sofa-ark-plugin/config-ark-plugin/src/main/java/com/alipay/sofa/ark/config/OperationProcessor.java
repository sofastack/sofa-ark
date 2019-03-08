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
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.model.BizOperation;
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
                        clientResponses.add(ArkClient.installOperation(bizOperation));
                        break;
                    case UNINSTALL:
                        clientResponses.add(ArkClient.uninstallOperation(bizOperation));
                        break;
                    case SWITCH:
                        clientResponses.add(ArkClient.switchOperation(bizOperation));
                        break;
                    case CHECK:
                        clientResponses.add(ArkClient.checkOperation(bizOperation));
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
}