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
package com.alipay.sofa.ark.container.service.biz;

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.ark.common.util.OrderComparator;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizInfo.StateChangeReason;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Service Implementation to manager ark biz
 *
 * @author ruoshan
 * @since 0.1.0
 */
@Singleton
public class BizManagerServiceImpl implements BizManagerService {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Biz>> bizRegistration = new ConcurrentHashMap<>();

    @Override
    public boolean registerBiz(Biz biz) {
        AssertUtils.assertNotNull(biz, "Biz must not be null.");
        AssertUtils.isTrue(biz.getBizState() == BizState.RESOLVED, "BizState must be RESOLVED.");
        // Two level cache here. First level cache key is biz name, and value is versions cache.
        // Second level cache key is version, value is biz model.
        bizRegistration.putIfAbsent(biz.getBizName(), new ConcurrentHashMap<>(16));
        ConcurrentHashMap<String, Biz> bizCache = bizRegistration.get(biz.getBizName());
        return bizCache.put(biz.getBizVersion(), biz) == null;
    }

    @Override
    public Biz unRegisterBiz(String bizName, String bizVersion) {
        AssertUtils.isTrue(getBizState(bizName, bizVersion) != BizState.RESOLVED,
            "Biz whose state is resolved must not be un-registered.");
        return unRegisterBizStrictly(bizName, bizVersion);
    }

    @Override
    public Biz unRegisterBizStrictly(String bizName, String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz version must not be empty.");
        ConcurrentHashMap<String, Biz> bizCache = bizRegistration.get(bizName);
        if (bizCache != null) {
            return bizCache.remove(bizVersion);
        }
        return null;
    }

    @Override
    public List<Biz> getBiz(String bizName) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        ConcurrentHashMap<String, Biz> bizCache = bizRegistration.get(bizName);
        List<Biz> bizList = new ArrayList<>();
        if (bizCache != null) {
            bizList.addAll(bizCache.values());
        }
        return bizList;
    }

    @Override
    public Biz getBiz(String bizName, String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz version must not be empty.");
        ConcurrentHashMap<String, Biz> bizCache = bizRegistration.get(bizName);
        if (bizCache != null) {
            return bizCache.get(bizVersion);
        }
        return null;
    }

    @Override
    public Biz getBizByIdentity(String bizIdentity) {
        AssertUtils.isTrue(BizIdentityUtils.isValid(bizIdentity),
            "Format of Biz Identity is error.");
        String[] str = bizIdentity.split(Constants.STRING_COLON);
        return getBiz(str[0], str[1]);
    }

    @Override
    public Biz getBizByClassLoader(ClassLoader classLoader) {
        for (Map.Entry<String, ConcurrentHashMap<String, Biz>> bizMapEntry : bizRegistration
            .entrySet()) {
            for (Map.Entry<String, Biz> bizEntry : bizMapEntry.getValue().entrySet()) {
                Biz biz = bizEntry.getValue();
                if (biz.getBizClassLoader().equals(classLoader)) {
                    return biz;
                }
            }
        }
        return null;
    }

    @Override
    public Set<String> getAllBizNames() {
        return bizRegistration.keySet();
    }

    @Override
    public Set<String> getAllBizIdentities() {
        Set<String> bizIdentities = new HashSet<>();
        for (Biz biz : getBizInOrder()) {
            bizIdentities.add(biz.getIdentity());
        }
        return bizIdentities;
    }

    @Override
    public List<Biz> getBizInOrder() {
        List<Biz> bizList = new ArrayList<>();
        for (String bizName : bizRegistration.keySet()) {
            bizList.addAll(bizRegistration.get(bizName).values());
        }
        Collections.sort(bizList, new OrderComparator());
        return bizList;
    }

    @Override
    public Biz getActiveBiz(String bizName) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        Map<String, Biz> bizCache = bizRegistration.get(bizName);
        if (bizCache != null) {
            for (Biz biz : bizCache.values()) {
                if (biz.getBizState() == BizState.ACTIVATED) {
                    return biz;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isActiveBiz(String bizName, String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz version must not be empty.");
        Map<String, Biz> bizCache = bizRegistration.get(bizName);
        if (bizCache != null) {
            Biz biz = bizCache.get(bizVersion);
            return biz != null && (biz.getBizState() == BizState.ACTIVATED);
        }
        return false;
    }

    @Override
    public void activeBiz(String bizName, String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz version must not be empty.");
        Biz biz = getBiz(bizName, bizVersion);
        Biz activeBiz = getActiveBiz(bizName);
        if (biz != null && biz.getBizState() == BizState.DEACTIVATED) {
            if (activeBiz != null) {
                ((BizModel) activeBiz).setBizState(BizState.DEACTIVATED,
                    StateChangeReason.SWITCHED,
                    String.format("switch to new version %s", biz.getIdentity()));
            }
            String message = activeBiz == null ? "" : String.format("switch from old version: %s",
                activeBiz.getIdentity());
            ((BizModel) biz).setBizState(BizState.ACTIVATED, StateChangeReason.SWITCHED, message);
        }
    }

    @Override
    public BizState getBizState(String bizName, String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz name must not be empty.");
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz version must not be empty.");
        Map<String, Biz> bizCache = bizRegistration.get(bizName);
        if (bizCache != null) {
            Biz biz = bizCache.get(bizVersion);
            return biz != null ? biz.getBizState() : BizState.UNRESOLVED;
        }
        return BizState.UNRESOLVED;
    }

    @Override
    public BizState getBizState(String bizIdentity) {
        AssertUtils.isTrue(BizIdentityUtils.isValid(bizIdentity),
            "Format of Biz Identity is error.");
        String[] str = bizIdentity.split(Constants.STRING_COLON);
        return getBizState(str[0], str[1]);
    }

    @Override
    public boolean removeAndAddBiz(Biz addingBiz, Biz removingBiz) {
        Set<Map.Entry<String, ConcurrentHashMap<String, Biz>>> bizEntrySet = bizRegistration.entrySet();
        bizEntrySet.forEach(item -> {
            String bizName = item.getKey();
            if (removingBiz.getBizName().equals(bizName)){
                bizEntrySet.remove(item);
                return;
            }
        });
        bizRegistration.putIfAbsent(addingBiz.getBizName(), new ConcurrentHashMap<>(16));
        return bizRegistration.get(addingBiz.getBizName()).put(addingBiz.getBizVersion(), addingBiz) == null;
    }

    @Override
    public ConcurrentHashMap<String, ConcurrentHashMap<String, Biz>> getBizRegistration() {
        return bizRegistration;
    }
}
