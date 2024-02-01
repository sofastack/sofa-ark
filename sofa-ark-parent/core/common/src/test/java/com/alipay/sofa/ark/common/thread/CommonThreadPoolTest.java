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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;

import static com.alipay.sofa.ark.common.thread.ThreadPoolManager.*;
import static com.alipay.sofa.ark.common.util.ThreadPoolUtils.buildQueue;
import static java.lang.Integer.MAX_VALUE;
import static org.apache.commons.beanutils.BeanUtils.copyProperties;
import static org.junit.Assert.*;

public class CommonThreadPoolTest {

    private CommonThreadPool commonThreadPool;

    @Test
    public void testCommonThreadPool() throws Exception {

        CommonThreadPool commonThreadPool = new CommonThreadPool();
        copyProperties(commonThreadPool, commonThreadPool);
        commonThreadPool.setPrestartAllCoreThreads(true);
        assertNotNull(commonThreadPool.getExecutor());

        registerThreadPool("a", commonThreadPool);
        assertNotNull(getThreadPool("a"));
        unRegisterUserThread("a");
        assertNull(getThreadPool("a"));
    }

    @Test
    public void testBuildQueue() {

        BlockingQueue<Runnable> queue = buildQueue(5, true);
        assertEquals(PriorityBlockingQueue.class, queue.getClass());
        assertEquals(MAX_VALUE, queue.remainingCapacity());

        queue = buildQueue(-1, true);
        assertEquals(PriorityBlockingQueue.class, queue.getClass());
        assertEquals(0, queue.size());

        queue = buildQueue(5, false);
        assertEquals(LinkedBlockingDeque.class, queue.getClass());
        assertEquals(5, queue.remainingCapacity());

        queue = buildQueue(-1, false);
        assertEquals(LinkedBlockingDeque.class, queue.getClass());
        assertEquals(0, queue.size());
    }
}
