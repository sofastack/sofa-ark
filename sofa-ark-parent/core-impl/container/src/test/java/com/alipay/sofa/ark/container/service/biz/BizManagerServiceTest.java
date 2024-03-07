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
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static com.alipay.sofa.ark.spi.model.BizState.ACTIVATED;
import static com.alipay.sofa.ark.spi.model.BizState.RESOLVED;
import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.junit.Assert.*;

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
            .setBizState(RESOLVED);
        bizManagerService.registerBiz(biz);
    }

    @Test
    public void testRegisterBiz() {
        Biz ret = bizManagerService.getBiz("test-biz", "1.0.0");
        assertNotNull(ret);
    }

    @Test
    public void testDuplicatedRegisterBiz() {
        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.0")
            .setBizState(RESOLVED);
        assertFalse(bizManagerService.registerBiz(biz));
        assertEquals(1, bizManagerService.getBiz("test-biz").size());
    }

    @Test
    public void testRemovingAndAddBiz() {

        Biz adding = new BizModel().setBizName("test-biz-adding").setBizVersion("1.0.0")
            .setBizState(ACTIVATED);
        Biz removing = new BizModel().setBizName("test-biz-removing").setBizVersion("1.0.0")
            .setBizState(RESOLVED);

        bizManagerService.registerBiz(removing);
        ((BizModel) removing).setBizState(ACTIVATED);
        bizManagerService.removeAndAddBiz(adding, removing);

        List<Biz> biz = bizManagerService.getBiz("test-biz-adding");
        assertTrue(biz.size() == 1);

        biz = bizManagerService.getBiz("test-biz-removing");
        assertTrue(biz.size() == 0);
        bizManagerService.unRegisterBiz(adding.getBizName(), adding.getBizVersion());
    }

    @Test
    public void testUnRegister() {

        Biz biz = bizManagerService.unRegisterBiz("test-biz", "1.0.1");
        assertNull(biz);
        assertTrue(bizManagerService.getBiz("test-biz").size() == 1);
        biz = bizManagerService.unRegisterBizStrictly("test-biz", "1.0.0");
        assertNotNull(biz);
        assertTrue(bizManagerService.getBiz("test-biz").size() == 0);

        bizManagerService.registerBiz(biz);
        assertTrue(bizManagerService.getBiz("test-biz").size() == 1);
    }

    @Test
    public void testBizGet() {

        Biz biz = bizManagerService.getBizByIdentity("test-biz:1.0.0");
        assertNotNull(biz);

        Set<String> stringSet = bizManagerService.getAllBizNames();
        assertTrue(stringSet.size() == 1);
        assertTrue(stringSet.contains("test-biz"));

        biz = bizManagerService.getActiveBiz("test-biz");
        assertNull(biz);

        BizState bizState = bizManagerService.getBizState("test-biz:1.0.0");
        assertTrue(bizState == RESOLVED);
        bizState = bizManagerService.getBizState("test-biz", "1.0.0");
        assertTrue(bizState == RESOLVED);
        bizState = bizManagerService.getBizState("ss", "xx");
        assertTrue(bizState == BizState.UNRESOLVED);

        biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.1").setBizState(RESOLVED)
            .setPriority("10");
        bizManagerService.registerBiz(biz);
        List<Biz> bizList = bizManagerService.getBizInOrder();
        assertTrue(bizList.size() == 2);
        assertTrue(bizList.get(0).getBizVersion().equals("1.0.1"));
        assertTrue(bizList.get(1).getBizVersion().equals("1.0.0"));

        biz = bizManagerService.getActiveBiz("test-biz");
        assertNull(biz);
        bizManagerService.activeBiz("test-biz", "1.0.1");
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == RESOLVED);
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == RESOLVED);

        biz = bizManagerService.getBiz("test-biz", "1.0.1");
        ((BizModel) biz).setBizState(BizState.DEACTIVATED);
        bizManagerService.activeBiz("test-biz", "1.0.1");
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == ACTIVATED);
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == RESOLVED);

        bizManagerService.activeBiz("test-biz", "1.0.0");
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == ACTIVATED);
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == RESOLVED);

        biz = bizManagerService.getBiz("test-biz", "1.0.0");
        ((BizModel) biz).setBizState(BizState.DEACTIVATED);
        bizManagerService.activeBiz("test-biz", "1.0.0");
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.0") == ACTIVATED);
        assertTrue(bizManagerService.getBizState("test-biz", "1.0.1") == BizState.DEACTIVATED);
    }

    @Test(expected = ArkRuntimeException.class)
    public void testDeployWithException() throws IllegalAccessException {

        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.3")
            .setBizState(RESOLVED).setPriority("10");
        bizManagerService.registerBiz(biz);

        DefaultBizDeployer defaultBizDeployer = new DefaultBizDeployer();
        setVariableValueInObject(defaultBizDeployer, "bizManagerService", bizManagerService);
        defaultBizDeployer.deploy();
    }

    @Test(expected = ArkRuntimeException.class)
    public void testUndeployWithException() throws IllegalAccessException {

        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.3")
            .setBizState(RESOLVED).setPriority("10");
        bizManagerService.registerBiz(biz);

        DefaultBizDeployer defaultBizDeployer = new DefaultBizDeployer();
        setVariableValueInObject(defaultBizDeployer, "bizManagerService", bizManagerService);
        defaultBizDeployer.unDeploy();
    }

    @Test
    public void testIsActiveBiz() {

        bizManagerService = new BizManagerServiceImpl();
        assertNull(bizManagerService.getBizByClassLoader(this.getClass().getClassLoader()));

        Biz biz = new BizModel().setBizName("test-biz").setBizVersion("1.0.1")
            .setBizState(RESOLVED).setPriority("10");
        bizManagerService.registerBiz(biz);

        assertFalse(bizManagerService.isActiveBiz("test-biz", "1.0.1"));
        assertFalse(bizManagerService.isActiveBiz("test-biz", "2.0.1"));
        assertNotNull(bizManagerService.getBizRegistration());
    }
}
