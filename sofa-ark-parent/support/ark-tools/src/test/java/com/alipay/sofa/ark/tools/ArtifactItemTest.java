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
package com.alipay.sofa.ark.tools;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ArtifactItemTest.java, v 0.1 2024年08月07日 16:54 立蓬 Exp $
 */
public class ArtifactItemTest {

    @Test
    public void testParseArtifactItem() {
        // case1: {groupId:artifactId}
        ArtifactItem item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin");
        assertEquals("com.alipay.sofa", item.getGroupId());
        assertEquals("sofa-ark-plugin", item.getArtifactId());

        // case2: {groupId:artifactId:version}
        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0");
        assertEquals("com.alipay.sofa", item.getGroupId());
        assertEquals("sofa-ark-plugin", item.getArtifactId());
        assertEquals("1.0.0", item.getVersion());

        // case3: {groupId:artifactId:version:classifier}
        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0:jdk11");
        assertEquals("com.alipay.sofa", item.getGroupId());
        assertEquals("sofa-ark-plugin", item.getArtifactId());
        assertEquals("1.0.0", item.getVersion());
        assertEquals("jdk11", item.getClassifier());
    }

    @Test
    public void testIsSameWithVersion() {
        // case1: dependencyWithoutClassifier
        ArtifactItem dependencyWithoutClassifier = ArtifactItem
            .parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0");

        ArtifactItem item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin");
        assertTrue(item.isSameWithVersion(dependencyWithoutClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0");
        assertTrue(item.isSameWithVersion(dependencyWithoutClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.1");
        assertFalse(item.isSameWithVersion(dependencyWithoutClassifier));

        // case2: dependencyWithClassifier
        ArtifactItem dependencyWithClassifier = ArtifactItem
            .parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0:jdk11");

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0:jdk11");
        assertTrue(item.isSameWithVersion(dependencyWithClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.1:jdk11");
        assertFalse(item.isSameWithVersion(dependencyWithClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0:jdk12");
        assertFalse(item.isSameWithVersion(dependencyWithClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin:1.0.0");
        assertFalse(item.isSameWithVersion(dependencyWithClassifier));

        item = ArtifactItem.parseArtifactItem("com.alipay.sofa:sofa-ark-plugin");
        assertFalse(item.isSameWithVersion(dependencyWithClassifier));
    }

}