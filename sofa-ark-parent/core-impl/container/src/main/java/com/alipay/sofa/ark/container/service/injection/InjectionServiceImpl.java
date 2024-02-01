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

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.ReflectionUtils;
import com.alipay.sofa.ark.common.util.ReflectionUtils.FieldCallback;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.google.inject.Inject;
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

    @Inject
    private RegistryService registryService;

    @Override
    public void inject(final ServiceReference reference) {
        inject(reference.getService(), reference.toString());
    }

    @Override
    public void inject(final Object object) {
        inject(object, object.getClass().getName());
    }

    private void inject(final Object instance, final String type) {
        ReflectionUtils.doWithFields(instance.getClass(), new FieldCallback() {
            @Override
            public void doWith(Field field) throws ArkRuntimeException {
                ArkInject arkInject = field.getAnnotation(ArkInject.class);
                if (arkInject == null) {
                    return;
                }

                Class<?> serviceType = arkInject.interfaceType() == void.class ? field.getType()
                    : arkInject.interfaceType();
                Object value = getService(serviceType, arkInject.uniqueId());

                if (value == null) {
                    ArkLoggerFactory.getDefaultLogger().warn(
                        String.format("Inject {field= %s} of {service= %s} fail!", field.getName(),
                            type));
                    return;
                }
                ReflectionUtils.makeAccessible(field);
                try {
                    field.set(instance, value);
                    ArkLoggerFactory.getDefaultLogger().info(
                        String.format("Inject {field= %s} of {service= %s} success!",
                            field.getName(), type));
                } catch (Throwable throwable) {
                    throw new ArkRuntimeException(throwable);
                }
            }
        });
    }

    private Object getService(Class serviceType, String uniqueId) {
        ServiceReference serviceReference = registryService.referenceService(serviceType, uniqueId);
        return serviceReference == null ? null : serviceReference.getService();
    }
}
