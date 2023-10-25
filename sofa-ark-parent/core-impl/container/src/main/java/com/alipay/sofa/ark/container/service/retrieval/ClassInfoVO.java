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
package com.alipay.sofa.ark.container.service.retrieval;

import java.util.Arrays;

/**
 * Class Detail Info VO Of Telnet 'ck' Command
 *
 * @author yanzhu
 * @since 2.2.4
 */

public class ClassInfoVO {

    private String   classInfo;
    private String   codeSource;
    private Boolean  isInterface;
    private Boolean  isAnnotation;
    private Boolean  isEnum;
    private String   containerName;
    private String   simpleName;
    private String   modifier;
    private String[] superClass;
    private String[] classloader;

    public ClassInfoVO() {
    }

    public String getClassInfo() {
        return classInfo;
    }

    public void setClassInfo(String classInfo) {
        this.classInfo = classInfo;
    }

    public String getCodeSource() {
        return codeSource;
    }

    public void setCodeSource(String codeSource) {
        this.codeSource = codeSource;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public boolean isAnnotation() {
        return isAnnotation;
    }

    public void setAnnotation(boolean annotation) {
        isAnnotation = annotation;
    }

    public boolean isEnum() {
        return isEnum;
    }

    public void setEnum(boolean anEnum) {
        isEnum = anEnum;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public String[] getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String[] superClass) {
        this.superClass = superClass;
    }

    public String[] getClassloader() {
        return classloader;
    }

    public void setClassloader(String[] classloader) {
        this.classloader = classloader;
    }

    @Override
    public String toString() {
        return "ClassDetailVO{" + "classInfo='" + classInfo + '\'' + ", codeSource='" + codeSource
               + '\'' + ", isInterface=" + isInterface + ", isAnnotation=" + isAnnotation
               + ", isEnum=" + isEnum + ", containerName='" + containerName + '\''
               + ", simpleName='" + simpleName + '\'' + ", modifier='" + modifier + '\''
               + ", superClass=" + Arrays.toString(superClass) + ", classloader="
               + Arrays.toString(classloader) + '}';
    }
}
