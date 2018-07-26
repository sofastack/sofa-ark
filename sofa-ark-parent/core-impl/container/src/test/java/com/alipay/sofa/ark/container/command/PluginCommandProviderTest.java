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
package com.alipay.sofa.ark.container.command;

import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.container.ArkContainerTest;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.alipay.sofa.ark.spi.service.session.CommandProvider;

/**
 * test PluginCommandProvider
 *
 * @author joe
 * @version 0.5.0
 */
public class PluginCommandProviderTest extends BaseTest {

    private URL jarURL = ArkContainerTest.class.getClassLoader().getResource("test.jar");

    @Override
    public void before() {
        // no op
    }

    @Override
    public void after() {
        // no op
    }

    @Test
    public void testPluginCommandProvider() throws ArkException {
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        ArkContainer arkContainer = (ArkContainer) ArkContainer.main(args);
        Assert.assertTrue(arkContainer.isStarted());
        RegistryService service = ArkServiceContainerHolder.getContainer().getService(
            RegistryService.class);
        ServiceReference<CommandProvider> reference = service
            .referenceService(CommandProvider.class);
        CommandProvider provider = reference.getService();

        Assert.assertFalse(provider.validate("plugin"));
        Assert.assertFalse(provider.validate("plugin info"));
        Assert.assertTrue(provider.validate("plugin list"));
        Assert.assertTrue(provider.validate("plugin info abc"));

        Assert.assertTrue(provider.getHelp() != null);
        Assert.assertTrue(provider.getHelp("plugin") != null);
        Assert.assertTrue(provider.handleCommand("plugin list") != null);
        Assert.assertTrue(provider.handleCommand("plugin info abc") != null);

        arkContainer.stop();
    }
}
