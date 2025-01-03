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
package com.alipay.sofa.ark.container;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.bootstrap.ClasspathLauncher.ClassPathArchive;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.container.pipeline.DeployBizStage;
import com.alipay.sofa.ark.container.pipeline.HandleArchiveStage;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.EmbedClassPathArchive;
import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.ExplodedArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.BizArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.argument.LaunchCommand;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.pipeline.Pipeline;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.spi.service.biz.AddBizToStaticDeployHook;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.common.log.MultiAppLoggerSpaceManager;
import com.alipay.sofa.common.log.SpaceId;
import com.alipay.sofa.common.log.SpaceInfo;
import com.alipay.sofa.common.log.env.LogEnvUtils;
import com.alipay.sofa.common.log.factory.LogbackLoggerSpaceFactory;
import com.alipay.sofa.common.utils.ReportUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_FILE;
import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_FILE_FORMAT;
import static com.alipay.sofa.common.log.Constants.LOGGING_PATH_DEFAULT;
import static com.alipay.sofa.common.log.Constants.LOG_ENCODING_PROP_KEY;
import static com.alipay.sofa.common.log.Constants.LOG_PATH;
import static com.alipay.sofa.common.log.Constants.UTF8_STR;

/**
 * Ark Container Entry
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkContainer {

    private ArkServiceContainer            arkServiceContainer;

    private PipelineContext                pipelineContext;

    private AtomicBoolean                  started           = new AtomicBoolean(false);

    private AtomicBoolean                  stopped           = new AtomicBoolean(false);

    private long                           start             = System.currentTimeMillis();

    private List<AddBizToStaticDeployHook> addBizToStaticDeployHooks;

    /**
     * -Aclasspath or -Ajar is needed at lease. it specify the abstract executable ark archive,
     * default added by container itself
     */
    private static final int               MINIMUM_ARGS_SIZE = 1;

    public static Object main(String[] args) throws ArkRuntimeException {
        if (args.length < MINIMUM_ARGS_SIZE) {
            throw new ArkRuntimeException("Please provide suitable arguments to continue !");
        }

        try {
            LaunchCommand launchCommand = LaunchCommand.parse(args);
            if (launchCommand.isExecutedByCommandLine()) {
                ExecutableArkBizJar executableArchive;
                File rootFile = FileUtils.file(launchCommand.getExecutableArkBizJar().getFile());
                if (rootFile.isDirectory()) {
                    executableArchive = new ExecutableArkBizJar(new ExplodedArchive(rootFile));
                } else {
                    executableArchive = new ExecutableArkBizJar(new JarFileArchive(rootFile,
                        launchCommand.getExecutableArkBizJar()));
                }
                return new ArkContainer(executableArchive, launchCommand).start();
            } else {
                ClassPathArchive classPathArchive;
                if (ArkConfigs.isEmbedEnable()) {
                    classPathArchive = new EmbedClassPathArchive(launchCommand.getEntryClassName(),
                        launchCommand.getEntryMethodName(), launchCommand.getClasspath());
                } else {
                    classPathArchive = new ClassPathArchive(launchCommand.getEntryClassName(),
                        launchCommand.getEntryMethodName(), launchCommand.getClasspath());
                }
                return new ArkContainer(classPathArchive, launchCommand).start();
            }
        } catch (IOException e) {
            throw new ArkRuntimeException(String.format("SOFAArk startup failed, commandline=%s",
                LaunchCommand.toString(args)), e);
        }
    }

    public ArkContainer(ExecutableArchive executableArchive) throws Exception {
        this(executableArchive, new LaunchCommand().setExecutableArkBizJar(executableArchive
            .getUrl()));
    }

    public ArkContainer(ExecutableArchive executableArchive, LaunchCommand launchCommand) {
        arkServiceContainer = new ArkServiceContainer(launchCommand.getLaunchArgs());
        pipelineContext = new PipelineContext();
        pipelineContext.setExecutableArchive(executableArchive);
        pipelineContext.setLaunchCommand(launchCommand);
    }

    /**
     * Start Ark Container
     *
     * @throws ArkRuntimeException
     * @since 0.1.0
     */
    public Object start() throws ArkRuntimeException {
        AssertUtils.assertNotNull(arkServiceContainer, "arkServiceContainer is null !");
        if (started.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }));
            prepareArkConfig();
            // don't remove this log print, add to init log space first before initialize ArkLogger
            ArkLoggerFactory.getDefaultLogger().info("Ark container starting...");
            reInitializeArkLogger();
            arkServiceContainer.start();
            Pipeline pipeline = arkServiceContainer.getService(Pipeline.class);
            pipeline.process(pipelineContext);

            System.out.println("Ark container started in " + (System.currentTimeMillis() - start) //NOPMD
                               + " ms.");
        }
        return this;
    }

    public Object deployBizAfterMasterBizReady() throws Exception {
        // Scan all biz in classpath
        HandleArchiveStage handleArchiveStage = ArkServiceContainerHolder.getContainer()
            .getService(HandleArchiveStage.class);
        handleArchiveStage.processStaticBizFromClasspath(pipelineContext);

        // execute beforeEmbedStaticDeployBizHook
        addStaticBizFromCustomHooks();

        // start up
        DeployBizStage deployBizStage = ArkServiceContainerHolder.getContainer().getService(
            DeployBizStage.class);
        deployBizStage.processStaticBiz(pipelineContext);
        return this;
    }

    private void addStaticBizFromCustomHooks() throws Exception {
        addBizToStaticDeployHooks = ArkServiceLoader.loadExtensionsFromArkBiz(
            AddBizToStaticDeployHook.class, ArkClient.getMasterBiz().getIdentity());
        for (AddBizToStaticDeployHook hook : addBizToStaticDeployHooks) {
            List<BizArchive> bizsFromHook = hook.getStaticBizToAdd();
            addStaticBiz(bizsFromHook);
        }
    }

    private void addStaticBiz(List<BizArchive> bizArchives) throws IOException {
        if (null == bizArchives) {
            return;
        }

        for (BizArchive bizArchive : bizArchives) {
            Biz biz = ArkClient.getBizFactoryService().createBiz(bizArchive);
            ArkClient.getBizManagerService().registerBiz(biz);
        }
    }

    /**
     * Prepare to read ark conf
     * @throws ArkRuntimeException
     */
    public void prepareArkConfig() throws ArkRuntimeException {
        try {
            // Forbid to Monitoring and Management Using JMX, because it leads to conflict when setup multi spring boot app.
            ArkConfigs.setSystemProperty(Constants.SPRING_BOOT_ENDPOINTS_JMX_ENABLED,
                String.valueOf(false));
            // ignore thread class loader when loading classes and resource in log4j
            ArkConfigs.setSystemProperty(Constants.LOG4J_IGNORE_TCL, String.valueOf(true));

            // compatible sofa-hessian4, refer to https://github.com/sofastack/sofa-hessian/issues/38
            ArkConfigs.setSystemProperty(Constants.RESOLVE_PARENT_CONTEXT_SERIALIZER_FACTORY,
                "false");

            // read ark conf file
            List<URL> urls = getProfileConfFiles(pipelineContext.getLaunchCommand().getProfiles());
            ArkConfigs.init(urls);
        } catch (Throwable throwable) {
            throw new ArkRuntimeException(throwable);
        }
    }

    public List<URL> getProfileConfFiles(String... profiles) {
        List<URL> urls = new ArrayList<>();
        for (String profile : profiles) {
            URL url;
            if (StringUtils.isEmpty(profile)) {
                url = this.getClass().getClassLoader().getResource(ARK_CONF_FILE);
            } else {
                url = this.getClass().getClassLoader()
                    .getResource(String.format(ARK_CONF_FILE_FORMAT, profile));
            }
            if (url != null) {
                urls.add(url);
            } else if (!StringUtils.isEmpty(profile)) {
                ReportUtil.reportWarn(String.format("The %s conf file is not found.", profile));
            }
        }
        return urls;
    }

    /**
     * reInitialize Ark Logger
     *
     * @throws ArkRuntimeException
     */
    public void reInitializeArkLogger() throws ArkRuntimeException {
        for (Map.Entry<SpaceId, SpaceInfo> entry : ((Map<SpaceId, SpaceInfo>) MultiAppLoggerSpaceManager
            .getSpacesMap()).entrySet()) {
            SpaceId spaceId = entry.getKey();
            SpaceInfo spaceInfo = entry.getValue();
            if (!ArkLoggerFactory.SOFA_ARK_LOGGER_SPACE.equals(spaceId.getSpaceName())) {
                continue;
            }
            LogbackLoggerSpaceFactory arkLoggerSpaceFactory = (LogbackLoggerSpaceFactory) spaceInfo
                .getAbstractLoggerSpaceFactory();
            Map<String, String> arkLogConfig = new HashMap<>();
            // set base logging.path
            arkLogConfig.put(LOG_PATH, ArkConfigs.getStringValue(LOG_PATH, LOGGING_PATH_DEFAULT));
            // set log file encoding
            arkLogConfig.put(LOG_ENCODING_PROP_KEY,
                ArkConfigs.getStringValue(LOG_ENCODING_PROP_KEY, UTF8_STR));
            // set other log config
            for (String key : ArkConfigs.keySet()) {
                if (LogEnvUtils.filterAllLogConfig(key)) {
                    arkLogConfig.put(key, ArkConfigs.getStringValue(key));
                }
            }
            arkLoggerSpaceFactory.reInitialize(arkLogConfig);
        }
    }

    /**
     * Whether Ark Container is started or not
     *
     * @return
     */
    public boolean isStarted() {
        return started.get();
    }

    /**
     * Stop Ark Container
     *
     * @throws ArkRuntimeException
     */
    public void stop() throws ArkRuntimeException {
        AssertUtils.assertNotNull(arkServiceContainer, "arkServiceContainer is null !");
        if (stopped.compareAndSet(false, true)) {
            arkServiceContainer.stop();
        }
    }

    /**
     * Whether Ark Container is running or not
     * @return
     */
    public boolean isRunning() {
        return isStarted() && !stopped.get();
    }

    /**
     * Get {@link ArkServiceContainer} of ark container
     *
     * @return
     */
    public ArkServiceContainer getArkServiceContainer() {
        return arkServiceContainer;
    }

    /**
     * Get {@link PipelineContext} of ark container
     *
     * @return
     */
    public PipelineContext getPipelineContext() {
        return pipelineContext;
    }
}
