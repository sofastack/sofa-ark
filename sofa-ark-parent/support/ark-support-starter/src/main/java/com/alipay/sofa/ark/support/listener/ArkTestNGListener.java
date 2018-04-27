/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.ark.support.listener;

import org.testng.IAlterSuiteListener;
import org.testng.IExecutionListener;
import org.testng.xml.XmlSuite;

import java.util.List;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ArkTestNGListener implements IExecutionListener, IAlterSuiteListener {

    @Override
    public void onExecutionStart() {
        System.out.println("Start");
    }

    @Override
    public void onExecutionFinish() {
        System.out.println("Finish");
    }

    @Override
    public void alter(List<XmlSuite> suites) {
        System.out.println("suites\n");
    }
}