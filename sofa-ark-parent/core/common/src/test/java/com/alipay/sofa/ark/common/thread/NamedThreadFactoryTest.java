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
package com.alipay.sofa.ark.common.thread;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for NamedThreadFactory.
 * This test verifies the thread factory works correctly on JDK 21+ where
 * SecurityManager has been deprecated/removed.
 *
 * @author Claude Code
 * @since 3.1.10
 */
public class NamedThreadFactoryTest {

    @Test
    public void testNewThreadWithDefaultDaemon() {
        NamedThreadFactory factory = new NamedThreadFactory("test-pool");
        Thread thread = factory.newThread(() -> {});

        assertNotNull("Thread should not be null", thread);
        assertTrue("Thread name should start with SOFA-ARK-",
            thread.getName().startsWith("SOFA-ARK-test-pool-"));
        assertFalse("Thread should not be daemon by default", thread.isDaemon());
        assertEquals("Thread priority should be NORM_PRIORITY",
            Thread.NORM_PRIORITY, thread.getPriority());
        assertNotNull("Thread group should not be null", thread.getThreadGroup());
    }

    @Test
    public void testNewThreadWithDaemonTrue() {
        NamedThreadFactory factory = new NamedThreadFactory("daemon-pool", true);
        Thread thread = factory.newThread(() -> {});

        assertNotNull("Thread should not be null", thread);
        assertTrue("Thread should be daemon", thread.isDaemon());
        assertTrue("Thread name should contain daemon-pool",
            thread.getName().contains("daemon-pool"));
    }

    @Test
    public void testNewThreadWithDaemonFalse() {
        NamedThreadFactory factory = new NamedThreadFactory("non-daemon-pool", false);
        Thread thread = factory.newThread(() -> {});

        assertNotNull("Thread should not be null", thread);
        assertFalse("Thread should not be daemon", thread.isDaemon());
    }

    @Test
    public void testThreadGroupIsCurrentThreadGroup() {
        // After removing SecurityManager dependency, thread group should be
        // the current thread's thread group
        ThreadGroup expectedGroup = Thread.currentThread().getThreadGroup();

        NamedThreadFactory factory = new NamedThreadFactory("group-test");
        Thread thread = factory.newThread(() -> {});

        assertEquals("Thread group should be current thread's group",
            expectedGroup, thread.getThreadGroup());
    }

    @Test
    public void testThreadNameUniqueness() {
        NamedThreadFactory factory = new NamedThreadFactory("unique-test");

        Thread thread1 = factory.newThread(() -> {});
        Thread thread2 = factory.newThread(() -> {});
        Thread thread3 = factory.newThread(() -> {});

        assertNotEquals("Thread names should be unique",
            thread1.getName(), thread2.getName());
        assertNotEquals("Thread names should be unique",
            thread2.getName(), thread3.getName());
        assertNotEquals("Thread names should be unique",
            thread1.getName(), thread3.getName());
    }

    @Test
    public void testThreadNameFormat() {
        NamedThreadFactory factory = new NamedThreadFactory("format-test");
        Thread thread = factory.newThread(() -> {});

        String name = thread.getName();
        // Name format: SOFA-ARK-{secondPrefix}-{poolCount}-T{threadCount}
        // Example: SOFA-ARK-format-test-0-T1
        assertTrue("Thread name should match expected format: " + name,
            name.matches("SOFA-ARK-format-test-\\d+-T\\d+"));
    }

    @Test
    public void testMultipleFactoriesHaveDifferentPoolCounts() {
        NamedThreadFactory factory1 = new NamedThreadFactory("pool1");
        NamedThreadFactory factory2 = new NamedThreadFactory("pool2");

        Thread thread1 = factory1.newThread(() -> {});
        Thread thread2 = factory2.newThread(() -> {});

        // Verify each factory has unique pool count
        String name1 = thread1.getName();
        String name2 = thread2.getName();

        // Extract pool count from names
        int poolCount1 = extractPoolCount(name1);
        int poolCount2 = extractPoolCount(name2);

        assertNotEquals("Different factories should have different pool counts",
            poolCount1, poolCount2);
    }

    @Test
    public void testRunnableIsExecuted() throws InterruptedException {
        final boolean[] executed = {false};
        NamedThreadFactory factory = new NamedThreadFactory("runnable-test");

        Thread thread = factory.newThread(() -> {
            executed[0] = true;
        });

        thread.start();
        thread.join(1000);

        assertTrue("Runnable should have been executed", executed[0]);
    }

    @Test
    public void testThreadPriorityNormalized() {
        NamedThreadFactory factory = new NamedThreadFactory("priority-test");
        Thread thread = factory.newThread(() -> {});

        // NamedThreadFactory ensures thread priority is NORM_PRIORITY
        assertEquals("Thread priority should be normalized to NORM_PRIORITY",
            Thread.NORM_PRIORITY, thread.getPriority());
    }

    private int extractPoolCount(String threadName) {
        // Format: SOFA-ARK-{prefix}-{poolCount}-T{threadCount}
        String[] parts = threadName.split("-");
        // parts[0] = SOFA, parts[1] = ARK, parts[2] = prefix, parts[3] = poolCount, parts[4] = T{threadCount}
        // But poolCount is after the prefix and before -T
        int tIndex = threadName.indexOf("-T");
        String beforeT = threadName.substring(0, tIndex);
        int lastDash = beforeT.lastIndexOf("-");
        return Integer.parseInt(beforeT.substring(lastDash + 1));
    }
}