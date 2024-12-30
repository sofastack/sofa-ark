package com.alipay.sofa.ark.springboot.listener;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;

import static org.mockito.Mockito.*;

/**
 * @author gaowh
 * @version 1.0
 * @time 2024/12/30
 */
public class ArkDeployStaticBizListenerTest {

    private static MockedStatic<EmbedSofaArkBootstrap> bootstrap;

    @BeforeClass
    public static void beforeClass() {
        bootstrap = mockStatic(EmbedSofaArkBootstrap.class);
        MockedStatic<ArkConfigs> arkConfigs = mockStatic(ArkConfigs.class);
        arkConfigs.when(ArkConfigs::isEmbedEnable).thenReturn(true);
        arkConfigs.when(ArkConfigs::isEmbedStaticBizEnable).thenReturn(true);
    }

    /**
     * classloader不匹配的场景
     */
    @Test
    public void testDiffClassLoader() {
        ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(new ClassLoader() {
            });
            listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
            bootstrap.verify(Mockito.times(0), EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * applicationEvent 不是 ContextRefreshedEvent 的场景
     */
    @Test
    public void testNonContextRefreshedEvent() {
        ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
        listener.onApplicationEvent(new ContextStartedEvent(new AnnotationConfigApplicationContext()));
        bootstrap.verify(Mockito.times(0), EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted);
    }

    /**
     * 事件重复发送的场景
     */
    @Test
    public void testDeployed() {
        ArkDeployStaticBizListener listener = new ArkDeployStaticBizListener();
        listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
        bootstrap.verify(Mockito.times(1), EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted);
        // 容器刷新事件已经发送过，重复发送不会重复部署
        listener.onApplicationEvent(new ContextRefreshedEvent(new AnnotationConfigApplicationContext()));
        bootstrap.verify(Mockito.times(1), EmbedSofaArkBootstrap::deployStaticBizAfterEmbedMasterBizStarted);
    }

}
