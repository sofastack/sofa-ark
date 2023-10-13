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
package com.alipay.sofa.ark.config.apollo;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.config.OperationProcessor;
import com.alipay.sofa.ark.config.util.OperationTransformer;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.service.PluginActivator;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;

import static com.alipay.sofa.ark.spi.constant.Constants.APOLLO_MASTER_BIZ_KEY;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_APOLLO_NAMESPACE;

/**
 * @author zsk
 * @version $Id: ApolloConfigActivator.java, v 0.1 2023年09月28日 17:24 zsk Exp $
 */
public class ApolloConfigActivator implements PluginActivator {

    private final static ArkLogger LOGGER = ArkLoggerFactory.getLogger(ApolloConfigActivator.class);

    private ConfigChangeListener   changeListener;

    @Override
    public void start(PluginContext context) {
        LOGGER.info("start apollo config activator");

        Config config = ConfigService.getConfig(CONFIG_APOLLO_NAMESPACE);
        changeListener = new ConfigChangeListener() {
            @Override
            public void onChange(ConfigChangeEvent changeEvent) {
                for (String key : changeEvent.changedKeys()) {
                    if (APOLLO_MASTER_BIZ_KEY.equals(key)) {
                        ConfigChange change = changeEvent.getChange(key);
                        String value = change.getNewValue();
                        if (StringUtils.isEmpty(value)) {
                            LOGGER.info("ignore empty masterBiz value");
                            return;
                        }
                        LOGGER.info("Start to process masterBiz config: {}", value);
                        OperationProcessor.process(OperationTransformer.transformToBizOperation(
                            value, context));
                    } else {
                        LOGGER
                            .warn(
                                "only accept config key={} in nameSpace={}, ignore changeEvent of key={}",
                                APOLLO_MASTER_BIZ_KEY, CONFIG_APOLLO_NAMESPACE, key);
                    }
                }
            }
        };
        config.addChangeListener(changeListener);
    }

    @Override
    public void stop(PluginContext context) {
        LOGGER.info("stop apollo config activator");

        Config config = ConfigService.getConfig(CONFIG_APOLLO_NAMESPACE);
        if (null == changeListener || null == config) {
            return;
        }
        config.removeChangeListener(changeListener);
    }
}
