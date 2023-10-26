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

import java.lang.reflect.Modifier;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Handle class information display
 *
 * @author yanzhu
 * @since 2.2.4
 */
public class ClassInfoMethod {

    /**
     * empty string
     */
    private static final String EMPTY_STRING = "";

    /**
     * get code source of class
     */
    private static String getCodeSource(final CodeSource cs) {
        if (null == cs || null == cs.getLocation() || null == cs.getLocation().getFile()) {
            return EMPTY_STRING;
        }

        return cs.getLocation().getFile();
    }

    /**
     * get the complete class name
     *
     * @param clazz
     * @return
     */
    private static String getClassName(Class<?> clazz) {
        if (clazz.isArray()) {
            StringBuilder sb = new StringBuilder(clazz.getName());
            sb.delete(0, 2);
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ';') {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("[]");
            return sb.toString();
        } else {
            return clazz.getName();
        }
    }

    /**
     * get modifier of class
     *
     * @param mod modifier
     * @return 翻译值
     */
    private static String getModifier(int mod, char splitter) {
        StringBuilder sb = new StringBuilder();
        if (Modifier.isAbstract(mod)) {
            sb.append("abstract").append(splitter);
        }
        if (Modifier.isFinal(mod)) {
            sb.append("final").append(splitter);
        }
        if (Modifier.isInterface(mod)) {
            sb.append("interface").append(splitter);
        }
        if (Modifier.isNative(mod)) {
            sb.append("native").append(splitter);
        }
        if (Modifier.isPrivate(mod)) {
            sb.append("private").append(splitter);
        }
        if (Modifier.isProtected(mod)) {
            sb.append("protected").append(splitter);
        }
        if (Modifier.isPublic(mod)) {
            sb.append("public").append(splitter);
        }
        if (Modifier.isStatic(mod)) {
            sb.append("static").append(splitter);
        }
        if (Modifier.isStrict(mod)) {
            sb.append("strict").append(splitter);
        }
        if (Modifier.isSynchronized(mod)) {
            sb.append("synchronized").append(splitter);
        }
        if (Modifier.isTransient(mod)) {
            sb.append("transient").append(splitter);
        }
        if (Modifier.isVolatile(mod)) {
            sb.append("volatile").append(splitter);
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * get all super classes of current class
     *
     * @param clazz
     * @return
     */
    private static String[] getSuperClass(Class clazz) {
        List<String> list = new ArrayList<String>();
        Class<?> superClass = clazz.getSuperclass();
        if (null != superClass) {
            list.add(ClassInfoMethod.getClassName(superClass));
            while (true) {
                superClass = superClass.getSuperclass();
                if (null == superClass) {
                    break;
                }
                list.add(ClassInfoMethod.getClassName(superClass));
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * get all classloaders of class
     *
     * @param clazz
     * @return
     */
    private static String[] getClassloader(Class clazz) {
        List<String> list = new ArrayList<String>();
        ClassLoader loader = clazz.getClassLoader();
        if (null != loader) {
            list.add(loader.toString());
            while (true) {
                loader = loader.getParent();
                if (null == loader) {
                    break;
                }
                list.add(loader.toString());
            }
        }
        return list.toArray(new String[0]);
    }

    /**
     * create complete class information
     *
     * @param clazz
     * @param bizName
     * @return
     */
    public static String createClassInfo(Class<?> clazz, String bizName) {
        ClassInfoVO classInfo = new ClassInfoVO();
        classInfo.setClassInfo(ClassInfoMethod.getClassName(clazz));
        classInfo.setCodeSource(ClassInfoMethod.getCodeSource(clazz.getProtectionDomain()
            .getCodeSource()));
        classInfo.setInterface(clazz.isInterface());
        classInfo.setAnnotation(clazz.isAnnotation());
        classInfo.setEnum(clazz.isEnum());
        classInfo.setContainerName(bizName);
        classInfo.setSimpleName(clazz.getSimpleName());
        classInfo.setModifier(ClassInfoMethod.getModifier(clazz.getModifiers(), ','));
        classInfo.setSuperClass(ClassInfoMethod.getSuperClass(clazz));
        classInfo.setClassloader(ClassInfoMethod.getClassloader(clazz));
        return ViewRender.renderClassInfo(classInfo);
    }
}
