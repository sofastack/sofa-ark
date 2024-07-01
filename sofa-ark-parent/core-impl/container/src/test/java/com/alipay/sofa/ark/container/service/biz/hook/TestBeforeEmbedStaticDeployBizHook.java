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
package com.alipay.sofa.ark.container.service.biz.hook;

import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import com.alipay.sofa.ark.spi.service.biz.BeforeEmbedStaticDeployBizHook;
import com.alipay.sofa.ark.spi.service.extension.Extension;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: TestBeforeEmbedStaticDeployBizHook.java, v 0.1 2024年06月27日 16:36 立蓬 Exp $
 */
@Extension("before-embed-static-deploy-biz-hook")
public class TestBeforeEmbedStaticDeployBizHook implements BeforeEmbedStaticDeployBizHook {

    //@Override
    //public void beforeStaticDeploy(BizFactoryService bizFactoryService,
    //                               BizManagerService bizManagerService) {
    //    BizModel biz = new BizModel().setBizState(BizState.RESOLVED).setBizVersion("mockVersion")
    //        .setBizName("mockName");
    //    Biz spyBiz = Mockito.spy(biz);
    //    try {
    //        doNothing().when(spyBiz).start(any(), any());
    //        doNothing().when(spyBiz).start(any());
    //    } catch (Throwable e) {
    //    }
    //
    //    bizManagerService.registerBiz(spyBiz);
    //}

    @Override
    public List<File> getStaticBizFilesToAdd() throws Exception {
        return Lists.newArrayList(new File(this.getClass().getClassLoader()
            .getResource("sample-ark-1.0.0-ark-biz.jar").toURI()));
    }

}