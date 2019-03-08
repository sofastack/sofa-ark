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
package com.alipay.sofa.ark.config.util;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class OperationTransformer {

    /**
     * transform config into biz operation
     *
     * @param config
     * @return
     */
    public static List<BizOperation> transformToBizOperation(String config,
                                                             PluginContext pluginContext)
                                                                                         throws IllegalStateException {
        BizManagerService bizManagerService = pluginContext.referenceService(
            BizManagerService.class).getService();
        Map<String, Map<String, BizState>> currentBizState = new HashMap<>();

        for (Biz biz : bizManagerService.getBizInOrder()) {
            if (!BizState.ACTIVATED.equals(biz.getBizState())
                && !BizState.DEACTIVATED.equals(biz.getBizState())) {
                throw new IllegalStateException(String.format(
                    "Exist illegal biz: %s, please wait.", biz));
            }
            if (biz.getBizName().equals(ArkConfigs.getStringValue(Constants.MASTER_BIZ))) {
                continue;
            } else if (currentBizState.get(biz.getBizName()) == null) {
                currentBizState.put(biz.getBizName(), new HashMap<String, BizState>());
            }
            currentBizState.get(biz.getBizName()).put(biz.getBizVersion(), biz.getBizState());
        }
        return doTransformToBizOperation(config, currentBizState);
    }

    public static List<BizOperation> doTransformToBizOperation(String config,
                                                               Map<String, Map<String, BizState>> currentBizState)
                                                                                                                  throws IllegalStateException {
        List<BizOperation> bizOperations = new ArrayList<>();
        Map<String, Map<String, BizState>> expectedBizState = new HashMap<>();
        Map<String, Map<String, BizState>> progressBizState = cloneBizStateMap(currentBizState);
        ArrayList<String> configOperation = adjustOperationOrder(config);

        for (String operation : configOperation) {
            int idx = operation.indexOf(Constants.QUESTION_MARK_SPLIT);
            String[] configInfo = (idx == -1) ? operation.split(Constants.STRING_COLON) : operation
                .substring(0, idx).split(Constants.STRING_COLON);
            String bizName = configInfo[0];
            String bizVersion = configInfo[1];
            String stateStr = configInfo[2];
            String parameterStr = (idx == -1) ? Constants.EMPTY_STR : operation.substring(idx + 1);
            BizState bizState = BizState.of(stateStr);
            Map<String, String> parameters = parseParameter(parameterStr);

            if (expectedBizState.get(bizName) != null
                && expectedBizState.get(bizName).get(bizVersion) == bizState) {
                continue;
            } else if (expectedBizState.get(bizName) != null
                       && expectedBizState.get(bizName).get(bizVersion) != null
                       && expectedBizState.get(bizName).get(bizVersion) != bizState) {
                throw new IllegalStateException(String.format(
                    "Don't specify same biz with different bizState, config is %s.", config));
            } else if (expectedBizState.get(bizName) != null
                       && expectedBizState.get(bizName).containsValue(BizState.ACTIVATED)
                       && bizState == BizState.ACTIVATED) {
                throw new IllegalStateException(String.format(
                    "Don't allow multi biz with same bizName to be active, config is %s. ", config));
            }

            if (bizState == BizState.ACTIVATED) {
                if (progressBizState.get(bizName) != null
                    && progressBizState.get(bizName).get(bizVersion) != null) {
                    // switch operation
                    if (progressBizState.get(bizName).get(bizVersion).equals(BizState.DEACTIVATED)) {
                        BizOperation bizOperation = BizOperation.createBizOperation()
                            .setBizName(bizName).setBizVersion(bizVersion)
                            .setOperationType(BizOperation.OperationType.SWITCH)
                            .setParameters(parameters);
                        bizOperations.add(bizOperation);
                        transformBizState(progressBizState, BizOperation.OperationType.SWITCH,
                            bizName, bizVersion);
                    }
                } else {
                    // install operation
                    BizOperation bizOperation = BizOperation.createBizOperation()
                        .setBizName(bizName).setBizVersion(bizVersion)
                        .setOperationType(BizOperation.OperationType.INSTALL)
                        .setParameters(parameters);
                    bizOperations.add(bizOperation);
                    if (progressBizState.get(bizName) != null
                        && progressBizState.get(bizName).containsValue(BizState.ACTIVATED)) {
                        // add switch
                        bizOperations.add(BizOperation.createBizOperation()
                            .setOperationType(BizOperation.OperationType.SWITCH)
                            .setBizName(bizName).setBizVersion(bizVersion));
                        transformBizState(progressBizState, BizOperation.OperationType.INSTALL,
                            bizName, bizVersion);
                        transformBizState(progressBizState, BizOperation.OperationType.SWITCH,
                            bizName, bizVersion);
                    } else {
                        transformBizState(progressBizState, BizOperation.OperationType.INSTALL,
                            bizName, bizVersion);
                    }
                }
            } else {
                if (progressBizState.get(bizName) != null
                    && progressBizState.get(bizName).get(bizVersion) == null
                    && progressBizState.get(bizName).containsValue(BizState.ACTIVATED)) {
                    BizOperation bizOperation = BizOperation.createBizOperation()
                        .setBizName(bizName).setBizVersion(bizVersion)
                        .setOperationType(BizOperation.OperationType.INSTALL)
                        .setParameters(parameters);
                    bizOperations.add(bizOperation);
                    transformBizState(progressBizState, BizOperation.OperationType.INSTALL,
                        bizName, bizVersion);
                } else if (progressBizState.get(bizName) == null
                           || !BizState.DEACTIVATED.equals(progressBizState.get(bizName).get(
                               bizVersion))) {
                    throw new IllegalStateException(String.format(
                        "Biz(%s:%s) cant be transform to %s, config is %s.", bizName, bizVersion,
                        bizState, config));
                }
            }
            if (expectedBizState.get(bizName) == null) {
                expectedBizState.put(bizName, new HashMap<String, BizState>());
            }
            expectedBizState.get(bizName).put(bizVersion, bizState);
        }

        for (String bizName : currentBizState.keySet()) {
            for (String bizVersion : currentBizState.get(bizName).keySet()) {
                if (expectedBizState.get(bizName) == null
                    || !expectedBizState.get(bizName).containsKey(bizVersion)) {
                    bizOperations.add(BizOperation.createBizOperation()
                        .setOperationType(BizOperation.OperationType.UNINSTALL).setBizName(bizName)
                        .setBizVersion(bizVersion));
                    transformBizState(progressBizState, BizOperation.OperationType.UNINSTALL,
                        bizName, bizVersion);
                }
            }
        }

        // double check
        if (!checkBizState(expectedBizState, progressBizState)) {
            throw new IllegalStateException(String.format(
                "Failed to transform biz operation, config is %s.", config));
        }

        return bizOperations;
    }

    /**
     * config format is similar to bizName:bizVersion:bizState;bizName:bizVersion:bizState
     *
     * @param config
     * @return
     */
    public static boolean isValidConfig(String config) {
        for (String singleConfig : config.split(Constants.STRING_SEMICOLON)) {
            int idx = singleConfig.indexOf(Constants.QUESTION_MARK_SPLIT);
            String parameterStr = (idx == -1) ? "" : singleConfig.substring(idx + 1);
            String nvs = (idx == -1) ? singleConfig : singleConfig.substring(0, idx);
            String[] configInfo = nvs.split(Constants.STRING_COLON);
            if (configInfo.length != 3) {
                return false;
            }

            if (!BizState.ACTIVATED.name().equalsIgnoreCase(configInfo[2])
                && !BizState.DEACTIVATED.name().equalsIgnoreCase(configInfo[2])) {
                return false;
            }

            if (!isValidParameter(parameterStr)) {
                return false;
            }
        }
        return true;
    }

    public static Map<String, String> parseParameter(String config) {
        Map<String, String> parameters = new HashMap<>();
        if (!StringUtils.isEmpty(config)) {
            String[] keyValue = config.split(Constants.AMPERSAND_SPLIT);
            for (String kv : keyValue) {
                String[] paramSplit = kv.split(Constants.EQUAL_SPLIT);
                parameters.put(paramSplit[0], paramSplit[1]);
            }
        }
        return parameters;
    }

    public static boolean isValidParameter(String config) {
        if (!StringUtils.isEmpty(config)) {
            String[] keyValue = config.split(Constants.AMPERSAND_SPLIT);
            for (String kv : keyValue) {
                String[] paramSplit = kv.split(Constants.EQUAL_SPLIT);
                if (paramSplit.length != 2) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void transformBizState(Map<String, Map<String, BizState>> progressBizState,
                                         BizOperation.OperationType operationType, String bizName,
                                         String bizVersion) {
        if (BizOperation.OperationType.SWITCH.equals(operationType)) {
            if (progressBizState.get(bizName) != null) {
                for (String version : progressBizState.get(bizName).keySet()) {
                    progressBizState.get(bizName).put(version, BizState.DEACTIVATED);
                }
            }
            progressBizState.get(bizName).put(bizVersion, BizState.ACTIVATED);
        } else if (BizOperation.OperationType.INSTALL.equals(operationType)) {
            if (progressBizState.get(bizName) != null
                && progressBizState.get(bizName).containsValue(BizState.ACTIVATED)) {
                progressBizState.get(bizName).put(bizVersion, BizState.DEACTIVATED);
            } else {
                if (progressBizState.get(bizName) == null) {
                    progressBizState.put(bizName, new HashMap<String, BizState>());
                }
                progressBizState.get(bizName).put(bizVersion, BizState.ACTIVATED);
            }
        } else if (BizOperation.OperationType.UNINSTALL.equals(operationType)) {
            if (progressBizState.get(bizName) != null) {
                progressBizState.get(bizName).remove(bizVersion);
            }
        }
    }

    /**
     * prefer to handle activated state
     *
     * @param config
     */
    public static ArrayList<String> adjustOperationOrder(String config) {
        ArrayList<String> activatedStateConfig = new ArrayList<>();
        ArrayList<String> deactivatedStateConfig = new ArrayList<>();
        for (String configOperation : config.split(Constants.STRING_SEMICOLON)) {
            if (StringUtils.isEmpty(configOperation)) {
                continue;
            }
            int idx = configOperation.indexOf(Constants.QUESTION_MARK_SPLIT);
            String[] configInfo = (idx == -1) ? configOperation.split(Constants.STRING_COLON)
                : configOperation.substring(0, idx).split(Constants.STRING_COLON);
            BizState bizState = BizState.of(configInfo[2]);
            if (BizState.ACTIVATED.equals(bizState)) {
                activatedStateConfig.add(configOperation);
            } else {
                deactivatedStateConfig.add(configOperation);
            }
        }
        activatedStateConfig.addAll(deactivatedStateConfig);
        return activatedStateConfig;
    }

    public static boolean checkBizState(Map<String, Map<String, BizState>> expectedBizState,
                                        Map<String, Map<String, BizState>> progressBizState) {
        for (String bizName : expectedBizState.keySet()) {
            if (progressBizState.get(bizName) == null
                || progressBizState.get(bizName).keySet().size() != expectedBizState.get(bizName)
                    .size()) {
                return false;
            }
            for (String bizVersion : expectedBizState.get(bizName).keySet()) {
                if (!expectedBizState.get(bizName).get(bizVersion)
                    .equals(progressBizState.get(bizName).get(bizVersion))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Map<String, Map<String, BizState>> cloneBizStateMap(Map<String, Map<String, BizState>> origin) {
        if (origin == null) {
            return null;
        }
        Map<String, Map<String, BizState>> duplicate = new HashMap<>();
        for (String name : origin.keySet()) {
            if (duplicate.get(name) == null) {
                duplicate.put(name, new HashMap<String, BizState>());
            }
            for (String version : origin.get(name).keySet()) {
                duplicate.get(name).put(version, origin.get(name).get(version));
            }
        }
        return duplicate;
    }
}