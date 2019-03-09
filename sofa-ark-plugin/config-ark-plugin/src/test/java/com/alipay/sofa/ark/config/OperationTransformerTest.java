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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.config.util.OperationTransformer;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.PluginContext;
import com.alipay.sofa.ark.spi.registry.ServiceReference;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class OperationTransformerTest {

    @Test
    public void testBizFilePath() {
        File file = ArkClient.createBizSaveFile("name", "version");
        Assert.assertTrue(StringUtils.isEmpty(ArkConfigs
            .getStringValue(Constants.CONFIG_INSTALL_BIZ_DIR)));
        Assert.assertTrue(file.getAbsolutePath().contains(
            "sofa-ark" + File.separator + "name-version-"));
    }

    @Test
    public void testTransformFormatError() {
        Assert.assertFalse(OperationTransformer.isValidConfig("aaa"));
        Assert.assertFalse(OperationTransformer.isValidConfig("name:version:resolved"));
        Assert.assertFalse(OperationTransformer.isValidConfig("name:version:activated?url"));
        Assert.assertTrue(OperationTransformer
            .isValidConfig("name:version:activated?url=http://xx"));
        Assert.assertTrue(OperationTransformer
            .isValidConfig("name:version:activated?url=http://xx&param2=value2"));
        String config = "k1=v1&k2=v2";
        Map<String, String> params = OperationTransformer.parseParameter(config);
        Assert.assertEquals(2, params.size());
        Assert.assertEquals("v1", params.get("k1"));
        Assert.assertEquals("v2", params.get("k2"));
    }

    @Test
    public void testTransformConfigOperationWithConflictState() {
        Map<String, Map<String, BizState>> currentBizState = new HashMap<>();
        Exception ex = null;
        List<BizOperation> operations = null;
        try {
            operations = OperationTransformer.doTransformToBizOperation(
                "n1:v1:activated;n1:v1:deactivated", currentBizState);
        } catch (IllegalStateException e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getMessage()
            .contains("Don't specify same biz with different bizState"));
    }

    @Test
    public void testTransformUninstallConfigOperation() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation("",
            mockBizState());
        Assert.assertEquals(3, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vA")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vA")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
    }

    @Test
    public void testTransformConfigOperationWithMultiActivateState() {
        Map<String, Map<String, BizState>> currentBizState = new HashMap<>();
        Exception ex = null;
        List<BizOperation> operations = null;
        try {
            operations = OperationTransformer.doTransformToBizOperation(
                "n1:v1:activated;n1:v2:activated", currentBizState);
        } catch (IllegalStateException e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getMessage().contains(
            "Don't allow multi biz with same bizName to be active"));
    }

    @Test
    public void testTransformConfigOperationNotAllowed() {
        Map<String, Map<String, BizState>> currentBizState = new HashMap<>();
        Exception ex = null;
        List<BizOperation> operations = null;
        try {
            operations = OperationTransformer.doTransformToBizOperation(
                "n1:v1:deactivated;n1:v2:deactivated", currentBizState);
        } catch (IllegalStateException e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getMessage().contains("cant be transform to"));
    }

    @Test
    public void testTransformUnInstallOperation() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated", mockBizState());
        Assert.assertEquals(2, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vA")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
    }

    @Test
    public void testTransformInstallOperation() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated;nameA:vB:deactivated;nameB:vA:activated;nameB:vB:deactivated",
            mockBizState());
        Assert.assertEquals(1, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.INSTALL)));
    }

    @Test
    public void testTransformInstallAndUninstallOperation() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated;nameB:vA:activated;nameB:vB:deactivated", mockBizState());
        Assert.assertEquals(2, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.INSTALL)));
    }

    @Test
    public void testTransformSwitchOperation() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:deactivated;nameA:vB:activated;nameB:vA:activated", mockBizState());
        Assert.assertEquals(1, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.SWITCH)));
    }

    @Test
    public void testTransformInstallAndSwitch() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated;nameA:vB:deactivated;nameB:vA:deactivated;nameB:vB:activated",
            mockBizState());
        Assert.assertEquals(2, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.INSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.SWITCH)));
    }

    @Test
    public void testTransformUninstallAndSwitch() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vB:activated;nameB:vA:activated", mockBizState());
        Assert.assertEquals(2, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vA")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameA").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.SWITCH)));
    }

    @Test
    public void testTransformUninstallAndInstall() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated;nameA:vB:deactivated;nameB:vB:activated", mockBizState());
        Assert.assertEquals(3, bizOperations.size());
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.INSTALL)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vB")
            .setOperationType(BizOperation.OperationType.SWITCH)));
        Assert.assertTrue(bizOperations.contains(BizOperation.createBizOperation()
            .setBizName("nameB").setBizVersion("vA")
            .setOperationType(BizOperation.OperationType.UNINSTALL)));
    }

    @Test
    public void testTransformUnstableState() {
        Exception ex = null;
        try {
            List<Biz> bizList = new ArrayList<>();
            Biz biz1 = Mockito.mock(Biz.class);
            Biz biz2 = Mockito.mock(Biz.class);
            Biz biz3 = Mockito.mock(Biz.class);
            bizList.add(biz1);
            bizList.add(biz2);
            bizList.add(biz3);
            when(biz1.getBizName()).thenReturn("nameA");
            when(biz1.getBizVersion()).thenReturn("vA");
            when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);

            when(biz1.getBizName()).thenReturn("nameA");
            when(biz1.getBizVersion()).thenReturn("vB");
            when(biz1.getBizState()).thenReturn(BizState.RESOLVED);

            when(biz1.getBizName()).thenReturn("nameB");
            when(biz1.getBizVersion()).thenReturn("vA");
            when(biz1.getBizState()).thenReturn(BizState.ACTIVATED);

            PluginContext pluginContext = Mockito.mock(PluginContext.class);
            ServiceReference serviceReference = Mockito.mock(ServiceReference.class);
            BizManagerService bizManagerService = Mockito.mock(BizManagerService.class);
            when(serviceReference.getService()).thenReturn(bizManagerService);
            when(pluginContext.referenceService(any(Class.class))).thenReturn(serviceReference);
            when(bizManagerService.getBizInOrder()).thenReturn(bizList);
            OperationTransformer.transformToBizOperation(
                "nameA:vA:activated;nameA:vB:deactivated;nameB:vB:activated", pluginContext);
        } catch (Exception e) {
            ex = e;
        }
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex.getMessage().contains("Exist illegal biz"));
    }

    @Test
    public void testTransformUnChangedState() {
        List<BizOperation> bizOperations = OperationTransformer.doTransformToBizOperation(
            "nameA:vA:activated;nameA:vB:deactivated;nameB:vA:activated", mockBizState());
        Assert.assertEquals(0, bizOperations.size());
    }

    private Map<String, Map<String, BizState>> mockBizState() {
        Map<String, Map<String, BizState>> bizStateMap = new LinkedHashMap<String, Map<String, BizState>>();

        Map<String, BizState> bizAVersionStateMap = new HashMap<String, BizState>();
        bizAVersionStateMap.put("vA", BizState.ACTIVATED);
        bizAVersionStateMap.put("vB", BizState.DEACTIVATED);
        bizStateMap.put("nameA", bizAVersionStateMap);

        Map<String, BizState> bizBVersionStateMap = new HashMap<String, BizState>();
        bizBVersionStateMap.put("vA", BizState.ACTIVATED);
        bizStateMap.put("nameB", bizBVersionStateMap);

        return bizStateMap;
    }
}
