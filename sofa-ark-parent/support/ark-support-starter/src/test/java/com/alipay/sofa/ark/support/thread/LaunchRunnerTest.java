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
package com.alipay.sofa.ark.support.thread;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class LaunchRunnerTest {

    public static int   count = 0;
    public final Object lock  = new Object();

    public static void add(String[] args) {
        if (args.length > 0) {
            LaunchRunnerTest.count += Integer.valueOf(args[0]);
        }
    }

    @Before
    public void init() {
        LaunchRunnerTest.count = 0;
    }

    @Test
    public void testMainWithNoParameters() {
        synchronized (lock) {
            LaunchRunner launchRunner = new LaunchRunner(MainClass.class.getName());
            launchRunner.run();
            Assert.assertTrue(LaunchRunnerTest.count == 0);
        }
    }

    @Test
    public void testMainWithParameters() {
        synchronized (lock) {
            LaunchRunner launchRunner = new LaunchRunner(MainClass.class.getName(),
                new String[] { "10" });
            launchRunner.run();
            Assert.assertTrue(LaunchRunnerTest.count == 10);
        }
    }

    @Test
    public void testNotMainMethodWithNoParameters() {
        synchronized (lock) {
            LaunchRunner launchRunner = new LaunchRunner(LaunchRunnerTest.class.getName(), "add",
                new String[] {});
            launchRunner.run();
            Assert.assertTrue(LaunchRunnerTest.count == 0);
        }
    }

    @Test
    public void testNotMainMethodWithParameters() {
        synchronized (lock) {
            LaunchRunner launchRunner = new LaunchRunner(LaunchRunnerTest.class.getName(), "add",
                new String[] { "10" });
            launchRunner.run();
            Assert.assertTrue(LaunchRunnerTest.count == 10);
        }
    }

    public static class MainClass {

        private static void main(String[] args) {
            if (args.length > 0) {
                LaunchRunnerTest.count += Integer.valueOf(args[0]);
            }
        }

    }

}