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
package com.alipay.sofa.ark.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Function;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileCopyDetails;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.bundling.Jar;

public class ArkJar extends Jar implements BootArchive {

    private static final String LAUNCHER = "com.alipay.sofa.ark.bootstrap.ArkLauncher";

    private static final String CLASSES_DIRECTORY = "classes/";

    private static final String LIB_DIRECTORY = "lib/";

    private static final String ARK_BIZ_MARK  = "com/alipay/sofa/ark/biz/mark";

    private static final String CLASSPATH_INDEX = "classpath.idx";

    private final ArkArchiveSupport support;

    private final CopySpec bootInfSpec;

    private final Property<String> mainClass;

    private FileCollection classpath;

    private File gitInfo;

    private SofaArkGradlePluginExtension arkExtension;


    /**
     * Creates a new {@code BootJar} task.
     */
    public ArkJar() {
        Project project = getProject();
        this.gitInfo = getGitDirectory(project);

        this.arkExtension = project.getExtensions().findByType(SofaArkGradlePluginExtension.class);
        this.support = new ArkArchiveSupport(LAUNCHER, new LibrarySpec(), new ZipCompressionResolver(), gitInfo, arkExtension);

        this.bootInfSpec = project.copySpec().into("");
        this.mainClass = project.getObjects().property(String.class);
        configureBootInfSpec(this.bootInfSpec);
        getMainSpec().with(this.bootInfSpec);

    }

    private File getGitDirectory(Project project) {
        File projectDir = project.getRootDir();
        File gitFolder = new File(projectDir, ".git");
        if (gitFolder.exists() && gitFolder.isDirectory()) {
            return gitFolder;
        }
        return new File(" ");
    }


    private void configureBootInfSpec(CopySpec bootInfSpec) {
        bootInfSpec.into("", fromCallTo(this::classpathDirectories));
        bootInfSpec.into("lib", fromCallTo(this::classpathFiles)).eachFile(this.support::excludeNonZipFiles);

        this.support.moveModuleInfoToRoot(bootInfSpec);
        moveMetaInfToRoot(bootInfSpec);
    }

    private Iterable<File> classpathDirectories() {
        return classpathEntries(File::isDirectory);
    }

    private Iterable<File> classpathFiles() {
        return classpathEntries(File::isFile);
    }

    private Iterable<File> classpathEntries(Spec<File> filter) {
        return (this.classpath != null) ? this.classpath.filter(filter) : Collections.emptyList();
    }

    private void moveMetaInfToRoot(CopySpec spec) {
        spec.eachFile((file) -> {
            String path = file.getRelativeSourcePath().getPathString();
            if (path.startsWith("META-INF/") && !path.equals("META-INF/aop.xml") && !path.endsWith(".kotlin_module")
                && !path.startsWith("META-INF/services/")) {
                this.support.moveToRoot(file);
            }
        });
    }

    @Override
    public void copy() {
        this.support.configureBizManifest(getManifest(), getMainClass().get(), CLASSES_DIRECTORY, LIB_DIRECTORY,
            CLASSPATH_INDEX);
        super.copy();
    }


    @Override
    protected CopyAction createCopyAction() {
        try {
            return this.support.createCopyAction(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Property<String> getMainClass() {
        return this.mainClass;
    }

    @Override
    public void requiresUnpack(String... patterns) {
        this.support.requiresUnpack(patterns);
    }

    @Override
    public void requiresUnpack(Spec<FileTreeElement> spec) {
        this.support.requiresUnpack(spec);
    }

    @Override
    public FileCollection getClasspath() {
        return this.classpath;
    }

    @Override
    public void classpath(Object... classpath) {
        FileCollection existingClasspath = this.classpath;
        this.classpath = getProject().files((existingClasspath != null) ? existingClasspath : Collections.emptyList(),
            classpath);
    }

    @Override
    public void setClasspath(Object classpath) {
        this.classpath = getProject().files(classpath);
    }

    @Override
    public void setClasspath(FileCollection classpath) {
        this.classpath = getProject().files(classpath);
    }

    /**
     * Returns a {@code CopySpec} that can be used to add content to the {@code BOOT-INF}
     * directory of the jar.
     * @return a {@code CopySpec} for {@code BOOT-INF}
     * @since 2.0.3
     */
    @Internal
    public CopySpec getBootInf() {
        CopySpec child = getProject().copySpec();
        this.bootInfSpec.with(child);
        return child;
    }


    /**
     * Return the {@link ZipCompression} that should be used when adding the file
     * represented by the given {@code details} to the jar. By default, any
     * {@link #isLibrary(FileCopyDetails) library} is {@link ZipCompression#STORED stored}
     * and all other files are {@link ZipCompression#DEFLATED deflated}.
     * @param details the file copy details
     * @return the compression to use
     */
    protected ZipCompression resolveZipCompression(FileCopyDetails details) {
        return isLibrary(details) ? ZipCompression.STORED : ZipCompression.DEFLATED;
    }

    /**
     * Return if the {@link FileCopyDetails} are for a library. By default any file in
     * {@code BOOT-INF/lib} is considered to be a library.
     * @param details the file copy details
     * @return {@code true} if the details are for a library
     * @since 2.3.0
     */
    protected boolean isLibrary(FileCopyDetails details) {
        String path = details.getRelativePath().getPathString();
        return path.startsWith(LIB_DIRECTORY);
    }


    /**
     * Syntactic sugar that makes {@link CopySpec#into} calls a little easier to read.
     * @param <T> the result type
     * @param callable the callable
     * @return an action to add the callable to the spec
     */
    private static <T> Action<CopySpec> fromCallTo(Callable<T> callable) {
        return (spec) -> spec.from(callTo(callable));
    }

    /**
     * Syntactic sugar that makes {@link CopySpec#from} calls a little easier to read.
     * @param <T> the result type
     * @param callable the callable
     * @return the callable
     */
    private static <T> Callable<T> callTo(Callable<T> callable) {
        return callable;
    }

    private final class LibrarySpec implements Spec<FileCopyDetails> {
        @Override
        public boolean isSatisfiedBy(FileCopyDetails details) {
            return isLibrary(details);
        }

    }

    private final class ZipCompressionResolver implements Function<FileCopyDetails, ZipCompression> {

        @Override
        public ZipCompression apply(FileCopyDetails details) {
            return resolveZipCompression(details);
        }

    }

}
