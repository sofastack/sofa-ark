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

import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.pipeline.PipelineStage;
import com.google.inject.Singleton;

/**
 * Set necessary environment properties
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class SystemPropertiesSettingStage implements PipelineStage {

    @Override
    public void process(PipelineContext pipelineContext) throws ArkException {
        // Forbid to Monitoring and Management Using JMX, because it leads to conflict when setup multi spring boot app.
        EnvironmentUtils.setSystemProperty(Constants.SPRING_BOOT_ENDPOINTS_JMX_ENABLED,
            String.valueOf(false));
        // ignore thread class loader when loading classes and resource in log4j
        EnvironmentUtils.setSystemProperty(Constants.LOG4J_IGNORE_TCL, String.valueOf(true));
    }
}