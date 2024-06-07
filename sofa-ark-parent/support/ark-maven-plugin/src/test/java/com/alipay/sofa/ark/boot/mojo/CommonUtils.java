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
package com.alipay.sofa.ark.boot.mojo;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CommonUtils.java, v 0.1 2024年06月07日 15:33 立蓬 Exp $
 */
public class CommonUtils {

    public static File getResourceFile(String resourcePath) throws URISyntaxException {
        URL url = CommonUtils.class.getClassLoader().getResource(resourcePath);
        return new File(url.toURI());
    }

    public static boolean resourceExists(String resourcePath) {
        return null != CommonUtils.class.getClassLoader().getResource(resourcePath);
    }
}