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
package com.alipay.sofa.ark.spi.service.extension;

/**
 * Annotation required on the implementation of extensible interface.
 *
 * @author qilong.zql
 * @since 0.6.0
 */
public @interface Extension {
    /**
     * extension name
     */
    String value();

    /**
     * extension order, Higher values are interpreted as lower priority.
     * As a consequence, the object with the lowest value has the highest
     * priority.
     */
    int order() default 100;
}