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

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class HandlerTest {

    private URL     url     = this.getClass().getClassLoader()
                                .getResource("sample-biz-withjar.jar");

    private Handler handler = new Handler();

    @Test(expected = MalformedURLException.class)
    public void testOpenConnectionWithIOException() throws Exception {
        handler.openConnection(this.getClass().getClassLoader()
            .getResource("sample-biz-withjar.jar"));
    }

    @Test(expected = NullPointerException.class)
    public void testOpenConnectionWithNPE() throws Exception {
        handler.openConnection(this.getClass().getClassLoader()
            .getResource("sample-biz-withjar.jar!/lib"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseURLWithIllegalSpec() throws Exception {
        handler.parseURL(url, "/", 0, 1);
    }

    @Test(expected = SecurityException.class)
    public void testParseURLWithEmptySpec() throws Exception {
        handler.parseURL(url, "/", 0, 0);
    }

    @Test
    public void testReplaceParentDir() throws Exception {
        assertEquals("../a", handler.replaceParentDir("/../../a"));
        assertEquals("../", handler.replaceParentDir("/../../"));
        assertEquals("aaa", handler.replaceParentDir("/../aaa/../aaa"));
    }
}
