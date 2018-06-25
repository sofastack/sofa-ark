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
package com.alipay.sofa.ark.spi.service.biz;

/**
 * BizDeployer to deploy Biz
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface BizDeployer {
    /**
     * Initialize biz deployer
     *
     * @param args command line arguments
     */
    void init(String[] args);

    /**
     * Start to deploy biz
     */
    void deploy();

    /**
     * un-deploy biz, whose resources and service would be unloaded.
     */
    void unDeploy();

    /**
     * Get description of biz deployer
     *
     * @return description
     */
    String getDesc();
}