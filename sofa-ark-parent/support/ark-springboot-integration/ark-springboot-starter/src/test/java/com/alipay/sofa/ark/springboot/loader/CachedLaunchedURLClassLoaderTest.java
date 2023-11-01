package com.alipay.sofa.ark.springboot.loader;

import com.alipay.sofa.ark.springboot.loader.CachedLaunchedURLClassLoader;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.loader.archive.ExplodedArchive;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CachedLaunchedURLClassLoaderTest {

    private CachedLaunchedURLClassLoader cachedLaunchedURLClassLoader;

    @Before
    public void setUp() throws Exception {
        File bizJarFile = new File("src/test/resources/sample-biz-0.3.0.jar");
        cachedLaunchedURLClassLoader = new CachedLaunchedURLClassLoader(
                true, new ExplodedArchive(new File("src/test/resources")),
                new URL[] { new URL("file:///" + bizJarFile.getAbsolutePath()) }, this.getClass().getClassLoader());
    }

    @Test
    public void testLoadClass() throws Exception {

        try {
            cachedLaunchedURLClassLoader.loadClass("a", true);
            assertTrue(false);
        } catch (ClassNotFoundException cnfe) {
        }

        try {
            cachedLaunchedURLClassLoader.loadClass("a", true);
            assertTrue(false);
        } catch (ClassNotFoundException cnfe) {
        }

        assertEquals(CachedLaunchedURLClassLoaderTest.class, cachedLaunchedURLClassLoader.loadClass("com.alipay.sofa.ark.springboot.loader.CachedLaunchedURLClassLoaderTest", true));
        assertEquals(CachedLaunchedURLClassLoaderTest.class, cachedLaunchedURLClassLoader.loadClass("com.alipay.sofa.ark.springboot.loader.CachedLaunchedURLClassLoaderTest", true));


    }
}
