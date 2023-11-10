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
package com.alipay.sofa.ark.tools;

import com.alipay.sofa.ark.tools.JarWriter.ZipHeaderPeekInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.tools.LibraryScope.MODULE;
import static org.junit.Assert.assertEquals;

/**
 * @author lylingzhen
 * @since 2.2.0
 */
public class JarWriterTest {

    private JarWriter jarWriter;

    private File      file        = new File("./JarWriterTest");

    private String    jarFilePath = this.getClass().getClassLoader().getResource("test-jar.jar")
                                      .getFile();

    @Before
    public void setUp() throws IOException {
        file.createNewFile();
        jarWriter = new JarWriter(file);
    }

    @After
    public void tearDown() {
        file.delete();
    }

    @Test
    public void testWriteManifest() throws Exception {
        Manifest manifest = new Manifest();
        jarWriter.writeManifest(manifest);
    }

    @Test
    public void testWriteMethods() throws IOException {

        jarWriter.writeMarkEntry();

        JarFile jarFile = new JarFile(jarFilePath);
        jarWriter.writeBootstrapEntry(jarFile);

        Library library = new Library(new File(jarFilePath), MODULE);
        jarWriter.writeNestedLibrary("./", library);
        jarWriter.writeLoaderClasses(new JarInputStream(new FileInputStream(jarFilePath)));

        ZipHeaderPeekInputStream zipHeaderPeekInputStream = new ZipHeaderPeekInputStream(
            new FileInputStream(jarFilePath));
        assertEquals(80, zipHeaderPeekInputStream.read());
    }
}
