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
package com.alipay.sofa.ark.loader;

import com.alipay.sofa.ark.spi.archive.Archive;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutableArkBizJarTest {

    private Archive             archive             = mock(Archive.class);

    private ExecutableArkBizJar executableArkBizJar = new ExecutableArkBizJar(archive);

    @Before
    public void setUp() {
        when(archive.iterator()).thenReturn(mock(Iterator.class));
    }

    @Test
    public void testExecutableArkBizJar() throws Exception {

        assertNull(executableArkBizJar.getManifest());
        assertNull(executableArkBizJar.getInputStream(null));
        assertNull(executableArkBizJar.getNestedArchive(null));

        try {
            executableArkBizJar.getContainerArchive();
            assertTrue(false);
        } catch (RuntimeException e) {
        }

        assertEquals(0, executableArkBizJar.getConfClasspath().size());
    }
}
