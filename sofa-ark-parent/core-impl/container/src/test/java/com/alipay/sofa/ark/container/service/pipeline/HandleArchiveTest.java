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
package com.alipay.sofa.ark.container.service.pipeline;

import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.pipeline.HandleArchiveStage;
import org.junit.Assert;
import org.junit.Test;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_ACTIVE_INCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_EXCLUDE;
import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_ACTIVE_INCLUDE;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class HandleArchiveTest {

    @Test
    public void testIncludeExcludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_INCLUDE, "pluginA");
            System.setProperty(PLUGIN_ACTIVE_EXCLUDE, "pluginA,pluginB");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_EXCLUDE);
            System.clearProperty(PLUGIN_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testIncludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_INCLUDE, "pluginA");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testExcludePlugin() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(PLUGIN_ACTIVE_EXCLUDE, "pluginA,pluginB");
            PluginModel pluginModel = new PluginModel();
            pluginModel.setPluginName("pluginA");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginB");
            Assert.assertTrue(handleArchiveStage.isPluginExcluded(pluginModel));
            pluginModel.setPluginName("pluginC");
            Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
        } finally {
            System.clearProperty(PLUGIN_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testNoIncludeExcludeBiz() {
        HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
        PluginModel pluginModel = new PluginModel();
        pluginModel.setPluginName("pluginA");
        Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
        pluginModel.setPluginName("pluginB");
        Assert.assertFalse(handleArchiveStage.isPluginExcluded(pluginModel));
    }

    @Test
    public void testIncludeExcludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_INCLUDE, "bizA:1.0.0");
            System.setProperty(BIZ_ACTIVE_EXCLUDE, "bizA:1.0.0,BizB:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizC");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_INCLUDE);
            System.clearProperty(BIZ_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testIncludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_INCLUDE, "bizA:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("pluginB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("pluginC");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_INCLUDE);
        }
    }

    @Test
    public void testExcludeBiz() {
        try {
            HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
            System.setProperty(BIZ_ACTIVE_EXCLUDE, "bizA:1.0.0,bizB:1.0.0");
            BizModel bizModel = new BizModel();
            bizModel.setBizName("bizA").setBizVersion("1.0.0");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizB");
            Assert.assertTrue(handleArchiveStage.isBizExcluded(bizModel));
            bizModel.setBizName("bizC");
            Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
        } finally {
            System.clearProperty(BIZ_ACTIVE_EXCLUDE);
        }
    }

    @Test
    public void testNoIncludeExcludePlugin() {
        HandleArchiveStage handleArchiveStage = new HandleArchiveStage();
        BizModel bizModel = new BizModel();
        bizModel.setBizName("bizA").setBizVersion("1.0.0");
        Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
        bizModel.setBizName("bizB");
        Assert.assertFalse(handleArchiveStage.isBizExcluded(bizModel));
    }

}