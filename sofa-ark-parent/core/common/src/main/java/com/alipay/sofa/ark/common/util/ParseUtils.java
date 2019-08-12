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

import java.util.Set;

/**
 *
 * @author ruoshan
 * @since 1.0.0
 */
public class ParseUtils {

    /**
     * Parse package node(exactly match) and stem(with *)
     * @param candidates candidate packages
     * @param stems with * packages
     * @param nodes exactly match packages
     */
    public static void parsePackageNodeAndStem(Set<String> candidates, Set<String> stems,
                                               Set<String> nodes) {
        for (String pkgPattern : candidates) {
            if (pkgPattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                stems.add(ClassUtils.getPackageName(pkgPattern));
            } else {
                nodes.add(pkgPattern);
            }
        }
    }

    /**
     * Parse resource exactly match and stem(with *)
     * @param candidates candidate resources
     * @param stems with * resources
     * @param resources exactly match resources
     */
    public static void parseResourceAndStem(Set<String> candidates, Set<String> stems,
                                            Set<String> resources) {
        for (String candidate : candidates) {
            if (candidate.endsWith(Constants.RESOURCE_STEM_MARK)) {
                stems.add(candidate.substring(0,
                    candidate.length() - Constants.RESOURCE_STEM_MARK.length()));
            } else {
                resources.add(candidate);
            }
        }
    }
}