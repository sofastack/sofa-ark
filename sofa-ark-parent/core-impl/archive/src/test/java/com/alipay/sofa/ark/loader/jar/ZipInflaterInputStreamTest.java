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
package com.alipay.sofa.ark.loader.jar;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.zip.InflaterInputStream;

import static org.junit.Assert.assertEquals;

public class ZipInflaterInputStreamTest {

    @Test
    public void testFill() throws Exception {

        ZipInflaterInputStream zipInflaterInputStream = new ZipInflaterInputStream(
            new ByteArrayInputStream(new byte[0]), 0);
        assertEquals(0, zipInflaterInputStream.available());
        zipInflaterInputStream.fill();

        Field field = ZipInflaterInputStream.class.getDeclaredField("extraBytesWritten");
        field.setAccessible(true);
        assertEquals(true, field.get(zipInflaterInputStream));
    }
}
