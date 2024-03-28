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
package com.alipay.sofa.ark.container.model;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.bootstrap.MainMethodRunner;
import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.ark.common.util.ClassLoaderUtils;
import com.alipay.sofa.ark.loader.jar.JarUtils;
import com.alipay.sofa.ark.common.util.ParseUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.container.service.classloader.AbstractClasspathClassLoader;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.jar.JarUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizRecycleEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStopEvent;
import com.alipay.sofa.ark.spi.event.biz.AfterBizFailedEvent;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.apache.commons.io.FileUtils.deleteQuietly;

/**
 * Ark Biz Standard Model
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class BizModel implements Biz {
    private String               bizName;

    private String               bizVersion;

    private BizState             bizState;

    private String               mainClass;

    private String               webContextPath;

    private URL[]                urls;

    private URL[]                pluginUrls;

    private ClassLoader          classLoader;

    private Map<String, String>  attributes                    = new ConcurrentHashMap<>();

    private int                  priority                      = DEFAULT_PRECEDENCE;

    private Set<String>          denyImportPackages;

    private Set<String>          denyImportPackageNodes        = new HashSet<>();

    private Set<String>          denyImportPackageStems        = new HashSet<>();

    private Set<String>          denyImportClasses;

    private Set<String>          denyImportResources           = new HashSet<>();

    private Set<String>          injectPluginDependencies      = new HashSet<>();
    private Set<String>          injectExportPackages          = new HashSet<>();

    private Set<String>          declaredLibraries             = new LinkedHashSet<>();
    private Map<String, Boolean> declaredCacheMap              = new ConcurrentHashMap<>();

    private Set<String>          denyPrefixImportResourceStems = new HashSet<>();

    private Set<String>          denySuffixImportResourceStems = new HashSet<>();

    private File                 bizTempWorkDir;

    private CopyOnWriteArrayList<BizStateChangeInfo> bizStateChangeLogs            = new CopyOnWriteArrayList<>();

    public BizModel setBizName(String bizName) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizName), "Biz Name must not be empty!");
        this.bizName = bizName;
        return this;
    }

    public BizModel setBizVersion(String bizVersion) {
        AssertUtils.isFalse(StringUtils.isEmpty(bizVersion), "Biz Version must not be empty!");
        this.bizVersion = bizVersion;
        return this;
    }

    public BizModel setBizState(BizState bizState) {
        this.bizState = bizState;
        addStateChangeLog();
        return this;
    }

    public BizModel setMainClass(String mainClass) {
        AssertUtils.isFalse(StringUtils.isEmpty(mainClass), "Biz Main Class must not be empty!");
        this.mainClass = mainClass;
        return this;
    }

    public BizModel setClassPath(URL[] urls) {
        this.urls = urls;
        return this;
    }

    public BizModel setPluginClassPath(URL[] urls) {
        this.pluginUrls = urls;
        return this;
    }

    public BizModel setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public BizModel setPriority(String priority) {
        this.priority = (priority == null ? DEFAULT_PRECEDENCE : Integer.valueOf(priority));
        return this;
    }

    public BizModel setWebContextPath(String webContextPath) {
        this.webContextPath = (webContextPath == null ? Constants.ROOT_WEB_CONTEXT_PATH
            : webContextPath);
        return this;
    }

    public BizModel setDenyImportPackages(String denyImportPackages) {
        this.denyImportPackages = StringUtils.strToSet(denyImportPackages,
            Constants.MANIFEST_VALUE_SPLIT);
        ParseUtils.parsePackageNodeAndStem(this.denyImportPackages, this.denyImportPackageStems,
            this.denyImportPackageNodes);
        return this;
    }

    public BizModel setDenyImportClasses(String denyImportClasses) {
        this.denyImportClasses = StringUtils.strToSet(denyImportClasses,
            Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public BizModel setDenyImportResources(String denyImportResources) {
        ParseUtils.parseResourceAndStem(
            StringUtils.strToSet(denyImportResources, Constants.MANIFEST_VALUE_SPLIT),
            this.denyPrefixImportResourceStems, denySuffixImportResourceStems,
            this.denyImportResources);
        return this;
    }

    public BizModel setAttribute(String key, String val) {
        attributes.put(key, val);
        return this;
    }

    public BizModel setAttributes(Map<String, String> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    public BizModel setInjectPluginDependencies(Set<String> injectPluginDependencies) {
        this.injectPluginDependencies = injectPluginDependencies;
        return this;
    }

    public BizModel setInjectExportPackages(String injectExportPackages) {
        this.injectExportPackages = StringUtils.strToSet(injectExportPackages,
            Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    public Set<String> getInjectExportPackages() {
        return injectExportPackages;
    }

    private void addStateChangeLog() {
        bizStateChangeLogs.add(new BizStateChangeInfo(new Date(), bizState));
    }

    @Override
    public String getBizName() {
        return bizName;
    }

    @Override
    public String getBizVersion() {
        return bizVersion;
    }

    @Override
    public String getIdentity() {
        return BizIdentityUtils.generateBizIdentity(this);
    }

    @Override
    public String getMainClass() {
        return mainClass;
    }

    @Override
    public URL[] getClassPath() {
        return urls;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public ClassLoader getBizClassLoader() {
        return classLoader;
    }

    @Override
    public Set<String> getDenyImportPackages() {
        return denyImportPackages;
    }

    @Override
    public Set<String> getDenyImportPackageNodes() {
        return denyImportPackageNodes;
    }

    @Override
    public Set<String> getDenyImportPackageStems() {
        return denyImportPackageStems;
    }

    @Override
    public Set<String> getDenyImportClasses() {
        return denyImportClasses;
    }

    @Override
    public Set<String> getDenyImportResources() {
        return denyImportResources;
    }

    public Set<String> getInjectPluginDependencies() {
        return injectPluginDependencies;
    }

    @Override
    public Set<String> getDenyPrefixImportResourceStems() {
        return denyPrefixImportResourceStems;
    }

    @Override
    public Set<String> getDenySuffixImportResourceStems() {
        return denySuffixImportResourceStems;
    }

    @Override
    public void start(String[] args) throws Throwable {
        doStart(args, null);
    }

    @Override
    public void start(String[] args, Map<String, String> envs) throws Throwable {
        doStart(args, envs);
    }

    private void doStart(String[] args, Map<String, String> envs) throws Throwable {
        AssertUtils.isTrue(bizState == BizState.RESOLVED, "BizState must be RESOLVED");
        if (mainClass == null) {
            throw new ArkRuntimeException(String.format("biz: %s has no main method", getBizName()));
        }
        ClassLoader oldClassLoader = ClassLoaderUtils.pushContextClassLoader(this.classLoader);
        EventAdminService eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        try {
            eventAdminService.sendEvent(new BeforeBizStartupEvent(this));
            resetProperties();
            if (!isMasterBizAndEmbedEnable()) {
                long start = System.currentTimeMillis();
                ArkLoggerFactory.getDefaultLogger().info("Ark biz {} start.", getIdentity());
                MainMethodRunner mainMethodRunner = new MainMethodRunner(mainClass, args, envs);
                mainMethodRunner.run();
                // this can trigger health checker handler
                eventAdminService.sendEvent(new AfterBizStartupEvent(this));
                ArkLoggerFactory.getDefaultLogger().info("Ark biz {} started in {} ms",
                    getIdentity(), (System.currentTimeMillis() - start));
            }
        } catch (Throwable e) {
            setBizState(BizState.BROKEN);
            eventAdminService.sendEvent(new AfterBizFailedEvent(this, e));
            throw e;
        } finally {
            ClassLoaderUtils.popContextClassLoader(oldClassLoader);
        }
        BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer().getService(
            BizManagerService.class);

        if (Boolean.getBoolean(Constants.ACTIVATE_NEW_MODULE)) {
            Biz currentActiveBiz = bizManagerService.getActiveBiz(bizName);
            if (currentActiveBiz == null) {
                setBizState(BizState.ACTIVATED);
            } else {
                ((BizModel) currentActiveBiz).setBizState(BizState.DEACTIVATED);
                setBizState(BizState.ACTIVATED);
            }
        } else {
            if (bizManagerService.getActiveBiz(bizName) == null) {
                setBizState(BizState.ACTIVATED);
            } else {
                setBizState(BizState.DEACTIVATED);
            }
        }
    }

    @Override
    public void stop() {
        AssertUtils.isTrue(bizState == BizState.ACTIVATED || bizState == BizState.DEACTIVATED
                           || bizState == BizState.BROKEN,
            "BizState must be ACTIVATED, DEACTIVATED or BROKEN.");
        if (isMasterBizAndEmbedEnable()) {
            // skip stop when embed mode
            return;
        }
        ClassLoader oldClassLoader = ClassLoaderUtils.pushContextClassLoader(this.classLoader);
        if (bizState == BizState.ACTIVATED) {
            setBizState(BizState.DEACTIVATED);
        }
        EventAdminService eventAdminService = ArkServiceContainerHolder.getContainer().getService(
            EventAdminService.class);
        try {
            // this can trigger uninstall handler
            long start = System.currentTimeMillis();
            ArkLoggerFactory.getDefaultLogger().info("Ark biz {} stops.", getIdentity());
            eventAdminService.sendEvent(new BeforeBizStopEvent(this));
            ArkLoggerFactory.getDefaultLogger().info("Ark biz {} stopped in {} ms", getIdentity(),
                (System.currentTimeMillis() - start));
        } finally {
            BizManagerService bizManagerService = ArkServiceContainerHolder.getContainer()
                .getService(BizManagerService.class);
            bizManagerService.unRegisterBiz(bizName, bizVersion);
            setBizState(BizState.UNRESOLVED);
            eventAdminService.sendEvent(new BeforeBizRecycleEvent(this));
            urls = null;
            denyImportPackages = null;
            denyImportClasses = null;
            denyImportResources = null;
            deleteQuietly(bizTempWorkDir);
            bizTempWorkDir = null;
            if (classLoader instanceof AbstractClasspathClassLoader) {
                ((AbstractClasspathClassLoader) classLoader).clearCache();
            }
            classLoader = null;
            ClassLoaderUtils.popContextClassLoader(oldClassLoader);
            eventAdminService.sendEvent(new AfterBizStopEvent(this));
        }
    }

    @Override
    public void setCustomBizName(String bizName) {
        this.bizName = bizName;
    }

    @Override
    public BizState getBizState() {
        return bizState;
    }

    @Override
    public String getWebContextPath() {
        return webContextPath;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public CopyOnWriteArrayList<BizStateChangeInfo> getBizStateChangeLogs() {
        return bizStateChangeLogs;
    }

    @Override
    public String toString() {
        String classloaderTag = classLoader == null ? "null" : classLoader.toString();
        return "Ark Biz: " + getIdentity() + ", State: " + bizState + ", classloader: "
               + classloaderTag + ", changeLogs: " + bizStateChangeLogs;
    }

    private void resetProperties() {
        if (!ArkConfigs.isEmbedEnable()) {
            System.getProperties().remove("logging.path");
        } else if (this != ArkClient.getMasterBiz()) {
            System.getProperties().remove("spring.application.admin.enabled");
        }
    }

    public File getBizTempWorkDir() {
        return bizTempWorkDir;
    }

    public BizModel setBizTempWorkDir(File bizTempWorkDir) {
        this.bizTempWorkDir = bizTempWorkDir;
        return this;
    }

    private boolean isMasterBizAndEmbedEnable() {
        return this == ArkClient.getMasterBiz() && ArkConfigs.isEmbedEnable();
    }

    public BizModel setDeclaredLibraries(String declaredLibraries) {
        if (StringUtils.isEmpty(declaredLibraries)) {
            return this;
        }
        this.declaredLibraries = StringUtils.strToSet(declaredLibraries,
            Constants.MANIFEST_VALUE_SPLIT);
        return this;
    }

    /**
     * check if the resource is defined in classloader, ignore jar version
     * @param url
     * @return
     */
    public boolean isDeclared(URL url, String resourceName) {
        // compatibility with no-declaredMode
        if (!isDeclaredMode()) {
            return true;
        }
        if (url != null) {
            String libraryFile = url.getFile().replace("file:", "");
            if (!StringUtils.isEmpty(resourceName) && libraryFile.endsWith(resourceName)) {
                libraryFile = libraryFile.substring(0, libraryFile.lastIndexOf(resourceName));
            }
            return checkDeclaredWithCache(libraryFile);
        }

        return false;
    }

    public boolean isDeclaredMode() {
        if (declaredLibraries == null || declaredLibraries.size() == 0) {
            return false;
        }
        return true;
    }

    private boolean checkDeclaredWithCache(String libraryFile) {
        // set key as jar, but need to checkDeclared by specific file.
        return declaredCacheMap.computeIfAbsent(libraryFile, this::doCheckDeclared);
    }

    boolean doCheckDeclared(String jarFilePath) {

        // if from ark plugin, then set as declared
        if (isFromPlugin(jarFilePath)) {
            return true;
        }

        String artifactId = JarUtils.parseArtifactId(jarFilePath);
        if (artifactId == null) {
            if (jarFilePath.contains(".jar!") || jarFilePath.endsWith(".jar")) {
                // if in jar, and can't get artifactId from jar file, then just rollback to all delegate.
                ArkLoggerFactory.getDefaultLogger().info(
                    String.format("Can't find artifact id for %s, default as declared.",
                        jarFilePath));
                return true;
            } else {
                // for not in jar, then default not delegate.
                ArkLoggerFactory.getDefaultLogger().info(
                    String.format("Can't find artifact id for %s, default as not declared.",
                        jarFilePath));
                return false;
            }
        }

        // some ark related lib which each ark module needed should set declared as default
        if (StringUtils.startWithToLowerCase(artifactId, "sofa-ark-")) {
            return true;
        }

        return declaredLibraries.contains(artifactId);
    }

    private boolean isFromPlugin(String jarFilePath) {
        if (pluginUrls == null) {
            return false;
        }
        for (URL pluginUrl : pluginUrls) {
            String pluginUrlFile = pluginUrl.getFile().replace("file:", "");
            if (pluginUrlFile.endsWith(JarUtils.JAR_SEPARATOR)) {
                pluginUrlFile = pluginUrlFile.substring(0, pluginUrlFile.length()
                                                           - JarUtils.JAR_SEPARATOR.length());
            }
            if (jarFilePath.contains(pluginUrlFile)) {
                return true;
            }
        }
        return false;
    }
}
