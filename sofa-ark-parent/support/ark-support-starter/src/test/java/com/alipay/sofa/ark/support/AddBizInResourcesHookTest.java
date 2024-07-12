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
package com.alipay.sofa.ark.support;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.support.common.AddBizInResourcesHook;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: AddBizInResourcesHookTest.java, v 0.1 2024年07月10日 19:56 立蓬 Exp $
 */
public class AddBizInResourcesHookTest {
    AddBizInResourcesHook addBizInResourcesHook = new AddBizInResourcesHook();

    @Test
    public void testGetStaticBizToAdd() throws Exception {
        // case1: ark is not embed
        ArkConfigs.setEmbedEnable(false);
        assertEquals(0,addBizInResourcesHook.getStaticBizToAdd().size());

        // case2: ark is embed but set 'EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE' as false
        ArkConfigs.setEmbedEnable(true);
        ArkConfigs.putStringValue(Constants.EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE, "false");

        // config master biz
        Biz masterBiz = createTestBizModel("test", "1.0.0", BizState.ACTIVATED, this.getClass().getClassLoader());

        try (MockedStatic<ArkClient> mockedStatic = Mockito.mockStatic(ArkClient.class)){
            mockedStatic.when(ArkClient::getMasterBiz).thenReturn(masterBiz);
            List<BizArchive> bizArchives = addBizInResourcesHook.getStaticBizToAdd();
            assertEquals(0, bizArchives.size());
        }finally {
            ArkConfigs.setEmbedEnable(false);
        }


        // case3: ark is embed and set 'EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE' as true
        ArkConfigs.setEmbedEnable(true);
        ArkConfigs.putStringValue(Constants.EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE, "true");

        try (MockedStatic<ArkClient> mockedStatic = Mockito.mockStatic(ArkClient.class)){
            mockedStatic.when(ArkClient::getMasterBiz).thenReturn(masterBiz);
            List<BizArchive> bizArchives = addBizInResourcesHook.getStaticBizToAdd();
            assertEquals(1, bizArchives.size());
        }finally {
            ArkConfigs.setEmbedEnable(false);
        }
    }

    private BizModel createTestBizModel(String bizName, String bizVersion, BizState bizState,
                                        ClassLoader classLoader) {
        BizModel bizModel = new BizModel().setBizState(bizState);
        bizModel.setBizName(bizName).setBizVersion(bizVersion);
        bizModel.setClassLoader(classLoader);
        return bizModel;
    }
}