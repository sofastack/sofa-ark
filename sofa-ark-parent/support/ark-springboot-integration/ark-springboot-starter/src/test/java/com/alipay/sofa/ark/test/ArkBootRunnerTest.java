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
package com.alipay.sofa.ark.test;

import com.alipay.sofa.ark.container.test.TestClassLoader;
import com.alipay.sofa.ark.spi.event.ArkEvent;
import com.alipay.sofa.ark.spi.service.ArkInject;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.springboot.runner.ArkBootEmbedRunner;
import com.alipay.sofa.ark.test.springboot.BaseSpringApplication;
import com.alipay.sofa.ark.test.springboot.facade.SampleService;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.Comparator;

import static com.alipay.sofa.ark.test.springboot.TestValueHolder.getTestValue;
import static org.junit.Assert.*;
import static org.springframework.util.ReflectionUtils.*;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
@RunWith(ArkBootEmbedRunner.class)
@SpringBootTest(classes = BaseSpringApplication.class)
public class ArkBootRunnerTest {

    @Autowired
    public SampleService        sampleService;

    @ArkInject
    public PluginManagerService pluginManagerService;

    @ArkInject
    public EventAdminService    eventAdminService;

    @Test
    public void test() throws NoTestsRemainException {

        assertNotNull(sampleService);
        assertNotNull(pluginManagerService);
        assertEquals("SampleService", sampleService.say());

        ArkBootEmbedRunner runner = new ArkBootEmbedRunner(ArkBootRunnerTest.class);
        Field field = findField(ArkBootEmbedRunner.class, "runner");
        assertNotNull(field);

        makeAccessible(field);
        BlockJUnit4ClassRunner springRunner = (BlockJUnit4ClassRunner) getField(field, runner);
        assertTrue(springRunner.getClass().getCanonicalName()
            .equals(SpringRunner.class.getCanonicalName()));

        ClassLoader loader = springRunner.getTestClass().getJavaClass().getClassLoader();
        assertTrue(loader.getClass().getCanonicalName()
            .equals(TestClassLoader.class.getCanonicalName()));

        assertEquals(0, getTestValue());
        eventAdminService.sendEvent(new ArkEvent() {
            @Override
            public String getTopic() {
                return "test-event-A";
            }
        });
        assertEquals(10, getTestValue());
        eventAdminService.sendEvent(new ArkEvent() {
            @Override
            public String getTopic() {
                return "test-event-B";
            }
        });
        assertEquals(20, getTestValue());

        runner.filter(new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                return true;
            }

            @Override
            public String describe() {
                return "";
            }
        });
        runner.sort(new Sorter(new Comparator<Description>() {
            @Override
            public int compare(Description o1, Description o2) {
                return 0;
            }
        }) {
        });
    }

    /**
     * issue#234
     */
    @Test
    public void testLogClassCastBug() {
        Class<?> clazz = null;
        try {
            clazz = this.getClass().getClassLoader()
                .loadClass("org.apache.logging.slf4j.Log4jLoggerFactory");
        } catch (Throwable t) {
            System.out.println(t.getStackTrace().toString());
        }
        assertTrue(clazz.getClassLoader().toString().contains("TestClassLoader"));
    }
}
