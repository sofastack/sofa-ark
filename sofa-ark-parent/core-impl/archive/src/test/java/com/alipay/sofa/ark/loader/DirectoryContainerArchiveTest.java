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

import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertTrue;

public class DirectoryContainerArchiveTest {

    private DirectoryContainerArchive directoryContainerArchive;

    @Test
    public void testDirectoryContainerArchive() throws Exception {

        directoryContainerArchive = new DirectoryContainerArchive(new URL[] { new URL("file://a") });

        try {
            directoryContainerArchive.getUrl();
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryContainerArchive.getManifest();
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryContainerArchive.getNestedArchives(null);
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryContainerArchive.getNestedArchive(null);
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryContainerArchive.getInputStream(null);
            assertTrue(false);
        } catch (Exception e) {
        }
        try {
            directoryContainerArchive.iterator();
            assertTrue(false);
        } catch (Exception e) {
        }
    }
}
