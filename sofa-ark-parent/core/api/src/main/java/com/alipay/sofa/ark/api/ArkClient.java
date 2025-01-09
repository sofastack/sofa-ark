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
import com.alipay.sofa.ark.spi.model.*;
import com.alipay.sofa.ark.spi.replay.Replay;
import com.alipay.sofa.ark.spi.replay.ReplayContext;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.alipay.sofa.ark.spi.constant.Constants.AUTO_UNINSTALL_WHEN_FAILED_ENABLE;

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
    private static PluginFactoryService pluginFactoryService;
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

    public static PluginFactoryService getPluginFactoryService() {
        return pluginFactoryService;
    }

    public static void setPluginFactoryService(PluginFactoryService pluginFactoryService) {
        ArkClient.pluginFactoryService = pluginFactoryService;
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

    public static ClientResponse installBiz(File bizFile, String[] args) throws Throwable {
        return installBiz(bizFile, args, null);
    }

    public static ClientResponse installBiz(File bizFile, String[] args, Map<String, String> envs)
                                                                                                  throws Throwable {
        BizConfig bizConfig = new BizConfig();
        bizConfig.setArgs(args);
        bizConfig.setEnvs(envs);
        return doInstallBiz(bizFile, bizConfig);
    }

    public static ClientResponse installBiz(File bizFile, BizConfig bizConfig) throws Throwable {
        return doInstallBiz(bizFile, bizConfig);
    }

    private static ClientResponse doInstallBiz(File bizFile, BizConfig bizConfig) throws Throwable {
        AssertUtils.assertNotNull(bizFactoryService, "bizFactoryService must not be null!");
        AssertUtils.assertNotNull(bizManagerService, "bizManagerService must not be null!");
        AssertUtils.assertNotNull(bizFile, "bizFile must not be null!");
        AssertUtils.assertNotNull(bizConfig, "bizConfig must not be null!");

        long start = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
        String startDate = sdf.format(new Date(start));

        Biz biz = bizFactoryService.createBiz(bizFile, bizConfig);
        ClientResponse response = new ClientResponse();
        if (bizManagerService.getBizByIdentity(biz.getIdentity()) != null
            || !bizManagerService.registerBiz(biz)) {
            return response.setCode(ResponseCode.REPEAT_BIZ).setMessage(
                String.format("Biz: %s has been installed or registered.", biz.getIdentity()));
        }

        try {
            biz.start(bizConfig.getArgs(), bizConfig.getEnvs());
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

            boolean autoUninstall = Boolean.parseBoolean(ArkConfigs.getStringValue(
                AUTO_UNINSTALL_WHEN_FAILED_ENABLE, "true"));
            if (autoUninstall) {
                try {
                    getLogger().error(
                        String.format("Start Biz: %s failed, try to unInstall this biz.",
                            biz.getIdentity()));
                    biz.stop();
                } catch (Throwable e) {
                    getLogger().error(String.format("UnInstall Biz: %s fail.", biz.getIdentity()),
                        e);
                }
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

        // prepare extension urls if necessary
        URL[] extensionUrls = null;
        if (bizOperation.getParameters().get(Constants.BIZ_EXTENSION_URLS) != null) {
            Set<String> extensionLibs = StringUtils.strToSet(
                bizOperation.getParameters().get(Constants.BIZ_EXTENSION_URLS),
                Constants.COMMA_SPLIT);
            List<URL> urlsList = new ArrayList<>();
            if (!extensionLibs.isEmpty()) {
                for (String extension : extensionLibs) {
                    URL url = new URL(extension);
                    urlsList.add(url);
                }
            }
            extensionUrls = urlsList.toArray(new URL[0]);
        }

        BizConfig bizConfig = new BizConfig();
        bizConfig.setExtensionUrls(extensionUrls);
        bizConfig.setArgs(args);
        bizConfig.setEnvs(envs);
        return installBiz(bizFile, bizConfig);
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

    public static ClientResponse installPlugin(PluginOperation pluginOperation) throws Exception {
        AssertUtils.assertNotNull(pluginOperation, "pluginOperation must not be null");

        // prepare plugin file
        File localFile = pluginOperation.getLocalFile();
        if (localFile == null && !StringUtils.isEmpty(pluginOperation.getUrl())) {
            URL url = new URL(pluginOperation.getUrl());
            String pluginDir = ArkConfigs.getStringValue(Constants.CONFIG_INSTALL_PLUGIN_DIR);
            File pluginDirectory = StringUtils.isEmpty(pluginDir) ? FileUtils
                .createTempDir("sofa-ark") : FileUtils.mkdir(pluginDir);
            localFile = new File(pluginDirectory, pluginOperation.getPluginName() + "-"
                                                  + pluginOperation.getPluginVersion() + "-"
                                                  + System.currentTimeMillis());
            try (InputStream inputStream = url.openStream()) {
                FileUtils.copyInputStreamToFile(inputStream, localFile);
            }
        }

        ClientResponse response = new ClientResponse();
        if (localFile == null) {
            response.setCode(ResponseCode.FAILED).setMessage(
                String.format("Install Plugin: %s-%s fail, local file is null.",
                    pluginOperation.getPluginName(), pluginOperation.getPluginVersion()));
            return response;
        }

        PluginConfig pluginConfig = new PluginConfig();
        if (!StringUtils.isEmpty(pluginOperation.getPluginName())) {
            pluginConfig.setSpecifiedName(pluginOperation.getPluginName());
        }
        if (!StringUtils.isEmpty(pluginOperation.getPluginVersion())) {
            pluginConfig.setSpecifiedVersion(pluginOperation.getPluginVersion());
        }

        // prepare extension urls if necessary
        List<String> extensionLibs = pluginOperation.getExtensionLibs();
        List<URL> urlsList = new ArrayList<>();
        if (extensionLibs != null && !extensionLibs.isEmpty()) {
            for (String extension : extensionLibs) {
                URL url = new URL(extension);
                urlsList.add(url);
            }
        }
        URL[] extensionUrls = urlsList.toArray(new URL[0]);
        pluginConfig.setExtensionUrls(extensionUrls);

        long start = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS");
        String startDate = sdf.format(new Date(start));

        // create
        Plugin plugin = pluginFactoryService.createPlugin(localFile, pluginConfig);
        // register
        pluginManagerService.registerPlugin(plugin);
        // start
        try {
            plugin.start();
            long end = System.currentTimeMillis();
            response.setCode(ResponseCode.SUCCESS).setMessage(
                String.format("Install Plugin: %s success, cost: %s ms, started at: %s",
                    plugin.getPluginName() + ":" + plugin.getVersion(), end - start, startDate));
            getLogger().info(response.getMessage());
        } catch (Throwable throwable) {
            long end = System.currentTimeMillis();
            response.setCode(ResponseCode.FAILED).setMessage(
                String.format("Install Plugin: %s fail,cost: %s ms, started at: %s",
                    plugin.getPluginName() + ":" + plugin.getVersion(), end - start, startDate));
            getLogger().error(response.getMessage(), throwable);
            throw throwable;
        }
        return response;
    }

    public static ClientResponse checkPlugin() {
        return checkPlugin(null);
    }

    public static ClientResponse checkPlugin(String pluginName) {
        AssertUtils.assertNotNull(pluginFactoryService, "pluginFactoryService must not be null!");
        AssertUtils.assertNotNull(pluginManagerService, "pluginManagerService must not be null!");

        ClientResponse response = new ClientResponse();
        Set<Plugin> plugins = new HashSet<>();
        if (pluginName != null) {
            Plugin plugin = pluginManagerService.getPluginByName(pluginName);
            if (plugin != null) {
                plugins.add(plugin);
            }
        } else {
            plugins.addAll(pluginManagerService.getPluginsInOrder());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Plugin count=%d", plugins.size())).append("\n");
        for (Plugin plugin : plugins) {
            sb.append(
                String.format("pluginName=%s, pluginVersion=%s", plugin.getPluginName(),
                    plugin.getVersion())).append("\n");
        }
        response.setCode(ResponseCode.SUCCESS).setPluginInfos(plugins).setMessage(sb.toString());
        getLogger().info(String.format("Check Plugin: %s", response.getMessage()));
        return response;
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
