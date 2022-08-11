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

import static org.junit.Assert.*;

public class JarUtilsTest {

    @Test
    public void getArtifactIdFromTestClassPath() throws IOException {
        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        String artifactId = JarUtils.getArtifactIdFromClassPath(url.getPath());
        Assert.assertEquals("sofa-ark-common", artifactId);
    }

    @Test
    public void getArtifactIdFromClassPath() throws IOException {
        URL clazzURL = this.getClass().getClassLoader()
            .getResource("com/alipay/sofa/ark/common/util/JarUtils.class");

        String artifactId = JarUtils.getArtifactIdFromClassPath(clazzURL.getPath());
        Assert.assertEquals("sofa-ark-common", artifactId);
    }
}