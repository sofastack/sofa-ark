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
package com.alipay.sofa.ark.loader.test.data;

import com.alipay.sofa.ark.loader.data.RandomAccessData;
import com.alipay.sofa.ark.loader.data.RandomAccessDataFile;
import com.alipay.sofa.ark.loader.test.base.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class RandomAccessDataFileTest extends BaseTest {

    @Test
    public void testInputStream() throws IOException {
        RandomAccessDataFile testFile = new RandomAccessDataFile(getTempDemoFile());
        InputStream is = null;

        try {
            is = testFile.getInputStream(RandomAccessData.ResourceAccess.PER_READ);
            byte[] bytes = new byte[20];
            Assert.assertTrue(is.read(bytes) == 16);
            for (int i = 0; i < 16; ++i) {
                Assert.assertTrue((bytes[i] & 0xFF) == '1' + i / 2);
            }
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    @Test
    public void testSize() {
        RandomAccessDataFile testFile = new RandomAccessDataFile(getTempDemoFile());
        Assert.assertTrue(testFile.getSize() == 16);
    }

    @Test
    public void testSubsection() throws IOException {
        RandomAccessDataFile testFile = new RandomAccessDataFile(getTempDemoFile());
        try {
            testFile.getSubsection(0, 17);
            Assert.fail("Should throws IndexOutOfBoundsException");
        } catch (Exception ex) {
            Assert.assertTrue(ex instanceof IndexOutOfBoundsException);
        }

        RandomAccessData subData = testFile.getSubsection(2, 4);
        InputStream is = null;

        try {
            is = subData.getInputStream(RandomAccessData.ResourceAccess.ONCE);
            byte[] bytes = new byte[10];
            Assert.assertTrue(is.read(bytes) == 4);
            for (int i = 0; i < 4; ++i) {
                Assert.assertTrue((bytes[i] & 0xFF) == '2' + i / 2);
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}