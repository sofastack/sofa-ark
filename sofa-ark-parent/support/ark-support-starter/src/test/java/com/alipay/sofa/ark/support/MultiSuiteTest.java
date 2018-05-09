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

import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import com.alipay.sofa.ark.support.listener.ArkTestNGAlterSuiteListener;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Collections;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class MultiSuiteTest {

    private XmlSuite clown  = new XmlSuite();
    private XmlSuite parent = new XmlSuite();
    private XmlSuite child  = new XmlSuite();

    @BeforeMethod
    public void init() {
        clown.setParentSuite(parent);
        clown.getChildSuites().add(child);

        clown.setTests(generateXmlTest(MultiSuiteTest.class));
        parent.setTests(generateXmlTest(TestNGOnArkTest.class));
        child.setTests(generateXmlTest(TestNGCommonTest.class));
    }

    protected List<XmlTest> generateXmlTest(Class testClass) {
        XmlTest xmlTest = new XmlTest();
        XmlClass xmlClass = new XmlClass();
        xmlClass.setClass(testClass);
        xmlTest.setClasses(Collections.singletonList(xmlClass));
        return Collections.singletonList(xmlTest);
    }

    @Test
    public void test() {
        new ArkTestNGAlterSuiteListener().alter(Collections.singletonList(clown));
        Assert.assertTrue(clown.getTests().get(0).getClasses().get(0).getSupportClass()
            .getClassLoader().equals(ClassLoader.getSystemClassLoader()));
        Assert.assertTrue(parent.getTests().get(0).getClasses().get(0).getSupportClass()
            .getClassLoader().equals(DelegateArkContainer.getTestClassLoader()));
        Assert.assertTrue(child.getTests().get(0).getClasses().get(0).getSupportClass()
            .getClassLoader().equals(ClassLoader.getSystemClassLoader()));
    }

}