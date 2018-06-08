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
package com.alipay.sofa.ark.spi.service.event;

import com.alipay.sofa.ark.spi.event.ArkEvent;

/**
 * @author qilong.zql
 * @since 0.4.0
 */
public interface EventAdminService {

    /**
     * Initiate synchronous delivery of an event. This method does not return to
     * the caller until delivery of the event is completed.
     *
     * @param event The event to send to all listeners which subscribe to the
     *        topic of the event.
     */
    void sendEvent(ArkEvent event);

}