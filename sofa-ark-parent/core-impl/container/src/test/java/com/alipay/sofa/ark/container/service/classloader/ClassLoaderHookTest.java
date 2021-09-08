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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.service.classloader.hook.TestBizClassLoaderHook;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.hook.TestBizClassLoaderHook;
import com.alipay.sofa.ark.container.service.classloader.hook.TestDefaultBizClassLoaderHook;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import mockit.Expectations;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ClassLoaderHookTest extends BaseTest {
    @Override
    public void before() {
        super.before();
        registerMockBiz();
        registerMockPlugin();
    }

    @Test
    public void testBizClassLoaderSPI() throws Throwable {
        BizClassLoader bizClassLoader = new BizClassLoader("mock:1.0", ((URLClassLoader) this
            .getClass().getClassLoader()).getURLs());
        Assert.assertTrue(TestBizClassLoaderHook.ClassA.class.getName().equals(
            bizClassLoader.loadClass("A.A").getName()));
        Assert.assertTrue(TestBizClassLoaderHook.ClassB.class.getName().equals(
            bizClassLoader.loadClass("B").getName()));
        Assert.assertTrue(bizClassLoader.getResource("R1").getFile()
            .endsWith("pluginA_export_resource1.xml"));
        Assert.assertTrue(bizClassLoader.getResource("sample-biz.jar").getFile()
            .endsWith("sample-biz.jar"));
        Assert.assertTrue(bizClassLoader.getResource("any").getFile()
            .endsWith("pluginA_export_resource2.xml"));

        Enumeration<URL> urls = bizClassLoader.getResources("R2");
        Assert.assertTrue(urls.hasMoreElements());
        URL url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-biz.jar"));

        urls = bizClassLoader.getResources("test.jar");
        Assert.assertTrue(urls.hasMoreElements());
        url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("test.jar"));

        urls = bizClassLoader.getResources("any");
        Assert.assertTrue(urls.hasMoreElements());
        url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-plugin.jar"));
    }

    @Test
    public void testDefaultBizClassLoaderSPI() throws Throwable {
        new Expectations(ArkServiceLoader.class) {
            {
                ArkServiceLoader.loadExtensionFromArkBiz(ClassLoaderHook.class,
                    BIZ_CLASS_LOADER_HOOK, "mock_default_classloader:1.0");
                result = null;
            }
        };
        new Expectations(ArkClient.class) {
            {
                ArkClient.getMasterBiz();
                result = new BizModel().setBizName("mock_master_biz").setBizVersion("1.0")
                    .setClassLoader(this.getClass().getClassLoader());
            }
        };
        BizClassLoader bizClassLoader = new BizClassLoader("mock_default_classloader:1.0",
            ((URLClassLoader) this.getClass().getClassLoader()).getURLs());
        System.setProperty(BIZ_CLASS_LOADER_HOOK_DIR,
            "com.alipay.sofa.ark.container.service.classloader.hook.TestDefaultBizClassLoaderHook");
        Assert.assertTrue(TestDefaultBizClassLoaderHook.ClassDefaultA.class.getName().equals(
            bizClassLoader.loadClass("defaultA").getName()));
        System.clearProperty(BIZ_CLASS_LOADER_HOOK_DIR);
    }

    @Test
    public void testPluginClassLoaderSPI() throws Throwable {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("mock", ((URLClassLoader) this
            .getClass().getClassLoader()).getURLs());
        Assert.assertTrue(TestBizClassLoaderHook.ClassA.class.getName().equals(
            pluginClassLoader.loadClass("A.A").getName()));
        Assert.assertTrue(TestBizClassLoaderHook.ClassB.class.getName().equals(
            pluginClassLoader.loadClass("B").getName()));
        Assert.assertTrue(pluginClassLoader.getResource("R1").getFile()
            .endsWith("pluginA_export_resource1.xml"));
        Assert.assertTrue(pluginClassLoader.getResource("sample-biz.jar").getFile()
            .endsWith("sample-biz.jar"));
        Assert.assertTrue(pluginClassLoader.getResource("any").getFile()
            .endsWith("pluginA_export_resource2.xml"));

        Enumeration<URL> urls = pluginClassLoader.getResources("R2");
        Assert.assertTrue(urls.hasMoreElements());
        URL url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-biz.jar"));

        urls = pluginClassLoader.getResources("test.jar");
        Assert.assertTrue(urls.hasMoreElements());
        url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("test.jar"));

        urls = pluginClassLoader.getResources("any");
        Assert.assertTrue(urls.hasMoreElements());
        url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-plugin.jar"));
    }
}