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
package com.alipay.sofa.ark.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Thread pool utils
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class ThreadPoolUtils {

    /**
     * Build Queue
     *
     * @param size size of queue
     * @return queue
     */
    public static BlockingQueue<Runnable> buildQueue(int size) {
        return buildQueue(size, false);
    }

    /**
     * Build Queue
     *
     * @param size size of queue
     * @param isPriority whether use priority queue or not
     * @return queue
     */
    public static BlockingQueue<Runnable> buildQueue(int size, boolean isPriority) {
        BlockingQueue<Runnable> queue;
        if (size == 0) {
            queue = new SynchronousQueue<>();
        } else {
            if (isPriority) {
                queue = size < 0 ? new PriorityBlockingQueue<Runnable>()
                    : new PriorityBlockingQueue<Runnable>(size);
            } else {
                queue = size < 0 ? new LinkedBlockingDeque<Runnable>()
                    : new LinkedBlockingDeque<Runnable>(size);
            }
        }
        return queue;
    }
}