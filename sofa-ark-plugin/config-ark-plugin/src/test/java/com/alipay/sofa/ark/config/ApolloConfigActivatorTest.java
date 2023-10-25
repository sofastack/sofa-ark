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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.config.apollo.ApolloConfigActivator;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static com.alipay.sofa.ark.spi.constant.Constants.APOLLO_MASTER_BIZ_KEY;
import static com.alipay.sofa.ark.spi.constant.Constants.CONFIG_APOLLO_NAMESPACE;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author zsk
 * @version $Id: ApolloConfigActivatorTest.java, v 0.1 2023年10月11日 18:22 zsk Exp $
 */
public class ApolloConfigActivatorTest {

    @Test
    public void testStartStop() throws NoSuchFieldException, IllegalAccessException {
        MockApolloConfig mockConfig = new MockApolloConfig();
        MockedStatic<ConfigService> mockService = Mockito.mockStatic(ConfigService.class);
        mockService.when(() -> ConfigService.getConfig(CONFIG_APOLLO_NAMESPACE))
                .thenReturn(mockConfig);

        ApolloConfigActivator apolloActivator = new ApolloConfigActivator();
        PluginContext pluginContext = Mockito.mock(PluginContext.class);
        Mockito.doThrow(new RuntimeException("mock referenceService Failed"))
                .when(pluginContext).referenceService(any());
        //test start
        apolloActivator.start(pluginContext);
        Field field = ApolloConfigActivator.class.getDeclaredField("changeListener");
        field.setAccessible(true);
        Object changeListener = field.get(apolloActivator);
        Assert.assertNotNull(changeListener);

        List<ConfigChangeListener> list = mockConfig.getListeners();
        Assert.assertTrue(1 == list.size());

        String newValue = "nameA:vA:activated;nameA:vB:deactivated";
        ConfigChange configChange = new ConfigChange(CONFIG_APOLLO_NAMESPACE, APOLLO_MASTER_BIZ_KEY, "", newValue, PropertyChangeType.MODIFIED);
        Map<String, ConfigChange > changeMap = ImmutableMap.of(APOLLO_MASTER_BIZ_KEY, configChange);
        ConfigChangeEvent changeEvent = new ConfigChangeEvent(CONFIG_APOLLO_NAMESPACE, changeMap);

        //test apollo onChange event: match masterBiz key but OperationProcessor.process failed
        Throwable throwable = null;
        try {
            list.get(0).onChange(changeEvent);
        } catch (Throwable t) {
            throwable = t;
        }
        Assert.assertNotNull(throwable);
        Assert.assertTrue(throwable.getMessage().equals(
                "mock referenceService Failed"));

        //stop
        apolloActivator.stop(null);
        list = mockConfig.getListeners();
        Assert.assertTrue(0 == list.size());
    }
}
