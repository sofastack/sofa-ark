package com.alipay.sofa.ark.tools;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.jar.JarFile;

import static com.alipay.sofa.ark.tools.MainClassFinder.findSingleMainClass;
import static org.junit.Assert.assertEquals;

public class MainClassFinderTest {

    private String jarFilePath = this.getClass().getClassLoader().getResource("test-jar.jar").getFile();

    private JarFile jarFile;

    @Before
    public void setUp() throws IOException {
        jarFile = new JarFile(jarFilePath);
    }

    @Test
    public void testFindSingleMainClass() throws IOException {
        assertEquals("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication",
                findSingleMainClass(jarFile, "", ""));
    }
}
