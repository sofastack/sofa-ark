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
package com.alipay.sofa.ark.config.zk;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.ark.config.ConfigUtils;
import com.alipay.sofa.ark.spi.service.biz.BizFileGenerator;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ZookeeperConfigListener {

    //    public static class IpConfigListener implements ConfigListener {
    //        private final static ArkLogger LOGGER = ArkLoggerFactory
    //                                                  .getLogger("com.alipay.sofa.ark.config");
    //
    //        @Override
    //        public List<BizOperation> configUpdated(String newValue) {
    //            BizOperation bizOperation = ConfigParser.parseConfig(newValue);
    //            if (!bizOperation.isValid() && LOGGER.isWarnEnabled()) {
    //                LOGGER.warn("Receive invalid config {}", bizOperation);
    //            } else if (bizOperation.isValid()) {
    //                switch (bizOperation.getCommand()) {
    //                    case "install":
    //                        try {
    //                            File bizFile = fetchFile(bizOperation.getBizName(),
    //                                bizOperation.getBizVersion(), bizOperation.getBizUrl());
    //                            ArkClient.installBiz(bizFile);
    //                        } catch (Throwable throwable) {
    //                            LOGGER.error(
    //                                String.format("Failed to uninstall biz: %s:%s",
    //                                    bizOperation.getBizName(), bizOperation.getBizVersion()),
    //                                throwable);
    //                        }
    //                        break;
    //                    case "uninstall":
    //                        try {
    //                            ArkClient.uninstallBiz(bizOperation.getBizName(),
    //                                bizOperation.getBizVersion());
    //                        } catch (Throwable throwable) {
    //                            LOGGER.error(
    //                                String.format("Failed to uninstall biz: %s:%s",
    //                                    bizOperation.getBizName(), bizOperation.getBizVersion()),
    //                                throwable);
    //                        }
    //                        break;
    //                    case "switch":
    //                        try {
    //                            ArkClient.switchBiz(bizOperation.getBizName(),
    //                                bizOperation.getBizVersion());
    //                        } catch (Throwable throwable) {
    //                            LOGGER.error(
    //                                String.format("Failed to switch biz: %s:%s",
    //                                    bizOperation.getBizName(), bizOperation.getBizVersion()),
    //                                throwable);
    //                        }
    //                        break;
    //                    default:
    //                        LOGGER.error("invalid config: {}.", newValue);
    //                }
    //            }
    //            return Collections.singletonList(bizOperation);
    //        }
    //
    //        protected File fetchFile(String bizName, String bizVersion, String url) throws Throwable {
    //            if (StringUtils.isEmpty(url)) {
    //                List<BizFileGenerator> bizFileGenerator = ArkServiceLoader
    //                    .loadExtension(BizFileGenerator.class);
    //                File file = null;
    //                for (int i = 0; i < bizFileGenerator.size() && file == null; ++i) {
    //                    file = bizFileGenerator.get(i).createBizFile(bizName, bizVersion);
    //                }
    //                return file;
    //            } else {
    //                URL urlLocation = new URL(url);
    //                InputStream inputStream = urlLocation.openStream();
    //                File installBizFile = ConfigUtils.createBizSaveFile(bizName, bizVersion);
    //                FileUtils.copyInputStreamToFile(inputStream, installBizFile);
    //                return installBizFile;
    //            }
    //        }
    //    }
    //
    //    public static class BizConfigListener implements ConfigListener {
    //        @Override
    //        public List<BizOperation> configUpdated(String newValue) {
    //            return null;
    //        }
    //    }

}