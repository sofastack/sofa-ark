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

import com.alipay.sofa.ark.container.ArkContainer;
import com.alipay.sofa.ark.container.ArkContainerTest;
import com.alipay.sofa.ark.container.BaseTest;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.hook.TestBizClassLoaderHook;
import com.alipay.sofa.ark.container.service.extension.ExtensionLoaderServiceImpl;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderHook;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Map;

import static com.alipay.sofa.ark.spi.constant.Constants.BIZ_CLASS_LOADER_HOOK;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class ClassLoaderHookTest extends BaseTest {
    private URL  jarURL = ArkContainerTest.class.getClassLoader().getResource("test.jar");
    ArkContainer arkContainer;

    @Override
    public void before() {
        String[] args = new String[] { "-Ajar=" + jarURL.toExternalForm() };
        arkContainer = (ArkContainer) ArkContainer.main(args);
        cleanExtensionCache();
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);
        Plugin plugin = new PluginModel().setPluginName("mock")
            .setPluginClassLoader(this.getClass().getClassLoader()).setImportClasses("")
            .setImportPackages("").setImportResources("");
        pluginManagerService.registerPlugin(plugin);
    }

    @Override
    public void after() {
        arkContainer.stop();
        cleanExtensionCache();
    }

    public void cleanExtensionCache() {
        try {
            Field field = ExtensionLoaderServiceImpl.class.getDeclaredField("EXTENSION_MAP");
            field.setAccessible(true);
            Map extensionCache = (Map) field.get(null);
            extensionCache.clear();
        } catch (Throwable throwable) {
            // ignore
        }
    }

    @Test
    public void testBizClassLoaderSPI() throws Throwable {
        ClassLoaderHook mock = ArkServiceLoader.loadExtension("mock", ClassLoaderHook.class,
            BIZ_CLASS_LOADER_HOOK);
        Assert.assertTrue(mock instanceof TestBizClassLoaderHook);

        ClassLoaderService service = ArkServiceContainerHolder.getContainer().getService(
            ClassLoaderService.class);
        PluginManagerService pluginManagerService = ArkServiceContainerHolder.getContainer()
            .getService(PluginManagerService.class);

        Plugin location = pluginManagerService.getPluginsInOrder().get(0);
        Class aClass = mock.preFindClass("A.A", service, location);
        Assert.assertTrue(TestBizClassLoaderHook.ClassA.class.equals(aClass));

        Class bClass = mock.postFindClass("B", service, location);
        Assert.assertTrue(TestBizClassLoaderHook.ClassB.class.equals(bClass));

        Assert.assertTrue(mock.preFindResource("R1", service, location).getFile()
            .endsWith("pluginA_export_resource1.xml"));

        Assert.assertTrue(mock.postFindResource("any", service, location).getFile()
            .endsWith("pluginA_export_resource2.xml"));

        Enumeration<URL> urls = mock.preFindResources("R2", service, location);
        Assert.assertTrue(urls.hasMoreElements());
        URL url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-biz.jar"));

        urls = mock.postFindResources("any", service, location);
        Assert.assertTrue(urls.hasMoreElements());
        url = urls.nextElement();
        Assert.assertFalse(urls.hasMoreElements());
        Assert.assertTrue(url.getFile().contains("sample-plugin.jar"));
    }

    @Test
    public void testPluginClassLoaderSPI() throws Throwable {
        PluginClassLoader pluginClassLoader = new PluginClassLoader("mock", ((URLClassLoader) this
            .getClass().getClassLoader()).getURLs());
        Assert.assertTrue(TestBizClassLoaderHook.ClassA.class.equals(pluginClassLoader
            .loadClass("A.A")));
        Assert.assertTrue(TestBizClassLoaderHook.ClassB.class.equals(pluginClassLoader
            .loadClass("B")));
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