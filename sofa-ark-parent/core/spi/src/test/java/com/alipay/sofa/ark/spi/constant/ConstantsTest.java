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
package com.alipay.sofa.ark.spi.constant;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.alipay.sofa.ark.spi.constant.Constants.*;
import static java.lang.Class.forName;
import static org.junit.Assert.assertEquals;

public class ConstantsTest {

    @Test
    public void testAllMethods() throws Exception {
        forName("com.alipay.sofa.ark.spi.constant.Constants");
        List<String> channelQuits = new ArrayList<>();
        channelQuits.add("quit");
        channelQuits.add("q");
        channelQuits.add("exit");
        assertEquals(channelQuits, CHANNEL_QUIT);
        assertEquals(new String(new byte[] { (byte) 13, (byte) 10 }), TELNET_STRING_END);
        assertEquals("", DEFAULT_PROFILE);
    }
}
