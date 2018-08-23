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
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.testdata.classloader.ClassloaderTestClass;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test class load concurrency
 * @author ruoshan
 * @since 0.5.0
 */
public class ClassloaderConcurrencyTest extends BaseTest {

    private URL classPathURL = ClassloaderConcurrencyTest.class.getClassLoader().getResource("");

    @Test
    public void concurrencyLoadClass() {
        final BizClassLoader bizClassLoader = new BizClassLoader("test:1.0",
            new URL[] { classPathURL });

        final AtomicBoolean result = new AtomicBoolean(true);

        int totalTimes = 100;
        ThreadPoolExecutor executor = new CommonThreadPool().getExecutor();
        final CountDownLatch countDownLatch = new CountDownLatch(totalTimes);

        for (int index = 0; index < totalTimes; index++) {
            if (result.get()) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bizClassLoader.loadClass(ClassloaderTestClass.class.getName(), true);
                        } catch (ClassNotFoundException e) {
                            // ingore
                        } catch (LinkageError e) {
                            result.set(false);
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            } else {
                countDownLatch.countDown();
            }
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            // ignore
        } finally {
            executor.shutdown();
        }

        Assert.assertTrue("should not get linkega error when load class", result.get());
    }
}