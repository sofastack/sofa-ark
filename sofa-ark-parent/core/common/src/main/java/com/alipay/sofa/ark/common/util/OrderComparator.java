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

import com.alipay.sofa.ark.spi.service.Ordered;

import java.util.Comparator;

/**
 * {@link Comparator} implementation for {@link Ordered} objects, sorting
 * by order value ascending.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class OrderComparator implements Comparator<Object> {
    @Override
    public int compare(Object o1, Object o2) {
        int order1 = ((Ordered) o1).getOrder();
        int order2 = ((Ordered) o2).getOrder();
        return (order1 < order2) ? -1 : (order1 > order2) ? 1 : 0;
    }
}