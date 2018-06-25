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
package com.alipay.sofa.ark.container.service.injection;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.ReflectionUtils;
import com.alipay.sofa.ark.common.util.ReflectionUtils.FieldCallback;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.google.inject.Singleton;

import java.lang.reflect.Field;

/**
 * {@link InjectionService}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
@Singleton
public class InjectionServiceImpl implements InjectionService {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    @Override
    public void inject(final ServiceReference reference) {

        Class implClass = reference.getService().getClass();

        ReflectionUtils.doWithFields(implClass, new FieldCallback() {
            @Override
            public void doWith(Field field) throws ArkException {
                ArkInject arkInjectAnnotation = field.getAnnotation(ArkInject.class);
                if (arkInjectAnnotation == null) {
                    return;
                }

                Class<?> serviceType = field.getType();
                Object value = getService(serviceType);

                if (value == null) {
                    LOGGER.warn(
                        String.format("Inject {field=\'%s\'} of {service=\'%s\'} fail!",
                            field.getName(), reference.toString()), field.getName(),
                        reference.toString());
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                try {
                    field.set(reference.getService(), value);
                    LOGGER.info(
                        String.format("Inject {field=\'%s\'} of {service=\'%s\'} success!",
                            field.getName(), reference.toString()), field.getName(),
                        reference.toString());
                } catch (Throwable throwable) {
                    throw new ArkException(throwable);
                }
            }
        });
    }

    private Object getService(Class serviceType) {
        if (serviceType.equals(BizManagerService.class)
            || serviceType.equals(BizFactoryService.class)) {
            return ArkServiceContainerHolder.getContainer().getService(serviceType);
        }
        return null;
    }
}