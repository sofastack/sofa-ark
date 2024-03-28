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
import com.alipay.sofa.ark.spi.archive.Archive.Entry;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import static com.alipay.sofa.ark.spi.constant.Constants.ARK_BIZ_MARK_ENTRY;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DirectoryBizArchiveTest {

    private DirectoryBizArchive directoryBizArchive;

    @Before
    public void setUp() throws MalformedURLException {
        directoryBizArchive = new DirectoryBizArchive("a", "b", new URL[] { new URL("file://a") });
    }

    @Test
    public void testDirectoryBizArchive() throws Exception {

        assertFalse(directoryBizArchive.isTestMode());
        assertEquals("a", directoryBizArchive.getClassName());
        assertEquals("b", directoryBizArchive.getMethodName());
        assertArrayEquals(new URL[]{new URL("file://a")}, directoryBizArchive.getUrls());

        assertTrue(directoryBizArchive.isEntryExist(entry -> !entry.isDirectory() && entry.getName().equals(ARK_BIZ_MARK_ENTRY)));
        assertEquals(6, directoryBizArchive.getManifest().getMainAttributes().size());

        try {
            directoryBizArchive.getUrl();
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryBizArchive.getNestedArchives(entry -> false);
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryBizArchive.getInputStream(null);
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryBizArchive.iterator();
            assertTrue(false);
        } catch (Exception e) {
        }

        Archive nestedArchive = directoryBizArchive.getNestedArchive(new Entry() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public String getName() {
                return ARK_BIZ_MARK_ENTRY;
            }
        });

        Field field = JarBizArchive.class.getDeclaredField("archive");
        field.setAccessible(true);
        assertNull(field.get(nestedArchive));
    }

    @Test
    public void testJarBizArchive() throws Exception {

        Archive archive = mock(Archive.class);
        JarBizArchive jarBizArchive = new JarBizArchive(archive);

        Iterator iterator = singletonList(new Entry() {
            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public String getName() {
                return "lib/export/a";
            }
        }).iterator();

        when(archive.iterator()).thenReturn((Iterator<Entry>) iterator);
        when(archive.getUrl()).thenReturn(new URL("file://a"));
        when(archive.getNestedArchive(any())).thenReturn(archive);

        assertArrayEquals(new URL[] { new URL("file://a"), new URL("file://a") },
            jarBizArchive.getExportUrls());
        assertNull(jarBizArchive.getInputStream(null));
    }
}
