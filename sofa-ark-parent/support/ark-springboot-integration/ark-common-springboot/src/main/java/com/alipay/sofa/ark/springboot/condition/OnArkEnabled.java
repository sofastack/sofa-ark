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
package com.alipay.sofa.ark.springboot.condition;

import com.alipay.sofa.ark.api.ArkConfigs;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class OnArkEnabled extends SpringBootCondition {
    private static final String ARK_TEST_CLASSLOADER_NAME   = "com.alipay.sofa.ark.container.test.TestClassLoader";
    private static final String ARK_BIZ_CLASSLOADER_NAME    = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";
    private static final String ARK_PLUGIN_CLASSLOADER_NAME = "com.alipay.sofa.ark.container.service.classloader.PluginClassLoader";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String currentClassLoader = this.getClass().getClassLoader().getClass().getName();
        if (ARK_TEST_CLASSLOADER_NAME.equals(currentClassLoader)
            || ARK_BIZ_CLASSLOADER_NAME.equals(currentClassLoader)
            || ARK_PLUGIN_CLASSLOADER_NAME.equals(currentClassLoader)) {
            return new ConditionOutcome(true, "SOFAArk has started.");
        } else if (ArkConfigs.isEmbedEnable()) {
            return new ConditionOutcome(true, "Embed SOFAArk has started.");
        } else {
            return new ConditionOutcome(false, "SOFAArk has not started.");
        }
    }
}