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
package com.alipay.sofa.ark.spi.event.biz;

import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import com.alipay.sofa.ark.spi.model.Biz;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @since 2.2.9
 */
public class AfterBizStartupFailedEvent extends AbstractArkEvent<Biz> {

    private Throwable throwable;

    public AfterBizStartupFailedEvent(Biz source, Throwable e) {
        super(source);
        this.throwable = e;
        this.topic = Constants.BIZ_EVENT_TOPIC_AFTER_BIZ_FAILED;
    }

    public Throwable getThrowable() {
        return throwable;
    }

}
