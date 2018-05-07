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
package com.alipay.sofa.ark;

import com.alipay.sofa.ark.container.test.TestClassLoader;
import com.alipay.sofa.ark.springboot.SpringApplication;
import com.alipay.sofa.ark.springboot.facade.SampleService;
import com.alipay.sofa.ark.springboot.runner.ArkBootRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
@RunWith(ArkBootRunner.class)
@SpringBootTest(classes = SpringApplication.class)
public class ArkBootRunnerTest {

    @Autowired
    public SampleService sampleService;

    @Test
    public void test() {
        Assert.assertNotNull(sampleService);
        Assert.assertTrue("SampleService".equals(sampleService.say()));

        ArkBootRunner runner = new ArkBootRunner(ArkBootRunnerTest.class);
        Field field = ReflectionUtils.findField(ArkBootRunner.class, "runner");
        Assert.assertNotNull(field);

        ReflectionUtils.makeAccessible(field);
        BlockJUnit4ClassRunner springRunner = (BlockJUnit4ClassRunner) ReflectionUtils.getField(
            field, runner);
        Assert.assertTrue(springRunner.getClass().getCanonicalName()
            .equals(SpringRunner.class.getCanonicalName()));

        ClassLoader loader = springRunner.getTestClass().getJavaClass().getClassLoader();
        Assert.assertTrue(loader.getClass().getCanonicalName()
            .equals(TestClassLoader.class.getCanonicalName()));
    }

}