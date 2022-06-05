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
package com.alipay.sofa.ark.dynamic.listener;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.dynamic.common.context.SofaArkTestContext;
import com.alipay.sofa.ark.dynamic.hook.BizConfigurationHook;
import com.alipay.sofa.ark.dynamic.hook.MasterConfigurationHook;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;

import java.util.ServiceLoader;

/**
 * The type Dependency injection test execution listener.
 *
 * @author hanyue
 * @version : DependencyInjectionTestExecutionListener.java, v 0.1 2022年05月26日 下午1:54 hanyue Exp $
 * @see MasterConfigurationHook
 * @see BizConfigurationHook
 */
public class DependencyInjectionTestExecutionListener implements SofaArkTestExecutionListener {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public void afterInstallMaster(SofaArkTestContext testContext, ClassLoader testClassLoader) {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ApplicationContext applicationContext = testContext.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
                .getAutowireCapableBeanFactory();

        for (MasterConfigurationHook configurationHook : ServiceLoader.load(
                MasterConfigurationHook.class, testClassLoader)) {
            Class<? extends MasterConfigurationHook> configurationHookClass = configurationHook
                    .getClass();
            registryConfiguration(configurationHookClass, beanFactory, registry);
        }
    }

    @Override
    public void afterInstallBiz(SofaArkTestContext testContext, ClassLoader bizClassLoader) {
        BeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        ApplicationContext applicationContext = testContext.getApplicationContext();
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext
                .getAutowireCapableBeanFactory();

        for (BizConfigurationHook configurationHook : ServiceLoader.load(
                BizConfigurationHook.class, bizClassLoader)) {
            Class<? extends BizConfigurationHook> configurationHookClass = configurationHook
                    .getClass();
            registryConfiguration(configurationHookClass, beanFactory, registry);
        }
    }

    /**
     * Registry configuration.
     *
     * @param configurationHookClass the configuration hook class
     * @param beanFactory            the bean factory
     * @param registry               the registry
     */
    public void registryConfiguration(Class<?> configurationHookClass,
                                      DefaultListableBeanFactory beanFactory,
                                      BeanDefinitionRegistry registry) {
        if (configurationHookClass.isAnnotationPresent(Configuration.class)) {
            AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(
                    configurationHookClass);

            ScopeMetadata scopeMetadata = new AnnotationScopeMetadataResolver()
                    .resolveScopeMetadata(abd);
            abd.setScope(scopeMetadata.getScopeName());
            String beanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(abd, registry);

            AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);

            BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
            definitionHolder = applyScopedProxyMode(scopeMetadata, definitionHolder, registry);
            BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);

            new ConfigurationClassPostProcessor().processConfigBeanDefinitions(registry);

            for (String key : registry.getBeanDefinitionNames()) {
                BeanDefinition beanDefinition = registry.getBeanDefinition(key);

                if (!beanFactory.containsBean(key)) {
                    LOGGER.info("Bean[{}] be registered", key);

                    beanFactory.registerBeanDefinition(key, beanDefinition);
                }
            }
        }
    }

    /**
     * Apply scoped proxy mode bean definition holder.
     *
     * @param metadata   the metadata
     * @param definition the definition
     * @param registry   the registry
     * @return the bean definition holder
     */
    static BeanDefinitionHolder applyScopedProxyMode(ScopeMetadata metadata,
                                                     BeanDefinitionHolder definition,
                                                     BeanDefinitionRegistry registry) {

        ScopedProxyMode scopedProxyMode = metadata.getScopedProxyMode();
        if (scopedProxyMode.equals(ScopedProxyMode.NO)) {
            return definition;
        }
        boolean proxyTargetClass = scopedProxyMode.equals(ScopedProxyMode.TARGET_CLASS);
        return ScopedProxyCreator.createScopedProxy(definition, registry, proxyTargetClass);
    }

    /**
     * The type Scoped proxy creator.
     */
    static class ScopedProxyCreator {

        private ScopedProxyCreator() {
        }

        /**
         * Create scoped proxy bean definition holder.
         *
         * @param definitionHolder the definition holder
         * @param registry         the registry
         * @param proxyTargetClass the proxy target class
         * @return the bean definition holder
         */
        public static BeanDefinitionHolder createScopedProxy(BeanDefinitionHolder definitionHolder,
                                                             BeanDefinitionRegistry registry,
                                                             boolean proxyTargetClass) {

            return ScopedProxyUtils.createScopedProxy(definitionHolder, registry, proxyTargetClass);
        }
    }
}