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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

/**
 * Utility class that can be used to repackage an archive so that it can be executed using
 * {@literal java -jar}
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public class Repackager {

    private static final String                   ARK_VERSION_ATTRIBUTE              = "Sofa-Ark-Version";

    private static final String                   ARK_CONTAINER_ROOT                 = "Ark-Container-Root";

    private static final byte[]                   ZIP_FILE_HEADER                    = new byte[] {
            'P', 'K', 3, 4                                                          };

    private static final long                     FIND_WARNING_TIMEOUT               = TimeUnit.SECONDS
                                                                                         .toMillis(10);

    private static final String                   SPRING_BOOT_APPLICATION_CLASS_NAME = "org.springframework.boot.autoconfigure.SpringBootApplication";

    private List<MainClassTimeoutWarningListener> mainClassTimeoutListeners          = new ArrayList<>();

    private String                                mainClass;

    private String                                bizName;

    private String                                bizVersion;

    private String                                priority;

    private LinkedHashSet<String>                 denyImportPackages;

    private LinkedHashSet<String>                 denyImportClasses;

    private LinkedHashSet<String>                 denyImportResources;

    private final File                            source;

    private File                                  executableFatJar;

    private File                                  pluginModuleJar;

    private boolean                               packageProvided;

    private boolean                               keepArkBizJar;

    private String                                arkVersion                         = null;

    private Library                               arkContainerLibrary                = null;

    private final List<Library>                   standardLibraries                  = new ArrayList<>();

    private final List<Library>                   arkPluginLibraries                 = new ArrayList<>();

    private final List<Library>                   arkModuleLibraries                 = new ArrayList<>();

    public Repackager(File source) {
        if (source == null) {
            throw new IllegalArgumentException("Source file must be provided");
        }
        if (!source.exists() || !source.isFile()) {
            throw new IllegalArgumentException("Source must refer to an existing file, " + "got"
                                               + source.getAbsolutePath());
        }
        this.source = source.getAbsoluteFile();
    }

    /**
     * Add a listener that will be triggered to display a warning if searching for the
     * main class takes too long.
     * @param listener the listener to add
     */
    public void addMainClassTimeoutWarningListener(MainClassTimeoutWarningListener listener) {
        this.mainClassTimeoutListeners.add(listener);
    }

    /**
     * Set the main class that should be run. If not specified the value from the
     * MANIFEST will be used, or if no manifest entry is found the archive will be
     * searched for a suitable class.
     * @param mainClass
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * Set the ark-biz name that represents a unique id during runtime, it can be
     * bind uniquely to a ark-biz
     *
     * @param bizName
     */
    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public void setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
    }

    public void setArkVersion(String arkVersion) {
        this.arkVersion = arkVersion;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDenyImportPackages(LinkedHashSet<String> denyImportPackages) {
        this.denyImportPackages = denyImportPackages;
    }

    public void setDenyImportClasses(LinkedHashSet<String> denyImportClasses) {
        this.denyImportClasses = denyImportClasses;
    }

    public void setDenyImportResources(LinkedHashSet<String> denyImportResources) {
        this.denyImportResources = denyImportResources;
    }

    /**
     * Repackage to the given destination so that it can be launched using '
     * {@literal java -jar}'.
     *
     * @param appDestination the executable fat jar's destination
     * @param moduleDestination the 'plug-in' module jar's destination
     * @param libraries the libraries required to run the archive
     * @throws IOException if the file cannot be repackaged
     */
    public void repackage(File appDestination, File moduleDestination, Libraries libraries)
                                                                                           throws IOException {

        if (appDestination == null || appDestination.isDirectory() || moduleDestination == null
            || moduleDestination.isDirectory()) {
            throw new IllegalArgumentException("Invalid destination");
        }
        if (libraries == null) {
            throw new IllegalArgumentException("Libraries must not be null");
        }
        if (alreadyRepackaged()) {
            return;
        }

        executableFatJar = appDestination;
        pluginModuleJar = moduleDestination;

        libraries.doWithLibraries(new LibraryCallback() {
            @Override
            public void library(Library library) throws IOException {

                if (LibraryScope.PROVIDED.equals(library.getScope()) && !isPackageProvided()) {
                    return;
                }

                if (!isZip(library.getFile())) {
                    return;
                }
                JarFile jarFile = new JarFile(library.getFile());
                try {

                    if (isArkContainer(jarFile)) {
                        if (arkContainerLibrary != null) {
                            throw new RuntimeException("duplicate SOFAArk Container dependency");
                        }
                        arkContainerLibrary = library;
                    } else if (isArkModule(jarFile)) {
                        library.setScope(LibraryScope.MODULE);
                        arkModuleLibraries.add(library);
                    } else if (isArkPlugin(jarFile)) {
                        library.setScope(LibraryScope.PLUGIN);
                        arkPluginLibraries.add(library);
                    } else {
                        standardLibraries.add(library);
                    }

                } finally {
                    jarFile.close();
                }

            }
        });

        repackageModule();
        repackageApp();
        removeArkBizJar();
    }

    private void repackageModule() throws IOException {
        File destination = pluginModuleJar.getAbsoluteFile();
        destination.delete();

        JarFile jarFileSource = new JarFile(source);
        JarWriter writer = new JarWriter(destination);
        Manifest manifest = buildModuleManifest(jarFileSource);

        try {
            writer.writeManifest(manifest);
            writer.writeEntries(jarFileSource);
            writer.writeMarkEntry();
            writeNestedLibraries(standardLibraries, Layouts.Module.module(), writer);
        } finally {
            jarFileSource.close();
            try {
                writer.close();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    private List<Library> getModuleLibraries() {
        List<Library> libraries = new ArrayList<>(arkModuleLibraries);
        Library moduleLibrary = new Library(pluginModuleJar.getAbsoluteFile(), LibraryScope.MODULE);
        libraries.add(moduleLibrary);
        return libraries;
    }

    private void repackageApp() throws IOException {
        File destination = executableFatJar.getAbsoluteFile();
        destination.delete();

        JarFile jarFileSource = new JarFile(arkContainerLibrary.getFile().getAbsoluteFile());
        JarWriter writer = new JarWriter(destination);
        Manifest manifest = buildAppManifest(new JarFile(pluginModuleJar));

        try {
            writer.writeManifest(manifest);
            writer.writeEntries(jarFileSource, new RenamingEntryTransformer(Layouts.Jar.jar()
                .getArkContainerLocation()));
            writeNestedLibraries(arkPluginLibraries, Layouts.Jar.jar(), writer);
            writeNestedLibraries(getModuleLibraries(), Layouts.Jar.jar(), writer);
        } finally {
            jarFileSource.close();
            try {
                writer.close();
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    private void removeArkBizJar() {
        if (!keepArkBizJar) {
            pluginModuleJar.getAbsoluteFile().deleteOnExit();
        }

    }

    private void writeNestedLibraries(List<Library> libraries, Layout layout, JarWriter writer)
                                                                                               throws IOException {
        Set<String> alreadySeen = new HashSet<>();
        for (Library library : libraries) {
            String destination = layout
                .getLibraryDestination(library.getName(), library.getScope());
            if (destination != null) {
                if (!alreadySeen.add(destination + library.getName())) {
                    throw new IllegalStateException("Duplicate library " + library.getName());
                }
                writer.writeNestedLibrary(destination, library);
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isZip(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            try {
                return isZip(fileInputStream);
            } finally {
                fileInputStream.close();
            }
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean isArkContainer(JarFile jarFile) {
        return jarFile.getEntry(Constants.ARK_CONTAINER_MARK_ENTRY) != null;
    }

    private boolean isArkPlugin(JarFile jarFile) {
        return jarFile.getEntry(Constants.ARK_PLUGIN_MARK_ENTRY) != null;
    }

    private boolean isArkModule(JarFile jarFile) {
        return jarFile.getEntry(Constants.ARK_BIZ_MARK_ENTRY) != null;
    }

    private boolean isZip(InputStream inputStream) throws IOException {
        for (int i = 0; i < ZIP_FILE_HEADER.length; i++) {
            if (inputStream.read() != ZIP_FILE_HEADER[i]) {
                return false;
            }
        }
        return true;
    }

    public Manifest buildModuleManifest(JarFile source) throws IOException {
        Manifest manifest = source.getManifest();
        if (manifest == null) {
            manifest = new Manifest();
            manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        }
        manifest = new Manifest(manifest);
        String startClass = this.mainClass;
        if (startClass == null) {
            startClass = manifest.getMainAttributes().getValue(MAIN_CLASS_ATTRIBUTE);
        }

        if (startClass == null) {
            startClass = findMainMethodWithTimeoutWarning(source);
        }

        if (startClass == null) {
            throw new IllegalStateException("Unable to find main class.");
        }

        manifest.getMainAttributes().putValue(MAIN_CLASS_ATTRIBUTE, startClass);
        manifest.getMainAttributes().putValue(ARK_BIZ_NAME, this.bizName);
        manifest.getMainAttributes().putValue(ARK_BIZ_VERSION, this.bizVersion);
        manifest.getMainAttributes().putValue(PRIORITY_ATTRIBUTE, priority);
        manifest.getMainAttributes().putValue(DENY_IMPORT_PACKAGES,
            StringUtils.listToStr(denyImportPackages, MANIFEST_VALUE_SPLIT));
        manifest.getMainAttributes().putValue(DENY_IMPORT_CLASSES,
            StringUtils.listToStr(denyImportClasses, MANIFEST_VALUE_SPLIT));
        manifest.getMainAttributes().putValue(DENY_IMPORT_RESOURCES,
            StringUtils.listToStr(denyImportResources, MANIFEST_VALUE_SPLIT));

        return manifest;
    }

    private Manifest buildAppManifest(JarFile source) throws IOException {
        Manifest manifest = source.getManifest();
        /* theoretically impossible */
        if (manifest == null) {
            manifest = new Manifest();
            manifest.getMainAttributes().putValue("Manifest-Version", "1.0");
        }
        manifest = new Manifest(manifest);
        manifest.getMainAttributes().putValue(MAIN_CLASS_ATTRIBUTE,
            Layouts.Jar.jar().getLauncherClassName());

        if (arkVersion == null || arkVersion.isEmpty()) {
            throw new IllegalStateException("must specify version of SOFAArk.");
        }

        manifest.getMainAttributes().putValue(ARK_VERSION_ATTRIBUTE, arkVersion);
        manifest.getMainAttributes().putValue(ARK_CONTAINER_ROOT,
            Layouts.Jar.jar().getArkContainerLocation());
        return manifest;
    }

    public String findMainMethodWithTimeoutWarning(JarFile source) throws IOException {
        long startTime = System.currentTimeMillis();
        String mainMethod = findMainMethod(source);
        long duration = System.currentTimeMillis() - startTime;
        if (duration > FIND_WARNING_TIMEOUT) {
            for (MainClassTimeoutWarningListener listener : this.mainClassTimeoutListeners) {
                listener.handleTimeoutWarning(duration, mainMethod);
            }
        }
        return mainMethod;
    }

    private String findMainMethod(JarFile source) throws IOException {
        return MainClassFinder
            .findSingleMainClass(source, null, SPRING_BOOT_APPLICATION_CLASS_NAME);
    }

    private boolean alreadyRepackaged() throws IOException {
        JarFile jarFile = new JarFile(this.source);
        try {
            Manifest manifest = jarFile.getManifest();
            return (manifest != null && manifest.getMainAttributes()
                .getValue(ARK_VERSION_ATTRIBUTE) != null);
        } finally {
            jarFile.close();
        }
    }

    public final File getModuleTargetFile() {
        return pluginModuleJar;
    }

    /**
     * Callback interface used to present a warning when finding the main class takes too
     * long.
     */
    public interface MainClassTimeoutWarningListener {

        /**
         * Handle a timeout warning.
         * @param duration the amount of time it took to find the main method
         * @param mainMethod the main method that was actually found
         */
        void handleTimeoutWarning(long duration, String mainMethod);

    }

    /**
     * An {@code EntryTransformer} that renames entries by applying a prefix.
     */
    private static final class RenamingEntryTransformer implements JarWriter.EntryTransformer {

        private final String namePrefix;

        private RenamingEntryTransformer(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public JarEntry transform(JarEntry entry) {
            JarEntry renamedEntry = new JarEntry(this.namePrefix + entry.getName());
            renamedEntry.setTime(entry.getTime());
            renamedEntry.setSize(entry.getSize());
            renamedEntry.setMethod(entry.getMethod());
            if (entry.getComment() != null) {
                renamedEntry.setComment(entry.getComment());
            }
            renamedEntry.setCompressedSize(entry.getCompressedSize());
            renamedEntry.setCrc(entry.getCrc());
            //setCreationTimeIfPossible(entry, renamedEntry);
            if (entry.getExtra() != null) {
                renamedEntry.setExtra(entry.getExtra());
            }
            return renamedEntry;
        }

    }

    public boolean isPackageProvided() {
        return packageProvided;
    }

    public void setPackageProvided(boolean packageProvided) {
        this.packageProvided = packageProvided;
    }

    public void setKeepArkBizJar(boolean keepArkBizJar) {
        this.keepArkBizJar = keepArkBizJar;
    }
}