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
package com.alipay.sofa.ark.spi.registry;

/**
 * Service Metadata which contains Service metadata information
 * Service is unique by service anme and plugin name
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface ServiceMetadata {

    /**
     * get service id, different service implementation of same interface can be
     * recognised by uniqueId
     *
     * @return service name
     */
    String getUniqueId();

    /**
     * get Service Interface Class
     * @return interface class
     */
    Class<?> getInterfaceClass();

    /**
     * get ServiceProvider
     * @return
     */
    ServiceProvider getServiceProvider();

    /**
     * Service name, generally speaking, it's combined by {@link ServiceMetadata#getUniqueId()} and
     * {@link ServiceMetadata#getInterfaceClass}
     *
     * @return
     */
    String getServiceName();

}