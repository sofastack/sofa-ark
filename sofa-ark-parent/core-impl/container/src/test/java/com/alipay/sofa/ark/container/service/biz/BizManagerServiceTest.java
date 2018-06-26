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

import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public class BizManagerServiceTest extends BaseTest {

    private BizManagerService bizManagerService = new BizManagerServiceImpl();

    @Before
    public void before() {
        super.before();
        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.0")
            .setBizState(BizState.RESOLVED);
        bizManagerService.registerBiz(biz);
    }

    @Test
    public void testRegisterBiz() {
        Biz ret = bizManagerService.getBiz("test-biz", "1.0.0");
        Assert.assertNotNull(ret);
    }

    @Test
    public void testDuplicatedRegisterBiz() {
        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.0")
            .setBizState(BizState.RESOLVED);
        Assert.assertFalse(bizManagerService.registerBiz(biz));
        Assert.assertTrue(bizManagerService.getBiz("test-biz").size() == 1);
    }

    @Test
    public void testUnRegister() {
        Biz biz = bizManagerService.unRegisterBiz("test-biz", "1.0.1");
        Assert.assertNull(biz);
        Assert.assertTrue(bizManagerService.getBiz("test-biz").size() == 1);
        biz = bizManagerService.unRegisterBizStrictly("test-biz", "1.0.0");
        Assert.assertNotNull(biz);
        Assert.assertTrue(bizManagerService.getBiz("test-biz").size() == 0);

        bizManagerService.registerBiz(biz);
        Assert.assertTrue(bizManagerService.getBiz("test-biz").size() == 1);
    }

    @Test
    public void testBizGet() {
        Biz biz = bizManagerService.getBizByIdentity("test-biz:1.0.0");
        Assert.assertNotNull(biz);

        Set<String> stringSet = bizManagerService.getAllBizNames();
        Assert.assertTrue(stringSet.size() == 1);
        Assert.assertTrue(stringSet.contains("test-biz"));

        biz = bizManagerService.getActiveBiz("test-biz");
        Assert.assertNull(biz);

        BizState bizState = bizManagerService.getBizState("test-biz:1.0.0");
        Assert.assertTrue(bizState == BizState.RESOLVED);
        bizState = bizManagerService.getBizState("test-biz", "1.0.0");
        Assert.assertTrue(bizState == BizState.RESOLVED);
        bizState = bizManagerService.getBizState("ss", "xx");
        Assert.assertTrue(bizState == BizState.UNRESOLVED);

        biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.1")
            .setBizState(BizState.RESOLVED).setPriority("10");
        bizManagerService.registerBiz(biz);
        List<Biz> bizList = bizManagerService.getBizInOrder();
        Assert.assertTrue(bizList.size() == 2);
        Assert.assertTrue(bizList.get(0).getBizVersion().equals("1.0.1"));
        Assert.assertTrue(bizList.get(1).getBizVersion().equals("1.0.0"));

        biz = bizManagerService.getActiveBiz("test-biz");
        Assert.assertNull(biz);
        bizManagerService.activeBiz("test-biz", "1.0.1");
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == BizState.RESOLVED);
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == BizState.RESOLVED);

        biz = bizManagerService.getBiz("test-biz", "1.0.1");
        ((BizModel) biz).setBizState(BizState.DEACTIVATED);
        bizManagerService.activeBiz("test-biz", "1.0.1");
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == BizState.ACTIVATED);
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == BizState.RESOLVED);

        bizManagerService.activeBiz("test-biz", "1.0.0");
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == BizState.ACTIVATED);
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == BizState.RESOLVED);

        biz = bizManagerService.getBiz("test-biz", "1.0.0");
        ((BizModel) biz).setBizState(BizState.DEACTIVATED);
        bizManagerService.activeBiz("test-biz", "1.0.0");
        Assert.assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == BizState.ACTIVATED);
        Assert
            .assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == BizState.DEACTIVATED);

    }

}