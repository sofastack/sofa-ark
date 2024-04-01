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
package com.alipay.sofa.ark.spi.model;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public interface BizInfo {
    /**
     * get Biz Name
     * @return biz name
     */
    String getBizName();

    /**
     * get Biz Version
     */
    String getBizVersion();

    /**
     * get identity id in runtime, an unique-id of ark biz
     * @return
     */
    String getIdentity();

    /**
     * get Biz Main Entry Class Name
     * @return main class name
     */
    String getMainClass();

    /**
     * get Biz Class Path
     * @return biz classpath
     */
    URL[] getClassPath();

    /**
     * get denied imported packages config
     * @return
     */
    Set<String> getDenyImportPackages();

    /**
     * get biz deny import package which is exactly matched
     * @return
     */
    Set<String> getDenyImportPackageNodes();

    /**
     * get biz deny import package which is matched by prefix
     * @return
     */
    Set<String> getDenyImportPackageStems();

    /**
     * get denied imported classes
     * @return
     */
    Set<String> getDenyImportClasses();

    /**
     * get denied imported resources
     * @return
     */
    Set<String> getDenyImportResources();

    /**
     * get denied imported resource stems by prefix
     * @return denied imported resource stems
     */
    Set<String> getDenyPrefixImportResourceStems();

    /**
     * get denied imported resource stems by suffix
     * @return denied imported resource stems
     */
    Set<String> getDenySuffixImportResourceStems();

    /**
     * get Biz Classloader
     * @return biz classloader
     */
    ClassLoader getBizClassLoader();

    /**
     * get Biz State
     */
    BizState getBizState();

    /**
     * get web context path
     */
    String getWebContextPath();

    /**
     * get Biz attributes
     * @return
     */
    Map<String, String> getAttributes();

    /**
     * get getBizStateChangeLog
     * @since 2.2.9
     * @return java.util.concurrent.CopyOnWriteArrayList<com.alipay.sofa.ark.spi.model.BizInfo.BizStateChangeInfo>
     */
    List<BizStateRecord> getBizStateRecords();

    class BizStateRecord {
        private final Date                    changeTime;
        private final BizState                state;

        private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        static {
            sdf.setTimeZone(TimeZone.getDefault());
        }

        public BizStateRecord(Date changeTime, BizState state) {
            this.changeTime = changeTime;
            this.state = state;
        }

        @Override
        public String toString() {
            String date = sdf.format(changeTime);
            return String.format("%s -> %s", date, state);
        }
    }
}
