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

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginContextImpl;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.container.service.classloader.PluginClassLoader;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.jar.Attributes;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * response to handle executable fat jar, parse plugin model and biz model from it
 *
 * @author qilong.zql
 * @since 0.1.0
 */
@Singleton
public class HandleArchiveStage implements PipelineStage {

    @Inject
    private PluginManagerService pluginManagerService;

    @Inject
    private BizManagerService    bizManagerService;

    @Override
    public void process(PipelineContext pipelineContext) throws ArkException {
        try {

            ExecutableArchive executableArchive = pipelineContext.getExecutableArchive();

            for (PluginArchive pluginArchive : executableArchive.getPluginArchives()) {
                pluginManagerService.registerPlugin(transformPluginArchive(pluginArchive));
            }

            for (BizArchive bizArchive : executableArchive.getBizArchives()) {
                bizManagerService.registerBiz(transformBizArchives(bizArchive));
            }

        } catch (Throwable ex) {
            throw new ArkException(ex.getMessage(), ex);
        }
    }

    private Plugin transformPluginArchive(PluginArchive pluginArchive) throws Exception {
        PluginModel plugin = new PluginModel();
        Attributes manifestMainAttributes = pluginArchive.getManifest().getMainAttributes();

        return plugin
            .setPluginName(manifestMainAttributes.getValue(PLUGIN_NAME_ATTRIBUTE))
            .setGroupId(manifestMainAttributes.getValue(GROUP_ID_ATTRIBUTE))
            .setArtifactId(manifestMainAttributes.getValue(ARTIFACT_ID_ATTRIBUTE))
            .setVersion(manifestMainAttributes.getValue(PLUGIN_VERSION_ATTRIBUTE))
            .setPriority(Integer.valueOf(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE)))
            .setPluginActivator(manifestMainAttributes.getValue(ACTIVATOR_ATTRIBUTE))
            .setClassPath(pluginArchive.getUrls())
            .setExportClasses(
                new HashSet<>(Arrays.asList(filterEmptyString(manifestMainAttributes
                    .getValue(EXPORT_CLASSES_ATTRIBUTE)))))
            .setExportPackages(
                new HashSet<>(Arrays.asList(filterEmptyString(manifestMainAttributes
                    .getValue(EXPORT_PACKAGES_ATTRIBUTE)))))
            .setImportClasses(
                new HashSet<>(Arrays.asList(filterEmptyString(manifestMainAttributes
                    .getValue(IMPORT_CLASSES_ATTRIBUTE)))))
            .setImportPackages(
                new HashSet<>(Arrays.asList(filterEmptyString(manifestMainAttributes
                    .getValue(IMPORT_PACKAGES_ATTRIBUTE)))))
            .setExportIndex(pluginArchive.getExportIndex())
            .setPluginClassLoader(
                new PluginClassLoader(plugin.getPluginName(), plugin.getClassPath()))
            .setPluginContext(new PluginContextImpl(plugin));
    }

    private Biz transformBizArchives(BizArchive bizArchive) throws Exception {
        BizModel bizModel = new BizModel();
        Attributes manifestMainAttributes = bizArchive.getManifest().getMainAttributes();

        return bizModel.setBizName(manifestMainAttributes.getValue(ARK_BIZ_NAME))
            .setMainClass(manifestMainAttributes.getValue(MAIN_CLASS_ATTRIBUTE))
            .setPriority(manifestMainAttributes.getValue(PRIORITY_ATTRIBUTE))
            .setDenyImportPackages(manifestMainAttributes.getValue(DENY_IMPORT_PACKAGES))
            .setDenyImportClasses(manifestMainAttributes.getValue(DENY_IMPORT_CLASSES))
            .setDenyImportResources(manifestMainAttributes.getValue(DENY_IMPORT_RESOURCES))
            .setClassPath(bizArchive.getUrls())
            .setClassLoader(new BizClassLoader(bizModel.getBizName(), bizModel.getClassPath()));
    }

    private String[] filterEmptyString(String originString) {
        if (originString == null || originString.isEmpty()) {
            return new String[] {};
        }
        return originString.split(",");
    }

}