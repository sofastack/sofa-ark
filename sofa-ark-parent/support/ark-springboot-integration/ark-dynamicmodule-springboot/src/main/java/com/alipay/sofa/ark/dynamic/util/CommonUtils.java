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
package com.alipay.sofa.ark.dynamic.util;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants;
import org.testng.util.Strings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static com.alipay.sofa.ark.dynamic.common.SofaArkTestConstants.WORKSPACE;

/**
 * The type Common utils.
 *
 * @author hanyue
 * @version : CommonUtils.java, v 0.1 2022年05月30日 1:08 PM hanyue Exp $
 */
public class CommonUtils {
    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    /**
     * Gets project base dir.
     *
     * @return the project base dir
     */
    public static String getProjectBaseDir() {
        String projectBaseDir = PropertiesUtils.getProperty(SofaArkTestConstants.PROJECT_BASE_DIR);

        if (Strings.isNullOrEmpty(projectBaseDir)) {
            projectBaseDir = WORKSPACE;
        } else {
            projectBaseDir = FileUtil.getPathBasedOn(WORKSPACE, projectBaseDir);
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("ProjectBaseDir is {}", projectBaseDir);
        }
        return projectBaseDir;
    }

    /**
     * New array list array list.
     *
     * @param <E>      the type parameter
     * @param elements the elements
     * @return the array list
     */
    public static <E> ArrayList<E> newArrayList(Iterable<? extends E> elements) {
        if (elements == null) {
            throw new NullPointerException();
        }
        if (elements instanceof Collection) {
            return new ArrayList<E>((Collection) elements);
        } else {
            ArrayList<E> list = new ArrayList();
            Iterator<? extends E> iterator = elements.iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
            return list;
        }
    }
}