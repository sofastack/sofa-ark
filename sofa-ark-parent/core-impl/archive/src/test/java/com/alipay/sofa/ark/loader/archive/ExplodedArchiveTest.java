package com.alipay.sofa.ark.loader.archive;

import com.alipay.sofa.ark.spi.archive.Archive;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ExplodedArchiveTest {

    private ExplodedArchive explodedArchive;

    private String archiveTestDirPath = this.getClass().getClassLoader().getResource("./exploded-archive-test/").getFile();

    @Before
    public void setUp() {
        explodedArchive = new ExplodedArchive(new File(archiveTestDirPath));
    }

    @Test
    public void testGetMethods() throws Exception {

        assertTrue(explodedArchive.getUrl().getFile().endsWith("test-classes/exploded-archive-test/"));
        assertTrue(explodedArchive.getManifest() != null);

        List<Archive> nestedArchives = explodedArchive.getNestedArchives(entry -> !entry.getName().contains("META-INF"));
        assertEquals(2, nestedArchives.size());
        String nestedArchivesStr = nestedArchives.toString();
        assertTrue(nestedArchivesStr.contains("/example-jarinjarinjar.jar!/"));
        assertTrue(nestedArchivesStr.contains("/sample-biz.jar!/"));
    }
}
