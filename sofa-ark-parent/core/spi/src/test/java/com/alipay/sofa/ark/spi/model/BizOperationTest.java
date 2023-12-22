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
package com.alipay.sofa.ark.spi.model;

import com.alipay.sofa.ark.spi.event.AbstractArkEvent;
import org.junit.Test;

import static com.alipay.sofa.ark.spi.model.BizOperation.createBizOperation;
import static com.alipay.sofa.ark.spi.model.BizState.*;
import static org.junit.Assert.*;
import static org.springframework.beans.BeanUtils.copyProperties;

public class BizOperationTest {

    @Test
    public void testOperationEqual() {

        BizOperation op1 = createBizOperation();
        op1.setBizName("biz A").setBizVersion("1.0.0")
            .setOperationType(BizOperation.OperationType.INSTALL);
        BizOperation op2 = createBizOperation();
        op2.setBizName("biz A").setBizVersion("1.0.0")
            .setOperationType(BizOperation.OperationType.INSTALL);

        assertEquals(op1, op1);
        assertEquals(op1, op2);
        op2.setOperationType(BizOperation.OperationType.UNINSTALL);
        assertNotEquals(op1, op2);
        op2.setBizVersion("2.0.0");
        assertNotEquals(op1, op2);
        op2.setBizName("biz B");
        assertNotEquals(op1, op2);

        assertFalse(op1.equals(""));
        assertFalse(op1.equals(null));
    }

    @Test
    public void testAbstractArkEvent() {
        AbstractArkEvent abstractArkEvent = new AbstractArkEvent("") {
        };
        copyProperties(abstractArkEvent, abstractArkEvent);
    }

    @Test
    public void testBizState() {
        assertEquals(UNRESOLVED, BizState.of("UNRESOLVED"));
        assertEquals(RESOLVED, BizState.of("RESOLVED"));
        assertEquals(ACTIVATED, BizState.of("ACTIVATED"));
        assertEquals(DEACTIVATED, BizState.of("DEACTIVATED"));
        assertEquals(BROKEN, BizState.of("aaa"));
    }
}
