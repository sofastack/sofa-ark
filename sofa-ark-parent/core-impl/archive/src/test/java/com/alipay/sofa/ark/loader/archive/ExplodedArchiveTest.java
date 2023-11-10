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
package com.alipay.sofa.ark.loader.archive;

import com.alipay.sofa.ark.spi.archive.Archive;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExplodedArchiveTest {

    private ExplodedArchive explodedArchive;

    private String          archiveTestDirPath = this.getClass().getClassLoader()
                                                   .getResource("./exploded-archive-test/")
                                                   .getFile();

    @Before
    public void setUp() {
        explodedArchive = new ExplodedArchive(new File(archiveTestDirPath));
    }

    @Test
    public void testGetMethods() throws Exception {

        assertTrue(explodedArchive.getUrl().getFile().endsWith("test-classes/exploded-archive-test/"));
        assertTrue(explodedArchive.getManifest() != null);

        List<Archive> nestedArchives = explodedArchive.getNestedArchives(entry -> !entry.getName().contains("META-INF"));
        assertEquals(2, nestedArchives.size());
        String nestedArchivesStr = nestedArchives.toString();
        assertTrue(nestedArchivesStr.contains("/example-jarinjarinjar.jar!/"));
        assertTrue(nestedArchivesStr.contains("/sample-biz.jar!/"));
    }
}
