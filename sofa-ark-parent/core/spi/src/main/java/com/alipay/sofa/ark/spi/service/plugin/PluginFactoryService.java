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
package com.alipay.sofa.ark.spi.service.plugin;

import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.Plugin;

import java.io.File;
import java.io.IOException;

/**
 * Create Plugin according to {@link File} and {@link PluginArchive}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface PluginFactoryService {
    /**
     * Create Plugin Model according to {@link PluginArchive}
     *
     * @param pluginArchive the {@link PluginArchive} model
     * @return Biz
     * @throws IOException throw io exception when {@link PluginArchive} is invalid.
     */
    Plugin createPlugin(PluginArchive pluginArchive) throws IOException;

    /**
     * Create Plugin Model according to {@link File}
     *
     * @param file the plugin file
     * @return Plugin
     * @throws IOException throw io exception when {@link PluginArchive} is invalid.
     */
    Plugin createPlugin(File file) throws IOException;
}