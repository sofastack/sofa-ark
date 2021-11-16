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
import com.alipay.sofa.ark.spi.archive.BizArchive;
import mockit.Mock;
import mockit.MockUp;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ClasspathLauncherTest {

    static {
        new MockUp<ManagementFactory>() {
            @Mock
            public RuntimeMXBean getRuntimeMXBean() {
                return new MockUp<RuntimeMXBean>() {
                    @Mock
                    List<String> getInputArguments() {
                        List<String> mockArguments = new ArrayList<>();
                        String filePath = this.getClass().getClassLoader()
                            .getResource("SampleClass.class").getPath();
                        String workingPath = new File(filePath).getParent();
                        mockArguments.add(String.format("-javaagent:%s", workingPath));
                        return mockArguments;
                    }
                }.getMockInstance();
            }
        };
    }

    @Test
    public void testFilterAgentClasspath() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("sample-biz.jar");
        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        Assert.assertEquals(1, agentUrl.length);

        List<URL> urls = new ArrayList<>();
        urls.add(url);
        urls.addAll(Arrays.asList(agentUrl));

        ClasspathLauncher.ClassPathArchive classPathArchive = new ClasspathLauncher.ClassPathArchive(
            this.getClass().getCanonicalName(), null, urls.toArray(new URL[] {}));
        List<BizArchive> bizArchives = classPathArchive.getBizArchives();
        Assert.assertEquals(1, bizArchives.size());
        Assert.assertEquals(2, urls.size());
    }

    @Test
    public void testSpringBootFatJar() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("sample-springboot-fat-biz.jar");
        URL[] agentUrl = ClassLoaderUtils.getAgentClassPath();
        Assert.assertEquals(1, agentUrl.length);

        List<URL> urls = new ArrayList<>();
        JarFileArchive jarFileArchive = new JarFileArchive(new File(url.getFile()));
        List<Archive> archives = jarFileArchive.getNestedArchives(this::isNestedArchive);
        for (Archive archive : archives) {
            urls.add(archive.getUrl());
        }
        urls.addAll(Arrays.asList(agentUrl));


        EmbedClassPathArchive classPathArchive = new EmbedClassPathArchive(
                this.getClass().getCanonicalName(), null, urls.toArray(new URL[] {}));
        List<BizArchive> bizArchives = classPathArchive.getBizArchives();
        Assert.assertEquals(0, bizArchives.size());
        Assert.assertNotNull(classPathArchive.getContainerArchive());
        Assert.assertEquals(1, classPathArchive.getPluginArchives().size());
        Assert.assertEquals(archives.size() + 1, urls.size());
        Assert.assertEquals(3, classPathArchive.getConfClasspath().size());
        URLClassLoader classLoader = new URLClassLoader(classPathArchive.getContainerArchive().getUrls());
        try {
            Class clazz = classLoader.loadClass("com.alipay.sofa.ark.bootstrap.ArkLauncher");
            Assert.assertTrue(clazz != null);
        } catch (Exception e){
            Assert.assertTrue("loadClass class failed ",false);
        }
    }

    protected boolean isNestedArchive(Archive.Entry entry) {
        return entry.isDirectory() ? entry.getName().equals("BOOT-INF/classes/") : entry.getName()
            .startsWith("BOOT-INF/lib/");
    }

    @Test
    public void testConfClasspath() throws IOException {
        URLClassLoader urlClassLoader = (URLClassLoader) this.getClass().getClassLoader();
        ClasspathLauncher.ClassPathArchive classPathArchive = new ClasspathLauncher.ClassPathArchive(
            this.getClass().getCanonicalName(), null, urlClassLoader.getURLs());
        List<URL> confClasspath = classPathArchive.getConfClasspath();
        Assert.assertEquals(3, confClasspath.size());
    }

}