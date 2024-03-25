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

import com.alipay.sofa.ark.loader.jar.JarEntry;
import com.alipay.sofa.ark.loader.jar.JarFile;
import com.alipay.sofa.ark.loader.test.base.BaseTest;
import org.junit.Test;

import java.io.IOException;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.junit.Assert.assertTrue;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class JarFileTest extends BaseTest {

    @Test
    public void testJarFile() throws IOException {

        JarFile jarFile = new JarFile(getTempDemoZip());
        Manifest manifest = jarFile.getManifest();

        assertTrue(manifest.getMainAttributes().getValue("k1").equals("v1"));
        assertTrue(manifest.getMainAttributes().getValue("k2").equals("v2"));
        assertTrue(jarFile.containsEntry(TEST_ENTRY));

        ZipEntry zipEntry = jarFile.getEntry(TEST_ENTRY);
        assertTrue(zipEntry.getName().equals(TEST_ENTRY));
        assertTrue(zipEntry.getComment().equals(TEST_ENTRY_COMMENT));

        JarEntry jarEntry = jarFile.getJarEntry("lib/junit-4.12.jar");
        JarFile nestJarFile = jarFile.getNestedJarFile(jarEntry);
        Manifest nestManifest = nestJarFile.getManifest();
        assertTrue(nestManifest.getMainAttributes().getValue("Implementation-Title")
            .equals("JUnit"));
        assertTrue(nestManifest.getMainAttributes().getValue("Implementation-Version")
            .equals("4.12"));
    }
}
