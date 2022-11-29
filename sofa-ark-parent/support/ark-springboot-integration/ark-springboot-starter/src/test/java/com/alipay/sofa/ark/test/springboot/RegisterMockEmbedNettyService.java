//package com.alipay.sofa.ark.test.springboot;
//
//import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
//import com.alipay.sofa.ark.spi.service.ArkInject;
//import com.alipay.sofa.ark.spi.service.registry.RegistryService;
//import com.alipay.sofa.ark.spi.web.EmbeddedServerService;
//import com.alipay.sofa.ark.netty.EmbeddedServerServiceImpl;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//
//public class RegisterMockEmbedNettyService implements BeanPostProcessor, InitializingBean {
//    @ArkInject
//    private RegistryService registryService;
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        registryService.publishService(EmbeddedServerService.class,
//                new EmbeddedServerServiceImpl(), new ContainerServiceProvider());
//    }
//}
