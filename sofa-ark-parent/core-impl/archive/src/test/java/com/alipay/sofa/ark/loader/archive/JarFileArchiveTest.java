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

import com.alipay.sofa.ark.loader.archive.JarFileArchive.JarFileEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JarFileArchiveTest {

    private JarFileArchive jarFileArchive;

    private String         jarFilePath = this.getClass().getClassLoader()
                                           .getResource("./sample-springboot-fat-biz.jar")
                                           .getFile();

    @Before
    public void setUp() throws IOException {
        jarFileArchive = new JarFileArchive(new File(jarFilePath));
    }

    @Test
    public void testGetMethods() throws Exception {

        assertTrue(jarFileArchive.getManifest() != null);
        assertEquals(50, jarFileArchive.getNestedArchives(entry -> entry.getName().contains(".jar")).size());

        JarEntry jarEntry = new JarEntry("BOOT-INF/lib/slf4j-api-1.7.21.jar");
        jarEntry.setComment("UNPACK:xxx");
        assertTrue(jarFileArchive.getNestedArchive(new JarFileEntry(jarEntry)).getUrl().getFile().endsWith("slf4j-api-1.7.21.jar"));
    }
}
