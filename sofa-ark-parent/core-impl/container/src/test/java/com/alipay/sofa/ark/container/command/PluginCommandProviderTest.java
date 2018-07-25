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
 * @version 2018.07.25 17:41
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
        RegistryService service = ArkServiceContainerHolder.getContainer()
            .getService(RegistryService.class);
        ServiceReference<CommandProvider> reference = service
            .referenceService(CommandProvider.class);
        CommandProvider provider = reference.getService();

        Assert.assertTrue(provider.validate("plugin"));
        Assert.assertTrue(provider.validate("plugin list"));
        Assert.assertTrue(provider.validate("plugin info"));

        Assert.assertTrue(provider.getHelp() != null);
        Assert.assertTrue(provider.getHelp("plugin") != null);
        Assert.assertTrue(provider.handleCommand("plugin list") != null);
        Assert.assertTrue(provider.handleCommand("plugin info abc") != null);

        arkContainer.stop();
    }
}
