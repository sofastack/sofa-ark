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
package com.alipay.sofa.ark.boot.mojo;

import com.alipay.sofa.ark.tools.ArtifactItem;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Set;

public class MavenUtilsTest {

    @Test
    public void getRootProject() {
    }

    @Test
    public void parseDependencyTree() {
        String content = "com.alipay.bgrowth:bgrowthcomplete-parent:pom:1.0.0-SNAPSHOT\n"
                         + "+- io.sofastack:dynamic-stock-mng:jar:ark-biz:1.0.0:compile (optional) \n"
                         + "+- com.alipay.sofa:healthcheck-sofa-boot-starter:jar:3.11.1:provided (optional)\n"
                         + "|  +- com.alipay.sofa:healthcheck-sofa-boot:jar:3.11.1:provided\n"
                         + "|  |  +- com.alipay.sofa:sofa-boot:jar:3.11.1:provided\n"
                         + "|  |  |  \\- com.alipay.sofa:log-sofa-boot-starter:jar:3.11.1:provided\n"
                         + "|  |  |     \\- com.alipay.sofa:log-sofa-boot:jar:3.11.1:provided\n"
                         + "|  |  \\- com.alipay.sofa:runtime-sofa-boot:jar:3.11.1:provided\n"
                         + "|  |     \\- com.alipay.sofa:sofa-ark-spi:jar:2.0.2:provided\n"
                         + "|  |        \\- com.alipay.sofa:sofa-ark-exception:jar:2.0.2:provided\n"
                         + "|  +- com.alipay.sofa:sofa-boot-autoconfigure:jar:3.11.1:provided\n"
                         + "|  |  \\- org.springframework.boot:spring-boot-autoconfigure:jar:2.3.12.RELEASE:provided\n";

        Set<ArtifactItem> artifactItemSet = MavenUtils.convert(content);
        Assert.assertEquals(11, artifactItemSet.size());
    }
}