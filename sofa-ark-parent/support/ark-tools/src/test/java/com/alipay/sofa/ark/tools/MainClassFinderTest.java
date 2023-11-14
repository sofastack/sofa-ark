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

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.jar.JarFile;

import static com.alipay.sofa.ark.tools.MainClassFinder.findSingleMainClass;
import static org.junit.Assert.assertEquals;

public class MainClassFinderTest {

    private String  jarFilePath = this.getClass().getClassLoader().getResource("test-jar.jar")
                                    .getFile();

    private JarFile jarFile;

    @Before
    public void setUp() throws IOException {
        jarFile = new JarFile(jarFilePath);
    }

    @Test
    public void testFindSingleMainClass() throws IOException {
        assertEquals("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication",
            findSingleMainClass(jarFile, "", ""));
    }
}
