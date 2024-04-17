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
package com.alipay.sofa.ark.api;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.biz.AfterBizSwitchEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizSwitchEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizInfo;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.replay.Replay;
import com.alipay.sofa.ark.spi.replay.ReplayContext;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * API used to operate biz
 *
 * @author qilong.zql
 * @since 0.6.0
 */
public class ArkClient {

    private static BizManagerService    bizManagerService;
    private static BizFactoryService    bizFactoryService;
    private static PluginManagerService pluginManagerService;
    private static Biz                  masterBiz;
    private static InjectionService     injectionService;
    private static String[]             arguments;

    /**
     * in some case like multi-tenant jdk, we need to set envs for biz
     */
    private static Map<String, String>  envs;

    private static EventAdminService    eventAdminService;

    private static File getBizInstallDirectory() {
        String configDir = ArkConfigs.getStringValue(Constants.CONFIG_INSTALL_BIZ_DIR);
        return StringUtils.isEmpty(configDir) ? FileUtils.createTempDir("sofa-ark") : FileUtils
            .mkdir(configDir);
    }

    public static File createBizSaveFile(String bizName, String bizVersion, String fileSuffix) {
        String suffix = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        if (!StringUtils.isEmpty(fileSuffix)) {
            suffix = fileSuffix;
        }
        File bizInstallDirectory = getBizInstallDirectory();
        return new File(bizInstallDirectory, bizName + "-" + bizVersion + "-" + suffix);
    }

    public static File createBizSaveFile(String bizName, String bizVersion) {
        return createBizSaveFile(bizName, bizVersion, null);
    }

    public static InjectionService getInjectionService() {
        return injectionService;
    }

    public static void setInjectionService(InjectionService injectionService) {
        ArkClient.injectionService = injectionService;
    }

    public static BizManagerService getBizManagerService() {
        return bizManagerService;
    }

    public static void setBizManagerService(BizManagerService bizManagerService) {
        ArkClient.bizManagerService = bizManagerService;
    }

    public static BizFactoryService getBizFactoryService() {
        return bizFactoryService;
    }

    public static void setBizFactoryService(BizFactoryService bizFactoryService) {
        ArkClient.bizFactoryService = bizFactoryService;
    }

    public static void setPluginManagerService(PluginManagerService pluginManagerService) {
        ArkClient.pluginManagerService = pluginManagerService;
    }

    public static PluginManagerService getPluginManagerService() {
        return pluginManagerService;
    }

    public static Biz getMasterBiz() {
        return masterBiz;
    }

    public static void setMasterBiz(Biz masterBiz) {
        ArkClient.masterBiz = masterBiz;
    }

    public static EventAdminService getEventAdminService() {
        return eventAdminService;
    }

    public static void setEventAdminService(EventAdminService eventAdminService) {
        ArkClient.eventAdminService = eventAdminService;
    }

    public static String[] getArguments() {
        return arguments;
    }

    public static void setArguments(String[] arguments) {
        ArkClient.arguments = arguments;
    }

    public static Map<String, String> getEnvs() {
        return envs;
    }

    public static void setEnvs(Map<String, String> envs) {
        ArkClient.envs = envs;
    }

    /**
     * Install Biz with default arguments and envs throw file
     *
     * @param bizFile
     * @throws Throwable
     */
    public static ClientResponse installBiz(File bizFile) throws Throwable {
        return installBiz(bizFile, arguments, envs);
    }

    public static ClientResponse installBiz(File bizFile, String[] args, Map<String, String> envs)
                                                                                                  throws Throwable {
        return doInstallBiz(bizFile, args, envs);
    }

    public static ClientResponse installBiz(File bizFile, String[] args) throws Throwable {
        return doInstallBiz(bizFile, args, null);
    }

