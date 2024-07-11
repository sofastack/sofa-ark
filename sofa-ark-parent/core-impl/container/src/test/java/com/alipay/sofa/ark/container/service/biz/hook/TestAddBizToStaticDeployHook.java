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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.loader.JarBizArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.biz.AddBizToStaticDeployHook;
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
public class TestAddBizToStaticDeployHook implements AddBizToStaticDeployHook {

    @Override
    public List<BizArchive> getStaticBizToAdd() throws Exception {
        List<BizArchive> archives = new ArrayList<>();
        File bizFile = new File(this.getClass().getClassLoader()
            .getResource("sample-ark-1.0.0-ark-biz.jar").toURI());
        archives.add(new JarBizArchive(new JarFileArchive(bizFile)));
        return archives;
    }

}