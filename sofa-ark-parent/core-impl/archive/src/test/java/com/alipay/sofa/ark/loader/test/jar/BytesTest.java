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

import com.alipay.sofa.ark.loader.data.RandomAccessData;
import com.alipay.sofa.ark.loader.data.RandomAccessDataFile;
import com.alipay.sofa.ark.loader.jar.Bytes;
import com.alipay.sofa.ark.loader.test.base.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class BytesTest extends BaseTest {

    @Test
    public void testBytes() throws IOException {
        RandomAccessDataFile tempFile = new RandomAccessDataFile(getTempDemoFile());
        byte[] content = Bytes.get(tempFile);
        Assert.assertTrue(content.length == CONSTANT_BYTE.length);
        for (int i = 0; i < CONSTANT_BYTE.length; ++i) {
            Assert.assertTrue(content[i] == CONSTANT_BYTE[i]);
        }

        byte[] bytes = new byte[3];
        Bytes.fill(tempFile.getInputStream(RandomAccessData.ResourceAccess.ONCE), bytes);
        String.valueOf(bytes).equals("112");

        long value = Bytes.littleEndianValue(CONSTANT_BYTE, 0, 4);
        Assert.assertTrue(value == ('2' << 24) + ('2' << 16) + ('1' << 8) + '1');
    }

}