    private static ClientResponse doInstallBiz(File bizFile, String[] args, Map<String, String> envs)
                                                                                                     throws Throwable {
        AssertUtils.assertNotNull(bizFactoryService, "bizFactoryService must not be null!");
        AssertUtils.assertNotNull(bizManagerService, "bizManagerService must not be null!");
        AssertUtils.assertNotNull(bizFile, "bizFile must not be null!");

        long start = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
        String startDate = sdf.format(new Date(start));

        Biz biz = bizFactoryService.createBiz(bizFile);
        ClientResponse response = new ClientResponse();
        if (bizManagerService.getBizByIdentity(biz.getIdentity()) != null
            || !bizManagerService.registerBiz(biz)) {
            return response.setCode(ResponseCode.REPEAT_BIZ).setMessage(
                String.format("Biz: %s has been installed or registered.", biz.getIdentity()));
        }

        try {
            biz.start(args, envs);
            long end = System.currentTimeMillis();
            response
                .setCode(ResponseCode.SUCCESS)
                .setMessage(
                    String.format("Install Biz: %s success, cost: %s ms, started at: %s",
                        biz.getIdentity(), end - start, startDate))
                .setBizInfos(Collections.<BizInfo> singleton(biz));
            getLogger().info(response.getMessage());
            return response;
        } catch (Throwable throwable) {
            long end = System.currentTimeMillis();
            response.setCode(ResponseCode.FAILED).setMessage(
                String.format("Install Biz: %s fail,cost: %s ms, started at: %s",
                    biz.getIdentity(), end - start, startDate));
            getLogger().error(response.getMessage(), throwable);
            try {
                biz.stop();
            } catch (Throwable e) {
                getLogger().error(String.format("UnInstall Biz: %s fail.", biz.getIdentity()), e);
            } finally {
                bizManagerService.unRegisterBizStrictly(biz.getBizName(), biz.getBizVersion());
            }
            throw throwable;
        }
    }

    /**
     * Uninstall biz.
     *
     * @param bizName
     * @param bizVersion
     * @return
     * @throws Throwable
     */
    public static ClientResponse uninstallBiz(String bizName, String bizVersion) throws Throwable {
        AssertUtils.assertNotNull(bizFactoryService, "bizFactoryService must not be null!");
        AssertUtils.assertNotNull(bizManagerService, "bizManagerService must not be null!");
        AssertUtils.assertNotNull(bizName, "bizName must not be null!");
        AssertUtils.assertNotNull(bizVersion, "bizVersion must not be null!");

        // ignore when uninstall master biz
        if (bizName.equals(ArkConfigs.getStringValue(Constants.MASTER_BIZ))) {
            return new ClientResponse().setCode(ResponseCode.FAILED).setMessage(
                "Master biz must not be uninstalled.");
        }

        Biz biz = bizManagerService.getBiz(bizName, bizVersion);
        ClientResponse response = new ClientResponse().setCode(ResponseCode.NOT_FOUND_BIZ)
            .setMessage(
                String.format("Uninstall biz: %s not found.",
                    BizIdentityUtils.generateBizIdentity(bizName, bizVersion)));
        if (biz != null) {
            try {
                biz.stop();
            } catch (Throwable throwable) {
                getLogger().error(String.format("UnInstall Biz: %s fail.", biz.getIdentity()),
                    throwable);
                throw throwable;
            } finally {
                bizManagerService.unRegisterBizStrictly(biz.getBizName(), biz.getBizVersion());
            }
            response.setCode(ResponseCode.SUCCESS).setMessage(
                String.format("Uninstall biz: %s success.", biz.getIdentity()));
        }
        getLogger().info(response.getMessage());
        return response;
    }

    /**
     * Check all {@link com.alipay.sofa.ark.spi.model.BizInfo}
     *
     * @return
     */
    public static ClientResponse checkBiz() {
        return checkBiz(null, null);
    }

    /**
     * Check all {@link com.alipay.sofa.ark.spi.model.BizInfo} with specified bizName
     *
     * @param bizName
     * @return
     */
    public static ClientResponse checkBiz(String bizName) {
        return checkBiz(bizName, null);
    }

