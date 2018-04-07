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
package com.alipay.sofa.ark.container.model;

import com.alipay.sofa.ark.bootstrap.MainMethodRunner;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.classloader.ClassloaderUtil;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;

import java.net.URL;
import java.util.Set;

/**
 * Ark Biz Standard Model
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BizModel implements Biz {

    private static final int DEFAULT_PRIORITY = 1000;

    private String           bizName;

    private String           mainClass;

    private URL[]            urls;

    private ClassLoader      classLoader;

    private int              priority;

    private Set<String> denyImportPackages;

    private Set<String> denyImportClasses;

    private Set<String> denyImportResources;

    public BizModel setBizName(String bizName) {
        this.bizName = bizName;
        return this;
    }

    public BizModel setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public BizModel setClassPath(URL[] urls) {
        this.urls = urls;
        return this;
    }

    public BizModel setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public BizModel setPriority(String priority) {
        this.priority = (priority == null ? DEFAULT_PRIORITY : Integer.valueOf(priority));
        return this;
    }

    public BizModel setDenyImportPackages(String denyImportPackages) {
        this.denyImportPackages = StringUtils.strToSet(denyImportPackages, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public BizModel setDenyImportClasses(String denyImportClasses) {
        this.denyImportClasses = StringUtils.strToSet(denyImportClasses, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public BizModel setDenyImportResources(String denyImportResources) {
        this.denyImportResources = StringUtils.strToSet(denyImportResources, Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    @Override
    public String getBizName() {
        return bizName;
    }

    @Override
    public String getMainClass() {
        return mainClass;
    }

    @Override
    public URL[] getClassPath() {
        return urls;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public ClassLoader getBizClassLoader() {
        return classLoader;
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return denyImportPackages;
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return denyImportClasses;
    }

    @Override
    public Set<String> getDenyImportResources() {
        return denyImportResources;
    }

    @Override
    public void start(String[] args) throws ArkException {
        if (mainClass == null) {
            throw new ArkException(String.format("biz: %s has no main method", getBizName()));
        }

        ClassLoader oldClassloader = ClassloaderUtil.pushContextClassloader(this.classLoader);
        try {
            MainMethodRunner mainMethodRunner = new MainMethodRunner(mainClass, args);
            mainMethodRunner.run();
        } catch (Throwable e) {
            throw new ArkException(e.getMessage(), e);
        } finally {
            ClassloaderUtil.popContextClassloader(oldClassloader);
        }
    }

    @Override
    public void stop() throws ArkException {

    }
}