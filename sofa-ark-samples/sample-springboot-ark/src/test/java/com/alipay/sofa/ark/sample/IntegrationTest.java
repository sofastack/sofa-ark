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
package com.alipay.sofa.ark.sample;

import com.alipay.sofa.ark.sample.facade.SampleService;
import com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication;
import com.alipay.sofa.ark.springboot.runner.ArkBootRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author qilong.zql 18/3/31-下午4:14
 */
@RunWith(ArkBootRunner.class)
@SpringBootTest(classes = SpringbootDemoApplication.class)
public class IntegrationTest {

    @Autowired
    private SampleService sampleService;

    @Test
    public void test() {
        Assert.assertTrue("A Sample Service".equals(sampleService.service()));
    }

}