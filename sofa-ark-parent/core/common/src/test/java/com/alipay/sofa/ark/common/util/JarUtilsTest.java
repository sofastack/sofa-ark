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
package com.alipay.sofa.ark.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

public class JarUtilsTest {

    @Test
    public void getArtifactIdFromTestClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        String artifactId = JarUtils.getArtifactIdFromLocalClassPath(url.getPath());
        Assert.assertEquals("sofa-ark-common", artifactId);
    }

    @Test
    public void getArtifactIdFromClassPath() throws IOException {
        URL clazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/common/util/JarUtils.class");

        String artifactId = JarUtils.getArtifactIdFromLocalClassPath(clazzURL.getPath());
        Assert.assertEquals("sofa-ark-common", artifactId);
    }

    @Test
    public void testParseArtifactId() {
        String filePathPrefix = "file:///home/admin/xxx/xxx/%s.jar";
        String artifactId1 = JarUtils.getArtifactId(String.format(filePathPrefix,
            "dafdfa-2-dafdfad"));
        Assert.assertEquals(artifactId1, "dafdfa-2-dafdfad");
        String artifactId2 = JarUtils.getArtifactId(String.format(filePathPrefix,
            "dfadfa-dfadfa-3.0"));
        Assert.assertEquals(artifactId2, "dfadfa-dfadfa");
        String artifactId3 = JarUtils.getArtifactId(String.format(filePathPrefix,
            "hessian-4.0.7.bugfix12-tuning3"));
        Assert.assertEquals(artifactId3, "hessian");
    }
}