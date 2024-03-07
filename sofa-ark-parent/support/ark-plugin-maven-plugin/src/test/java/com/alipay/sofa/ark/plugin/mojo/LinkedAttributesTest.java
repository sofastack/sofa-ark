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
package com.alipay.sofa.ark.plugin.mojo;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import static java.util.jar.Attributes.Name.*;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.Assert.assertEquals;

public class LinkedAttributesTest {

    private LinkedAttributes linkedAttributes = new LinkedAttributes();

    @Test
    public void testLinkedAttributesAllMethods() throws Exception {

        assertEquals(0, linkedAttributes.entrySet().size());
        linkedAttributes.put(EXTENSION_NAME, "b");
        assertEquals(1, linkedAttributes.entrySet().size());

        linkedAttributes.remove(EXTENSION_NAME);
        assertEquals(0, linkedAttributes.entrySet().size());

        linkedAttributes.put(MANIFEST_VERSION, "1.0.0");
        linkedAttributes.put(CONTENT_TYPE, "d");
        linkedAttributes.put(IMPLEMENTATION_TITLE, repeat("f", 150));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        linkedAttributes.writeMain(new DataOutputStream(outputStream));
        outputStream.flush();

        assertEquals("Manifest-Version: 1.0.0\r\n" + "Content-Type: d\r\n"
                     + "Implementation-Title: " + repeat("f", 48) + "\r\n " + repeat("f", 69)
                     + "\r\n " + repeat("f", 33) + "\r\n\r\n", outputStream.toString("UTF-8"));
    }

    @Test
    public void testLinkedManifestWriteEmpty() throws Exception {

        LinkedManifest linkedManifest = new LinkedManifest();
        linkedManifest.getMainAttributes().putValue("a", "b");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        linkedManifest.write(outputStream);

        outputStream.flush();
        assertEquals("\r\n", outputStream.toString("UTF-8"));
    }
}
