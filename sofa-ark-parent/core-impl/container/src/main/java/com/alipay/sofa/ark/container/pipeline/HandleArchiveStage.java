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
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
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

import java.util.Set;

import static com.alipay.sofa.ark.spi.constant.Constants.COMMA_SPLIT;
import static com.alipay.sofa.ark.spi.constant.Constants.Biz_ACTIVE_EXCLUDE;
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

            for (BizArchive bizArchive : executableArchive.getBizArchives()) {
                Biz biz = bizFactoryService.createBiz(bizArchive);
                if (!isBizExcluded(biz)) {
                    bizManagerService.registerBiz(bizFactoryService.createBiz(bizArchive));
                } else {
                    LOGGER.warn(String.format("The biz of %s is excluded.", biz.getIdentity()));
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
        String excludeBizConf = ArkConfigs.getStringValue(Biz_ACTIVE_EXCLUDE);
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

}