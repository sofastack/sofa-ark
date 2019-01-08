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
package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class BizIdentityUtils {
    public static String generateBizIdentity(Biz biz) {
        return generateBizIdentity(biz.getBizName(), biz.getBizVersion());
    }

    public static String generateBizIdentity(String bizName, String bizVersion) {
        return bizName + Constants.STRING_COLON + bizVersion;
    }

    public static boolean isValid(String bizIdentity) {
        if (StringUtils.isEmpty(bizIdentity)) {
            return false;
        }
        String[] str = bizIdentity.split(Constants.STRING_COLON);
        if (str.length != 2) {
            return false;
        }
        return !StringUtils.isEmpty(str[0]) && !StringUtils.isEmpty(str[1]);
    }
}