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
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.loader.DirectoryBizArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ClasspathLauncherTest {

    static {
        new MockUp<ManagementFactory>() {
            @Mock
            public RuntimeMXBean getRuntimeMXBean() {
                return new MockUp<RuntimeMXBean>() {
                    @Mock
                    List<String> getInputArguments() {
                        List<String> mockArguments = new ArrayList<>();
                        String filePath = this.getClass().getClassLoader()
                            .getResource("SampleClass.class").getPath();
                        String workingPath = new File(filePath).getParent();
                        mockArguments.add(String.format("-javaagent:%s", workingPath));
                        return mockArguments;
                    }
                }.getMockInstance();
            }
        };
    }

    @Test
    public void testFilterAgentClasspath() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        Assert.assertEquals(1, agentUrl.length);

        List<URL> urls = new ArrayList<>();
        urls.add(url);
        urls.addAll(Arrays.asList(agentUrl));

        ClasspathLauncher.ClassPathArchive classPathArchive = new ClasspathLauncher.ClassPathArchive(
            urls.toArray(new URL[] {}));
        List<BizArchive> bizArchives = classPathArchive.getBizArchives();
        Assert.assertEquals(2, bizArchives.size());

        DirectoryBizArchive directoryBizArchive = null;
        for (BizArchive bizArchive : bizArchives) {
            if (bizArchive instanceof DirectoryBizArchive) {
                directoryBizArchive = (DirectoryBizArchive) bizArchive;
            }
        }
        Assert.assertNotNull(directoryBizArchive);
        Assert.assertEquals(2, urls.size());
        Assert.assertEquals(1, directoryBizArchive.getUrls().length);
    }

}