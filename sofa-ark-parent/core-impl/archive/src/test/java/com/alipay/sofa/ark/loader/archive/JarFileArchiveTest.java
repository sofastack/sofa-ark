package com.alipay.sofa.ark.loader.archive;

import com.alipay.sofa.ark.loader.archive.JarFileArchive.JarFileEntry;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JarFileArchiveTest {

    private JarFileArchive jarFileArchive;

    private String jarFilePath = this.getClass().getClassLoader().getResource("./sample-springboot-fat-biz.jar").getFile();

    @Before
    public void setUp() throws IOException {
        jarFileArchive = new JarFileArchive(new File(jarFilePath));
    }

    @Test
    public void testGetMethods() throws Exception {

        assertTrue(jarFileArchive.getManifest() != null);
        assertEquals(50, jarFileArchive.getNestedArchives(entry -> entry.getName().contains(".jar")).size());

        JarEntry jarEntry = new JarEntry("BOOT-INF/lib/slf4j-api-1.7.21.jar");
        jarEntry.setComment("UNPACK:xxx");
        assertTrue(jarFileArchive.getNestedArchive(new JarFileEntry(jarEntry)).getUrl().getFile().endsWith("slf4j-api-1.7.21.jar"));
    }
}
