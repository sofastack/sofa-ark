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

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.common.util.ClassUtils.*;
import static com.alipay.sofa.ark.spi.constant.Constants.DEFAULT_PACKAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ClassUtilsTest {

    @Test
    public void testGetPackageName() {
        assertEquals("a.b", getPackageName("a.b.C"));
        assertEquals(DEFAULT_PACKAGE, getPackageName("C"));
    }

    @Test
    public void testFindCommonPackage() {
        assertEquals(findCommonPackage(null).size(), 0);
        List<String> classNames = new ArrayList<>();
        classNames.add("com.example.project.subpackage1.classE");
        classNames.add("com.example.project.classA");
        classNames.add("com.example.project.classB");
        classNames.add("com.example.project.subpackage.classC");
        classNames.add("com.example.project.subpackage.classD");
        assertEquals(findCommonPackage(classNames).size(), 3);
        classNames.add("org.apache.util.ClassF");
        assertEquals(findCommonPackage(classNames).size(), 4);
    }

    @Test
    public void testCollectClasses() throws Exception {

        File dir = new File("target/classes");
        // fix mvn test fail issues
        File dir2 = new File(dir.getAbsolutePath());
        if (!dir2.exists()) {
            return;
        }

        List<String> classNames = collectClasses(dir2);
        assertTrue(classNames.contains("com.alipay.sofa.ark.common.util.ClassUtils"));
        assertTrue(findCommonPackage(classNames).contains("com.alipay.sofa.ark.common.util"));
    }
}
