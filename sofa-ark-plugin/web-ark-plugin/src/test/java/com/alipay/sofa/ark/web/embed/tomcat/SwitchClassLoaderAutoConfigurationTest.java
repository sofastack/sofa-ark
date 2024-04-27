package com.alipay.sofa.ark.web.embed.tomcat;

import com.alipay.sofa.ark.spi.model.Biz;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.net.URL;
import java.net.URLClassLoader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SwitchClassLoaderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner().withConfiguration(
            AutoConfigurations.of(SwitchClassLoaderAutoConfiguration.class));

    @Before
    public void prepare() {
        Biz biz = mock(Biz.class);
        when(biz.getBizClassLoader()).thenReturn(ClassLoader.getSystemClassLoader());
    }

    @Test
    public void test() {
        ClassLoader webHandlerClassLoader = new URLClassLoader(new URL[0]);
        ClassLoader classLoader = new ArkTomcatEmbeddedWebappClassLoader(webHandlerClassLoader);

        FilterChain filterChain = (servletRequest, servletResponse) -> Assert.assertEquals(webHandlerClassLoader, Thread.currentThread().getContextClassLoader());

        contextRunner.withClassLoader(classLoader).run(context -> {
            Assertions.assertThat(context).hasSingleBean(
                    SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter.class);
            SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter switchClassLoaderFilter = (SwitchClassLoaderAutoConfiguration.SwitchClassLoaderFilter) context.getBean(
                    "switchClassLoaderFilter");
            switchClassLoaderFilter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);
        });
    }


    @Test
    public void switchClassLoaderFilter() {
    }
}
