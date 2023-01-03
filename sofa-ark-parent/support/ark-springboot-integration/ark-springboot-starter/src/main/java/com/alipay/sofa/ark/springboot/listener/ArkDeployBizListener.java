package com.alipay.sofa.ark.springboot.listener;

import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;


public class ArkDeployBizListener implements ApplicationListener<ApplicationContextEvent>, Ordered {

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 仅监听基座启动时的Event
        if (this.getClass().getClassLoader() != Thread.currentThread().getContextClassLoader()) {
            return;
        }
        if (ArkConfigs.isEmbedEnable()) {
            if (event instanceof ContextRefreshedEvent) {
                // 基座启动后，静态合并部署biz
                EmbedSofaArkBootstrap.deployBizAfterEmbedMasterBizStarted();
            }
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE - 10;
    }
}
