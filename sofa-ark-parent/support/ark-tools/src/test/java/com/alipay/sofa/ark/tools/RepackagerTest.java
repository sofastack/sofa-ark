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
package com.alipay.sofa.ark.tools;

import com.alipay.sofa.ark.tools.Repackager.RenamingEntryTransformer;
import com.alipay.sofa.ark.tools.git.GitInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.alipay.sofa.ark.tools.LibraryScope.*;
import static com.alipay.sofa.ark.tools.Repackager.isZip;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.zip.ZipOutputStream.STORED;
import static org.junit.Assert.*;

/**
 * @author qilong.zql
 * @since 0.6.0
 */
public class RepackagerTest {

    private Repackager repackager;

    private String     jarFilePath = this.getClass().getClassLoader().getResource("test-jar.jar")
                                       .getFile();

    private File       jarFile     = new File(jarFilePath);

    @Before
    public void setUp() {
        repackager = new Repackager(jarFile);
    }

    @Test
    public void testZipFile() {
        URL testJarUrl = this.getClass().getClassLoader().getResource("test-jar.jar");
        URL testPomUrl = this.getClass().getClassLoader().getResource("test-pom.xml");
        assertNotNull(testJarUrl);
        assertNotNull(testPomUrl);
        assertTrue(isZip(new File(testJarUrl.getFile())));
        assertFalse(isZip(new File(testPomUrl.getFile())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithNullFile() throws IllegalArgumentException {
        new Repackager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructWithFileNotExists() throws IllegalArgumentException {
        new Repackager(new File("not_found"));
    }

    @Test
    public void testRepackage() throws Exception {

        Field field = Repackager.class.getDeclaredField("arkContainerLibrary");
        field.setAccessible(true);
        field.set(repackager, new Library("sofa-ark-2.0.jar", jarFile, CONTAINER, true));

        repackager.setArkVersion("2.0");
        repackager.setBaseDir(new File(this.getClass().getClassLoader().getResource("").getFile()));

        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("sofa-ark:2.0");
        repackager.setInjectPluginDependencies(linkedHashSet);

        field = Repackager.class.getDeclaredField("gitInfo");
        field.setAccessible(true);
        field.set(repackager, new GitInfo());

        Library library = new Library(jarFile, PLUGIN);
        repackager.repackage(new File("./target/dest"), new File("./target/module"), callback -> {
            callback.library(library);
        });

        field = Repackager.class.getDeclaredField("arkModuleLibraries");
        field.setAccessible(true);
        assertEquals(newArrayList(library), field.get(repackager));
        assertEquals(MODULE, ((List<Library>) field.get(repackager)).get(0).getScope());
        assertEquals("com.alipay.sofa.ark.sample.springbootdemo.SpringbootDemoApplication",
                repackager.findMainMethodWithTimeoutWarning(new JarFile(jarFile)));
    }

    @Test
    public void testSetInjectPluginDependencies() throws Exception {

        repackager.setInjectPluginDependencies(null);
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("sofa-ark:1.0");
        repackager.setInjectPluginDependencies(linkedHashSet);

        Field field = Repackager.class.getDeclaredField("injectPluginDependencies");
        field.setAccessible(true);
        ArtifactItem item = new ArtifactItem();
        item.setArtifactId("sofa-ark");
        item.setVersion("1.0");
        assertEquals(newHashSet(item), field.get(repackager));
    }

    @Test
    public void testPrepareDeclaredLibraries() throws Exception {

        repackager.prepareDeclaredLibraries(null);
        repackager.setDeclaredMode(true);
        repackager.prepareDeclaredLibraries(null);

        ArtifactItem artifactItem = new ArtifactItem();
        artifactItem.setArtifactId("x");
        repackager.prepareDeclaredLibraries(newArrayList(artifactItem));

        Field field = Repackager.class.getDeclaredField("declaredLibraries");
        field.setAccessible(true);
        assertEquals(newHashSet("x"), field.get(repackager));
    }

    @Test
    public void testRenamingEntryTransformer() {
        RenamingEntryTransformer renamingEntryTransformer = new RenamingEntryTransformer(
            "my-prefix");
        JarEntry jarEntry = new JarEntry("my-entry");
        jarEntry.setComment("xxx");
        jarEntry.setExtra(new byte[] {});
        jarEntry.setSize(1);
        jarEntry.setMethod(STORED);
        jarEntry.setCrc(1);
        jarEntry = renamingEntryTransformer.transform(jarEntry);
        assertEquals("my-prefixmy-entry", jarEntry.getName());
    }

    @Test
    public void testOtherMethods() {
        repackager.addMainClassTimeoutWarningListener(null);
        repackager.setMainClass(null);
        repackager.setBizName(null);
        repackager.setBizVersion(null);
        repackager.setPriority(null);
        repackager.setDenyImportPackages(null);
        repackager.setDenyImportClasses(null);
        repackager.setDenyImportResources(null);
        repackager.setInjectPluginExportPackages(null);
    }
}