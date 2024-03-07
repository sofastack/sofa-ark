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
package com.alipay.sofa.ark.loader.test.jar;

import com.alipay.sofa.ark.loader.jar.AsciiBytes;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class AsciiBytesTest {

    public final String content    = "SofaArk is a class-isolated container";

    private AsciiBytes  asciiBytes = new AsciiBytes(content);

    @Test
    public void testAsciiBytes() {

        assertTrue(asciiBytes.length() == content.length());
        assertTrue(asciiBytes.startsWith(new AsciiBytes("SofaArk")));
        assertTrue(asciiBytes.endsWith(new AsciiBytes("container")));
        assertTrue(asciiBytes.toString().equals(content));
        assertTrue(asciiBytes.substring(8, 10).endsWith(new AsciiBytes("is")));

        String suffix = "suffix";
        AsciiBytes suffixAsciiBytes = new AsciiBytes(suffix);
        byte[] suffixBytes = suffix.getBytes();

        asciiBytes.append(suffix).equals(content + suffix);
        asciiBytes.append(suffixAsciiBytes).equals(content + suffix);
        asciiBytes.append(suffixBytes).equals(content + suffix);
    }

    @Test
    public void testHashCode() {
        AsciiBytes asciiBytes = new AsciiBytes("" + (char) 0xffff + (char) -99 + (char) -255
                                               + (char) -128 + (char) 127 + (char) 128 + (char) 255
                                               + (char) 256 + content);
        assertEquals(-243313336, asciiBytes.hashCode());
        assertNotEquals(new AsciiBytes(""), asciiBytes);
    }
}
