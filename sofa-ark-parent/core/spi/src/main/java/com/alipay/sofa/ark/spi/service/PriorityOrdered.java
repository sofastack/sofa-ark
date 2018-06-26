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
package com.alipay.sofa.ark.spi.service;

/**
 * {@code PriorityOrdered} is an interface that can be implemented by objects that
 * should be ordered.
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public interface PriorityOrdered {

    /**
     * Useful constant for the highest precedence value.
     * @see java.lang.Integer#MIN_VALUE
     */
    int HIGHEST_PRECEDENCE = Integer.MIN_VALUE;

    /**
     * Useful constant for the lowest precedence value.
     * @see java.lang.Integer#MAX_VALUE
     */
    int LOWEST_PRECEDENCE  = Integer.MAX_VALUE;

    /**
     * Default priority
     */
    int DEFAULT_PRECEDENCE = 100;

    /**
     * Get the order value of this object. Higher values are interpreted as lower
     * priority. As a consequence, the object with the lowest value has the highest
     * priority.
     * @return
     */
    int getPriority();
}