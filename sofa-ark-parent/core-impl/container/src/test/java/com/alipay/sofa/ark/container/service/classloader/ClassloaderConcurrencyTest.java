/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2018 All Rights Reserved.
 */
package com.alipay.sofa.ark.container.service.classloader;

import com.alipay.sofa.ark.common.thread.CommonThreadPool;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.testdata.classloader.ClassloaderTestClass;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import org.junit.Assert;
import org.junit.Before;
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

    private URL classPathURL = ClassloaderConcurrencyTest.class.getClassLoader()
            .getResource("");

    @Test
    public void concurrencyLoadClass(){
        final BizClassLoader bizClassLoader = new BizClassLoader("test:1.0", new URL[]{classPathURL} );

        final AtomicBoolean result = new AtomicBoolean(true);

        int totalTimes = 100;
        ThreadPoolExecutor executor = new CommonThreadPool().getExecutor();
        final CountDownLatch countDownLatch = new CountDownLatch(totalTimes);

        for (int index = 0; index < totalTimes ; index++) {
            if (result.get()) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bizClassLoader.loadClass(ClassloaderTestClass.class.getName(), true);
                            countDownLatch.countDown();
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
        }catch (InterruptedException e){
            // ignore
        }finally {
            executor.shutdown();
        }

        Assert.assertTrue("should not get linkega error when load class", result.get());
    }
}