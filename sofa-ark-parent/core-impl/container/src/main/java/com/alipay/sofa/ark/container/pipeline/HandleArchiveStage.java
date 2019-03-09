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

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.DirectoryBizArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.COMMA_SPLIT;
import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_INCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_INCLUDE;

/**
 * response to handle executable fat jar, parse plugin model and biz model from it
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Singleton
public class HandleArchiveStage implements PipelineStage {
    private final static ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Inject
    private PluginManagerService   pluginManagerService;

    @Inject
    private PluginFactoryService   pluginFactoryService;

    @Inject
    private BizManagerService      bizManagerService;

    @Inject
    private BizFactoryService      bizFactoryService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkRuntimeException {
        try {
            ExecutableArchive executableArchive = pipelineContext.getExecutableArchive();

            for (PluginArchive pluginArchive : executableArchive.getPluginArchives()) {
                Plugin plugin = pluginFactoryService.createPlugin(pluginArchive);
                if (!isPluginExcluded(plugin)) {
                    pluginManagerService.registerPlugin(plugin);
                } else {
                    LOGGER.warn(String.format("The plugin of %s is excluded.",
                        plugin.getPluginName()));
                }
            }

            if (useDynamicConfig()) {
                AssertUtils.isFalse(
                    StringUtils.isEmpty(ArkConfigs.getStringValue(Constants.MASTER_BIZ)),
                    "Master biz should be configured when using dynamic config.");
            }

            int bizCount = 0;
            for (BizArchive bizArchive : executableArchive.getBizArchives()) {
                Biz biz = bizFactoryService.createBiz(bizArchive);
                if (bizArchive instanceof DirectoryBizArchive) {
                    if (!((DirectoryBizArchive) bizArchive).isTestMode()) {
                        bizManagerService.registerBiz(biz);
                        bizCount += 1;
                    }
                } else if (useDynamicConfig()) {
                    if (biz.getBizName().equals(ArkConfigs.getStringValue(Constants.MASTER_BIZ))) {
                        bizManagerService.registerBiz(biz);
                        bizCount += 1;
                    } else {
                        LOGGER.warn("The biz of {} is ignored when using dynamic config.",
                            biz.getIdentity());
                    }
                } else {
                    if (!isBizExcluded(biz)) {
                        bizManagerService.registerBiz(biz);
                        bizCount += 1;
                    } else {
                        LOGGER.warn(String.format("The biz of %s is excluded.", biz.getIdentity()));
                    }
                }
            }

            // master biz should be specified when deploy multi biz, otherwise the only biz would be token as master biz
            if (bizCount > 1) {
                AssertUtils.isFalse(
                    StringUtils.isEmpty(ArkConfigs.getStringValue(Constants.MASTER_BIZ)),
                    "Master biz should be configured when deploy multi biz.");
            } else {
                List<Biz> bizList = bizManagerService.getBizInOrder();
                if (!bizList.isEmpty()
                    && StringUtils.isEmpty(ArkConfigs.getStringValue(Constants.MASTER_BIZ))) {
                    ArkConfigs.putStringValue(Constants.MASTER_BIZ, bizList.get(0).getBizName());
                }
            }

        } catch (Throwable ex) {
            throw new ArkRuntimeException(ex.getMessage(), ex);
        }
    }

    public boolean isPluginExcluded(Plugin plugin) {
        String pluginName = plugin.getPluginName();
        String includePluginConf = ArkConfigs.getStringValue(PLUGIN_ACTIVE_INCLUDE);
        String excludePluginConf = ArkConfigs.getStringValue(PLUGIN_ACTIVE_EXCLUDE);
        Set<String> includePlugins = StringUtils.strToSet(includePluginConf, COMMA_SPLIT);
        Set<String> excludePlugins = StringUtils.strToSet(excludePluginConf, COMMA_SPLIT);
        if (includePluginConf == null && excludePluginConf == null) {
            return false;
        } else if (includePluginConf == null) {
            return excludePlugins.contains(pluginName);
        } else {
            return !includePlugins.contains(pluginName);
        }
    }

    public boolean isBizExcluded(Biz biz) {
        String bizIdentity = biz.getIdentity();
        String includeBizConf = ArkConfigs.getStringValue(BIZ_ACTIVE_INCLUDE);
        String excludeBizConf = ArkConfigs.getStringValue(BIZ_ACTIVE_EXCLUDE);
        Set<String> includeBizs = StringUtils.strToSet(includeBizConf, COMMA_SPLIT);
        Set<String> excludeBizs = StringUtils.strToSet(excludeBizConf, COMMA_SPLIT);
        if (includeBizConf == null && excludeBizConf == null) {
            return false;
        } else if (includeBizConf == null) {
            return excludeBizs.contains(bizIdentity);
        } else {
            return !includeBizs.contains(bizIdentity);
        }
    }

    public boolean useDynamicConfig() {
        return !StringUtils.isEmpty(ArkConfigs.getStringValue(Constants.CONFIG_SERVER_ADDRESS));
    }
}