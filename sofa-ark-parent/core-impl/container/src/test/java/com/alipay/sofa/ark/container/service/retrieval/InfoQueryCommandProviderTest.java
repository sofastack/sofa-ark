package com.alipay.sofa.ark.container.service.retrieval;

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.session.handler.ArkCommandHandler;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author yanzhu
 * @date 2023/10/17 17:55
 */
public class InfoQueryCommandProviderTest extends BaseTest {

    private InjectionService   injectionService;
    private InfoQueryCommandProvider queryCommandProvider;

    @Override
    public void before() {
        super.before();

        injectionService = arkServiceContainer.getService(InjectionService.class);

        queryCommandProvider = new InfoQueryCommandProvider();
        injectionService.inject(queryCommandProvider);

        // trigger telnet command thread pool to be created
        new ArkCommandHandler();
    }

    @Test
    public void testInfoQueryCommandPattern() {
        Assert.assertFalse(queryCommandProvider.validate("ck"));

        Assert.assertTrue(queryCommandProvider.validate("ck -h"));
        Assert.assertFalse(queryCommandProvider.validate("ck -c"));
        Assert.assertFalse(queryCommandProvider.validate("ck -ch"));

        Assert.assertTrue(queryCommandProvider.validate("ck -c com.example.HelloWorld"));
    }

    @Test
    public void testClassInfo() {
        String classInfo = queryCommandProvider.handleCommand("ck -c com.alipay.sofa.ark.container.session.handler.ArkCommandHandler");

        Assert.assertTrue(classInfo.contains("class-info"));
        Assert.assertTrue(classInfo.contains("code-source"));
        Assert.assertTrue(classInfo.contains("container-name"));
        Assert.assertTrue(classInfo.contains("class-loader"));
    }

}