    /**
     * Check all {@link com.alipay.sofa.ark.spi.model.BizInfo} with specified bizName and bizVersion
     *
     * @param bizName
     * @param bizVersion
     * @return
     */
    public static ClientResponse checkBiz(String bizName, String bizVersion) {
        AssertUtils.assertNotNull(bizFactoryService, "bizFactoryService must not be null!");
        AssertUtils.assertNotNull(bizManagerService, "bizManagerService must not be null!");

        ClientResponse response = new ClientResponse();
        Set<BizInfo> bizInfoSet = new HashSet<>();
        if (bizName != null && bizVersion != null) {
            Biz biz = bizManagerService.getBiz(bizName, bizVersion);
            if (biz != null) {
                bizInfoSet.add(biz);
            }
        } else if (bizName != null) {
            bizInfoSet.addAll(bizManagerService.getBiz(bizName));
        } else {
            bizInfoSet.addAll(bizManagerService.getBizInOrder());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Biz count=%d", bizInfoSet.size())).append("\n");
        for (BizInfo bizInfo : bizInfoSet) {
            sb.append(
                String.format("bizName=%s, bizVersion=%s, bizState=%s", bizInfo.getBizName(),
                    bizInfo.getBizVersion(), bizInfo.getBizState())).append("\n");
        }
        response.setCode(ResponseCode.SUCCESS).setBizInfos(bizInfoSet).setMessage(sb.toString());
        getLogger().info(String.format("Check Biz: %s", response.getMessage()));
        return response;
    }

    /**
     * Active biz with specified bizName and bizVersion
     *
     * @param bizName
     * @param bizVersion
     * @return
     */
    public static ClientResponse switchBiz(String bizName, String bizVersion) {
        AssertUtils.assertNotNull(bizFactoryService, "bizFactoryService must not be null!");
        AssertUtils.assertNotNull(bizManagerService, "bizManagerService must not be null!");
        AssertUtils.assertNotNull(bizName, "bizName must not be null!");
        AssertUtils.assertNotNull(bizVersion, "bizVersion must not be null!");
        Biz biz = bizManagerService.getBiz(bizName, bizVersion);
        ClientResponse response = new ClientResponse().setCode(ResponseCode.NOT_FOUND_BIZ)
            .setMessage(
                String.format("Switch biz: %s not found.",
                    BizIdentityUtils.generateBizIdentity(bizName, bizVersion)));
        if (biz != null) {
            if (biz.getBizState() != BizState.ACTIVATED
                && biz.getBizState() != BizState.DEACTIVATED) {
                response.setCode(ResponseCode.ILLEGAL_STATE_BIZ).setMessage(
                    String.format("Switch Biz: %s's state must not be %s.", biz.getIdentity(),
                        biz.getBizState()));
            } else {
                eventAdminService.sendEvent(new BeforeBizSwitchEvent(biz));
                bizManagerService.activeBiz(bizName, bizVersion);
                eventAdminService.sendEvent(new AfterBizSwitchEvent(biz));
                response.setCode(ResponseCode.SUCCESS).setMessage(
                    String.format("Switch biz: %s is activated.", biz.getIdentity()));
            }
        }
        getLogger().info(response.getMessage());
        return response;
    }

    public static ClientResponse installOperation(BizOperation bizOperation) throws Throwable {
        return doInstallOperation(bizOperation, arguments, envs);
    }

    public static ClientResponse installOperation(BizOperation bizOperation, String[] args)
                                                                                           throws Throwable {
        return doInstallOperation(bizOperation, args, null);
    }

    public static ClientResponse installOperation(BizOperation bizOperation, String[] args,
                                                  Map<String, String> envs) throws Throwable {
        return doInstallOperation(bizOperation, args, envs);
    }

    private static ClientResponse doInstallOperation(BizOperation bizOperation, String[] args,
                                                     Map<String, String> envs) throws Throwable {
        AssertUtils.isTrue(
            BizOperation.OperationType.INSTALL.equals(bizOperation.getOperationType()),
            "Operation type must be install");
        File bizFile = null;
        if (bizOperation.getParameters().get(Constants.CONFIG_BIZ_URL) != null) {
            URL url = new URL(bizOperation.getParameters().get(Constants.CONFIG_BIZ_URL));
            bizFile = ArkClient.createBizSaveFile(bizOperation.getBizName(),
                bizOperation.getBizVersion());

            try (InputStream inputStream = url.openStream()) {
                FileUtils.copyInputStreamToFile(inputStream, bizFile);
            }
        }
        return installBiz(bizFile, args, envs);
    }

    public static ClientResponse uninstallOperation(BizOperation bizOperation) throws Throwable {
        AssertUtils.isTrue(
            BizOperation.OperationType.UNINSTALL.equals(bizOperation.getOperationType()),
            "Operation type must be uninstall");
        return uninstallBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

    public static ClientResponse switchOperation(BizOperation bizOperation) {
        AssertUtils.isTrue(
            BizOperation.OperationType.SWITCH.equals(bizOperation.getOperationType()),
            "Operation type must be switch");
        return switchBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

    public static ClientResponse checkOperation(BizOperation bizOperation) {
        AssertUtils.isTrue(
            BizOperation.OperationType.CHECK.equals(bizOperation.getOperationType()),
            "Operation type must be check");
        return checkBiz(bizOperation.getBizName(), bizOperation.getBizVersion());
    }

    /**
     * dynamic invoke by specified version
     * @param version
     * @param replay
     * @return
     */
    public static Object invocationReplay(String version, Replay replay) {
        try {
            ReplayContext.set(version);
            return replay.invoke();
        } finally {
            ReplayContext.unset();
        }
    }

    private static ArkLogger getLogger() {
        return ArkLoggerFactory.getDefaultLogger();
    }

}
