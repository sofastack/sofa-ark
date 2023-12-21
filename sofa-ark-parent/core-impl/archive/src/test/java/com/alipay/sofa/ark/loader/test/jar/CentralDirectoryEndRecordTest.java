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
import com.alipay.sofa.ark.loader.jar.CentralDirectoryEndRecord;
import com.alipay.sofa.ark.loader.test.base.BaseTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class CentralDirectoryEndRecordTest extends BaseTest {

    @Test
    public void testEOCD() throws IOException {

        RandomAccessDataFile dataFile = new RandomAccessDataFile(getTempDemoZip());
        CentralDirectoryEndRecord eocd = new CentralDirectoryEndRecord(dataFile);

        assertTrue(eocd.isValid());
        assertTrue(eocd.getStartOfArchive(dataFile) == 0);
        assertTrue(eocd.getNumberOfRecords() == 5);
    }

    @Test
    public void testWithInvalidFile() throws Exception {

        RandomAccessData randomAccessData = mock(RandomAccessData.class);
        when(randomAccessData.getInputStream(any())).thenReturn(mock(InputStream.class));

        URL url = this.getClass().getClassLoader().getResource("example-jarinjarinjar.jar");
        new CentralDirectoryEndRecord(new RandomAccessDataFile(new File(url.getPath())));
    }
}
