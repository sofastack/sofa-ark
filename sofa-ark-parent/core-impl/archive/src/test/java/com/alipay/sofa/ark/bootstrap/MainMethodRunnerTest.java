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
package com.alipay.sofa.ark.bootstrap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class MainMethodRunnerTest {

    private static int count;

    @Before
    public void init() {
        MainMethodRunnerTest.count = 0;
    }

    @Test
    public void testRunner() {
        MainMethodRunner mainMethodRunner = new MainMethodRunner(MainClass.class.getName(),
            new String[] { "10" });

        try {
            mainMethodRunner.run();
            Assert.assertTrue(MainMethodRunnerTest.count == 10);
        } catch (Exception e) {
            Assert.assertNull(e);
        }

    }

    public static class MainClass {

        public static void main(String[] args) {
            if (args.length > 0) {
                MainMethodRunnerTest.count += Integer.valueOf(args[0]);
            }
        }

    }
}