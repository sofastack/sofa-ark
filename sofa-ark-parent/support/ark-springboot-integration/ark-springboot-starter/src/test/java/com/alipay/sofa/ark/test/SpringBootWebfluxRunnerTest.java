package com.alipay.sofa.ark.test;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.test.springboot.BaseSpringApplication;
import com.alipay.sofa.ark.test.springboot.TestValueHolder;
import com.alipay.sofa.ark.test.springboot.facade.SampleService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringBootWebfluxRunnerTest {
    @Autowired
    public SampleService sampleService;

    @ArkInject
    PluginManagerService pluginManagerService;

    @ArkInject
    EventAdminService eventAdminService;

    @Before
    public void before() {
        ClassLoaderUtils.pushContextClassLoader(ClassLoader.getSystemClassLoader());
        System.setProperty(Constants.EMBED_ENABLE, "true");
    }

    @After
    public void after() {
        System.setProperty(Constants.EMBED_ENABLE, "");
    }

    @Test
    public void test() {
        BaseSpringApplication.main(new String[] {});
        ArkClient.getInjectionService().inject(this);
        Assert.assertNotNull(pluginManagerService);
        Assert.assertEquals(0, TestValueHolder.getTestValue());
        eventAdminService.sendEvent(() -> "test-event-A");
        Assert.assertEquals(10, TestValueHolder.getTestValue());
        eventAdminService.sendEvent(() -> "test-event-B");
        Assert.assertEquals(20, TestValueHolder.getTestValue());
    }
}
