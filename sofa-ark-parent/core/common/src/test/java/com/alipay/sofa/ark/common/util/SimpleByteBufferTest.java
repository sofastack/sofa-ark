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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimpleByteBufferTest {

    private SimpleByteBuffer simpleByteBuffer = new SimpleByteBuffer();

    @Test
    public void testSimpleByteBuffer() {

        simpleByteBuffer.backSpace();
        simpleByteBuffer.delete();
        assertEquals(-1, simpleByteBuffer.goRight());
        assertEquals(false, simpleByteBuffer.goLeft());

        for (int i = 0; i <= 20; i++) {
            simpleByteBuffer.add((byte) i);
        }

        assertEquals(0, simpleByteBuffer.goRight());
        assertEquals(1, simpleByteBuffer.goRight());
        assertEquals(true, simpleByteBuffer.goLeft());

        for (int i = 0; i <= 40; i++) {
            simpleByteBuffer.insert((byte) i);
        }

        assertEquals(1, simpleByteBuffer.goRight());
        assertEquals(2, simpleByteBuffer.goRight());
        assertEquals(true, simpleByteBuffer.goLeft());

        simpleByteBuffer.backSpace();
        assertEquals(2, simpleByteBuffer.goRight());

        simpleByteBuffer.delete();
        assertEquals(4, simpleByteBuffer.goRight());

        assertEquals(60, simpleByteBuffer.getBuffer().length);
        assertEquals(60, simpleByteBuffer.getSize());
        assertEquals(44, simpleByteBuffer.getPos());
        assertEquals(16, simpleByteBuffer.getGap());

        assertEquals(60, simpleByteBuffer.getAndClearBuffer().length);
        assertEquals(0, simpleByteBuffer.getSize());
        assertEquals(0, simpleByteBuffer.getPos());
    }
}
