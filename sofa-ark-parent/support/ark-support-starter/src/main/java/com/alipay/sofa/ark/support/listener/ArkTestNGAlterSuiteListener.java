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
package com.alipay.sofa.ark.support.listener;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.support.common.DelegateArkContainer;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.EMBED_ENABLE;
import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ArkTestNGAlterSuiteListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        for (XmlSuite xmlSuite : suites) {
            resetXmlSuite(xmlSuite);
            resetChildrenXmlSuite(xmlSuite.getChildSuites());
        }
    }

    protected void resetXmlSuite(XmlSuite suite) {
        if (suite == null) {
            return;
        }
        resetXmlSuite(suite.getParentSuite());
        resetSingleXmlSuite(suite);
    }

    protected void resetChildrenXmlSuite(List<XmlSuite> childSuites) {
        if (childSuites.isEmpty()) {
            return;
        }

        for (XmlSuite xmlSuite : childSuites) {
            resetChildrenXmlSuite(xmlSuite.getChildSuites());
            resetSingleXmlSuite(xmlSuite);
        }

    }

    protected void resetSingleXmlSuite(XmlSuite suite) {
        for (XmlTest xmlTest : suite.getTests()) {
            for (XmlClass xmlClass : xmlTest.getClasses()) {
                Class testClass = xmlClass.getSupportClass();
                if (testClass.getAnnotation(TestNGOnArk.class) != null) {
                    if (!DelegateArkContainer.isStarted()) {
                        DelegateArkContainer.launch(testClass);
                    }

                    try {
                        xmlClass.setClass(DelegateArkContainer.getTestClassLoader().loadClass(
                            testClass.getCanonicalName()));
                    } catch (ClassNotFoundException ex) {
                        throw new ArkRuntimeException(String.format(
                            "Load testNG test class %s failed.", testClass.getCanonicalName()), ex);
                    }
                } else if (testClass.getAnnotation(TestNGOnArkEmbeded.class) != null) {
                    if (!DelegateArkContainer.isStarted()) {
                        System.setProperty(EMBED_ENABLE, "true");
                        System.setProperty(MASTER_BIZ, "test master biz");
                        DelegateArkContainer.launch(testClass);
                        System.clearProperty(EMBED_ENABLE);
                        System.clearProperty(MASTER_BIZ);
                    }

                    try {
                        xmlClass.setClass(DelegateArkContainer.getTestClassLoader().loadClass(
                            testClass.getCanonicalName()));
                    } catch (ClassNotFoundException ex) {
                        throw new ArkRuntimeException(String.format(
                            "Load testNG test class %s failed.", testClass.getCanonicalName()), ex);
                    }
                }
            }
        }
    }

}
