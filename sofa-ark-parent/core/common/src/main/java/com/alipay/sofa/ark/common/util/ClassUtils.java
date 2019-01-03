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

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ClassUtils {

    /**
     * Check whether a package is adapted to a pattern. e.g. the package {@literal com.alipay.sofa}
     * would be adapted to the pattern {@literal com.*} or the pattern {@literal com.alipay}, but not
     * the pattern {@literal com}.
     *
     * @param pkg
     * @param pkgPattern
     * @return
     */
    public static boolean isAdaptedToPackagePattern(String pkg, String pkgPattern) {
        if (pkgPattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
            return pkg.startsWith(getPackageName(pkgPattern));
        } else {
            return pkg.equals(pkgPattern);
        }
    }

    /**
     * Get package name from a specified class name.
     *
     * @param className
     * @return
     */
    public static String getPackageName(String className) {
        AssertUtils.isFalse(StringUtils.isEmpty(className), "ClassName should not be empty!");
        int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(0, index);
        }
        return Constants.DEFAULT_PACKAGE;
    }

}