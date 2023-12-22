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

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class JarEntryTest {

    private URL      url;

    private JarEntry jarEntry;

    @Before
    public void setUp() throws Exception {
        url = this.getClass().getClassLoader().getResource("sample-biz-withjar.jar");
        jarEntry = new JarEntry(new JarFile(new File(url.getPath())),
            new CentralDirectoryFileHeader(new byte[64], 0, new AsciiBytes("lib"), null,
                new AsciiBytes("mycomment"), 0));
    }

    @Test
    public void testGetters() throws Exception {
        assertEquals("jar:" + url + "!/lib", jarEntry.getUrl().toString());
        assertNull(jarEntry.getAttributes());
        assertNull(jarEntry.getCertificates());
        assertNull(jarEntry.getCodeSigners());
        jarEntry.setCertificates(new java.util.jar.JarEntry("a"));
    }
}
