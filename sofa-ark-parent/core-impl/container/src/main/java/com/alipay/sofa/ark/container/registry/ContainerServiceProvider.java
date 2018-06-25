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
package com.alipay.sofa.ark.container.registry;

import com.alipay.sofa.ark.spi.registry.ServiceProviderType;

/**
 * Ark Container Service Provider, default service provider if provider is not set
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ContainerServiceProvider extends AbstractServiceProvider {

    private int order;

    public ContainerServiceProvider() {
        this(DEFAULT_PRECEDENCE);
    }

    public ContainerServiceProvider(int order) {
        super(ServiceProviderType.ARK_CONTAINER);
        this.order = order;
    }

    @Override
    public int getPriority() {
        return order;
    }
}