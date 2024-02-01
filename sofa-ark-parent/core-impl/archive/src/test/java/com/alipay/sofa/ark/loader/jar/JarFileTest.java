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

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static com.alipay.sofa.ark.loader.jar.JarFile.JarFileType.DIRECT;
import static org.junit.Assert.assertEquals;

public class JarFileTest {

    @Test
    public void testSetupEntryCertificates() throws IOException {

        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        JarFile jarFile = new JarFile(new File(url.getPath()));
        assertEquals(7485, jarFile.size());
        assertEquals("jar:" + url.toString() + "!/", jarFile.getUrl().toString());

        jarFile.setupEntryCertificates(new JarEntry(jarFile, new CentralDirectoryFileHeader(
            new byte[64], 0, new AsciiBytes("lib"), null, new AsciiBytes("mycomment"), 0)));

        jarFile.clearCache();
        assertEquals(DIRECT, jarFile.getType());
    }
}
