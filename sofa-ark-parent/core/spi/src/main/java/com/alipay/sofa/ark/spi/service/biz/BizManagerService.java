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

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;

import java.util.List;
import java.util.Set;

/**
 * Service to manage biz
 *
 * @author ruoshan
 * @since 0.1.0
 */
public interface BizManagerService {

    /**
     * Register Biz
     *
     * @param biz
     * @return
     */
    boolean registerBiz(Biz biz);

    /**
     * Un-Register Biz, it requires the biz state must be {@link BizState#ACTIVATED}
     * or {@link BizState#DEACTIVATED} or {@link BizState#BROKEN}
     * @param bizName Biz Name
     * @param bizVersion Biz Version
     * @return Biz
     */
    Biz unRegisterBiz(String bizName, String bizVersion);

    /**
     * Un-Register Biz in strict mode, it ignores the biz state, generally invoked when install biz failed.
     * @param bizName Biz Name
     * @param bizVersion Biz Version
     * @return Biz
     */
    Biz unRegisterBizStrictly(String bizName, String bizVersion);

    /**
     * Get Biz List by name
     *
     * @param bizName
     * @return
     */
    List<Biz> getBiz(String bizName);

    /**
     * Get Biz determined by bizName and BizVersion
     *
     * @param bizName Biz Name
     * @param bizVersion Biz Version
     * @return
     */
    Biz getBiz(String bizName, String bizVersion);

    /**
     * Get Biz by identity id, an identity is usually consist of
     * biz name and biz version.
     *
     * @param bizIdentity
     * @return
     */
    Biz getBizByIdentity(String bizIdentity);

    /**
     * get All biz names
     *
     * @return
     */
    Set<String> getAllBizNames();

    /**
     * Get all biz in priority PriorityOrdered
     * @return
     */
    List<Biz> getBizInOrder();

    /**
     * Get active biz with given biz name whose state
     * is {@link BizState#ACTIVATED}
     *
     * @param bizName
     * @return
     */
    Biz getActiveBiz(String bizName);

    /**
     * Check whether the biz specified with a given name and a given version
     * is active {@link BizState#ACTIVATED}
     *
     * @param bizName
     * @param bizVersion
     * @return
     */
    boolean isActiveBiz(String bizName, String bizVersion);

    /**
     * Active biz with specified biz name and biz version.
     * @param bizName
     * @param bizVersion
     */
    void activeBiz(String bizName, String bizVersion);

    /**
     * Get {@link BizState} according to biz name and biz version.
     *
     * @param bizName
     * @param bizVersion
     * @return
     */
    BizState getBizState(String bizName, String bizVersion);

    /**
     * Get {@link BizState} according to biz identity.
     *
     * @param bizIdentity
     * @return
     */
    BizState getBizState(String bizIdentity);

}