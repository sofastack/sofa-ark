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
package com.alipay.sofa.ark.clean;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.event.EventHandler;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author firedemo1
 */
public class HisBizStaticFieldCleaner implements EventHandler<BeforeBizRecycleEvent> {
    private final static ArkLogger LOGGER = ArkLoggerFactory.getLogger(HisBizStaticFieldCleaner.class);

    @Override
    public void handleEvent(BeforeBizRecycleEvent beforeBizRecycleEvent) {
        Biz biz = beforeBizRecycleEvent.getSource();

        try {
            if (biz != null && biz != ArkClient.getMasterBiz()) {
                List<Class<?>> list = findClassesWithStaticFields(biz.getBizClassLoader(), biz.getClassPath()[0].getPath());

                cleanFieldsAndClazz(list);
            }
        } catch (Exception e) {
            LOGGER.error("Clean biz static fields meet exception.errMsg = {}" + e.getMessage());
        }
    }

    private void cleanFieldsAndClazz(List<Class<?>> list) {
        for (Class<?> clazz : list) {
            try {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    try {
                        if (!Modifier.isFinal(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
                            field.setAccessible(true);
                            field.set(null, null);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Clean static fields meet exception.errMsg = {}" + e.getMessage());
                    }
                }
                clazz = null;
            } catch (Exception e) {
                LOGGER.error("Clean static fields meet exception.errMsg = {}" + e.getMessage());
            }
        }
    }

    public List<Class<?>> findClassesWithStaticFields(ClassLoader bizClassLoader, String classPath) {
        List<Class<?>> classes = new ArrayList<>();
        File classDir = new File(classPath);
        File[] classFiles = collectClassFiles(classDir);

        for (File file : classFiles) {
            String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
            try {
                Class<?> cls = bizClassLoader.loadClass(className);
                if (hasStaticFields(cls)) {
                    classes.add(cls);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.error("Find classes with static fields meet exception.errMsg = {}" + e.getMessage());
            }
        }
        return classes;
    }
    private File[] collectClassFiles(File classDir) {
        List<File> fileList = new ArrayList<>();
        collectClassFiles(classDir, fileList);
        return fileList.toArray(new File[0]);
    }

    private void collectClassFiles(File classDir, List<File> fileList) {
        File[] files = classDir.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                // 处理以".class"结尾的文件
                fileList.add(file);
            } else if (file.isDirectory()) {
                // 递归遍历子目录
                collectClassFiles(file, fileList);
            }
        }
    }

    private boolean hasStaticFields(Class<?> cls) {
        for (java.lang.reflect.Field field : cls.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getPriority() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }
}
