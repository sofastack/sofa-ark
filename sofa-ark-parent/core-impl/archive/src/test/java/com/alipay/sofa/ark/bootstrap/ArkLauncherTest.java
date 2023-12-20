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

import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.loader.EmbedClassPathArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.Archive;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.alipay.sofa.ark.bootstrap.ArkLauncher.main;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * @author yan
 * @version ArkLauncherTest.java, v 0.1 2023年10月17日 20:28 yan
 */
public class ArkLauncherTest {

    static MockedStatic<ManagementFactory> managementFactoryMockedStatic;

    @BeforeClass
    public static void setup() {

        List<String> mockArguments = new ArrayList<>();
        String filePath = ClasspathLauncherTest.class.getClassLoader()
                .getResource("SampleClass.class").getPath();
        String workingPath = new File(filePath).getParent();
        mockArguments.add(String.format("-javaagent:%s", workingPath));

        RuntimeMXBean runtimeMXBean = Mockito.mock(RuntimeMXBean.class);
        when(runtimeMXBean.getInputArguments()).thenReturn(mockArguments);

        managementFactoryMockedStatic = mockStatic(ManagementFactory.class);
        managementFactoryMockedStatic.when(ManagementFactory::getRuntimeMXBean).thenReturn(runtimeMXBean);
    }

    @AfterClass
    public static void tearDown() {
        managementFactoryMockedStatic.close();
    }

    @Test
    public void testContainerClassLoader() throws Exception {

        URL url = this.getClass().getClassLoader().getResource("sample-springboot-fat-biz.jar");
        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        assertEquals(1, agentUrl.length);

        List<URL> urls = new ArrayList<>();
        JarFileArchive jarFileArchive = new JarFileArchive(new File(url.getFile()));
        List<Archive> archives = jarFileArchive.getNestedArchives(this::isNestedArchive);
        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }
        urls.addAll(Arrays.asList(agentUrl));

        EmbedClassPathArchive classPathArchive = new EmbedClassPathArchive(
                this.getClass().getCanonicalName(), null, urls.toArray(new URL[]{}));
        ArkLauncher arkLauncher = new ArkLauncher(classPathArchive);
        ClassLoader classLoader = arkLauncher.createContainerClassLoader(classPathArchive.getContainerArchive());
        assertNotNull(classLoader);

        try {
            Class clazz = classLoader.loadClass("com.alipay.sofa.ark.bootstrap.ArkLauncher");
            assertNotNull(clazz);
            clazz = classLoader.loadClass("SampleClass");
            assertNotNull(clazz);
        } catch (Exception e) {
            assertTrue("loadClass class failed ", false);
        }

        assertThrows(ClassNotFoundException.class, () -> classLoader.loadClass("NotExistClass"));
    }

    protected boolean isNestedArchive(Archive.Entry entry) {
        return entry.isDirectory() ? entry.getName().equals("BOOT-INF/classes/") : entry.getName()
            .startsWith("BOOT-INF/lib/");
    }

    @Test(expected = Exception.class)
    public void testMain() throws Exception {
        main(null);
    }
}
