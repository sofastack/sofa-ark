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
import com.alipay.sofa.ark.loader.jar.*;
import com.alipay.sofa.ark.loader.test.base.BaseTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class CentralDirectoryParserTest extends BaseTest {

    @Test
    public void testParser() throws IOException {
        CentralDirectoryParser cdParser = new CentralDirectoryParser();
        TestVisitor testVisitor = cdParser.addVisitor(new TestVisitor());
        RandomAccessDataFile dataFile = new RandomAccessDataFile(getTempDemoZip());
        cdParser.parse(dataFile, false);
        List<CentralDirectoryFileHeader> cdfhList = testVisitor.getCdfhList();

        Assert.assertTrue(testVisitor.getEntryNum() == 5);
        Assert.assertTrue(cdfhList.size() == 5);
        Assert.assertTrue(cdfhList.get(4).getName().toString().equals(TEST_ENTRY));
        Assert.assertTrue(cdfhList.get(4).getComment().toString().equals(TEST_ENTRY_COMMENT));
        Assert
            .assertTrue(compareByteArray(cdfhList.get(4).getExtra(), TEST_ENTRY_EXTRA.getBytes()));

    }

    public static class TestVisitor implements CentralDirectoryVisitor {

        public int                              entryNum;
        public List<CentralDirectoryFileHeader> cdfhList = new ArrayList<>();

        @Override
        public void visitStart(CentralDirectoryEndRecord endRecord,
                               RandomAccessData centralDirectoryData) {
            entryNum = endRecord.getNumberOfRecords();
        }

        @Override
        public void visitFileHeader(CentralDirectoryFileHeader fileHeader, int dataOffset) {
            cdfhList.add(fileHeader);
        }

        @Override
        public void visitEnd() {

        }

        public int getEntryNum() {
            return entryNum;
        }

        public List<CentralDirectoryFileHeader> getCdfhList() {
            return cdfhList;
        }
    }
}