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
package com.alipay.sofa.ark.spi.service.biz;

import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizConfig;
import com.alipay.sofa.ark.spi.model.BizOperation;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Create Biz according to {@link File} and {@link BizArchive}
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface BizFactoryService {

    /**
     * @param bizArchive
     * @return
     * @throws IOException
     */
    Biz createBiz(BizArchive bizArchive) throws IOException;

    /**
     * Create Biz Model according to {@link BizArchive}
     *
     * @param bizArchive the {@link BizArchive} model
     * @return Biz
     * @throws IOException throw io exception when {@link BizArchive} is invalid.
     */
    Biz createBiz(BizArchive bizArchive, URL[] extensionUrls) throws IOException;

    /**
     * Create Biz Model according to {@link File}
     *
     * @param file the ark biz file
     * @return Biz
     * @throws IOException throw io exception when {@link File} is invalid.
     */
    Biz createBiz(File file) throws IOException;

    /**
     * @param file
     * @param extensionUrls
     * @return
     * @throws IOException
     */
    Biz createBiz(File file, URL[] extensionUrls) throws IOException;

    /**
     * @param bizOperation
     * @param file
     * @return
     */
    Biz createBiz(BizOperation bizOperation, File file) throws IOException;

    /**
     * @param file
     * @param bizConfig
     * @return
     * @throws IOException
     */
    Biz createBiz(File file, BizConfig bizConfig) throws IOException;

    /**
     * @param bizArchive
     * @param bizConfig
     * @return
     * @throws IOException
     */
    Biz createBiz(BizArchive bizArchive, BizConfig bizConfig) throws IOException;

    /**
     * Create Biz Model according to master biz
     * @return
     */
    Biz createEmbedMasterBiz(ClassLoader masterClassLoader);
}