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

import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.spi.argument.LaunchCommand;
import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.pipeline.PipelineContext;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.exception.ArkException;
import com.alipay.sofa.ark.spi.pipeline.Pipeline;
import com.alipay.sofa.ark.bootstrap.ClasspathLauncher.ClassPathArchive;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ark Container Entry
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkContainer {

    private ArkServiceContainer arkServiceContainer;

    private PipelineContext     pipelineContext;

    private AtomicBoolean       started               = new AtomicBoolean(false);

    private AtomicBoolean       stopped               = new AtomicBoolean(false);

    private long                start                 = System.currentTimeMillis();

    private static final int    MINIMUM_ARGS_SIZE     = 1;
    private static final int    ARK_COMMAND_ARG_INDEX = 0;

    public static Object main(String[] args) throws ArkException {
        if (args.length < MINIMUM_ARGS_SIZE) {
            throw new ArkException("Please provide suitable arguments to continue !");
        }

        try {
            LaunchCommand launchCommand = LaunchCommand.parse(args[ARK_COMMAND_ARG_INDEX],
                Arrays.copyOfRange(args, MINIMUM_ARGS_SIZE, args.length));

            if (launchCommand.isExecutedByCommandLine()) {
                ExecutableArkBizJar executableArchive = new ExecutableArkBizJar(new JarFileArchive(
                    new File(launchCommand.getExecutableArkBizJar().getFile())),
                    launchCommand.getExecutableArkBizJar());
                return new ArkContainer(executableArchive, launchCommand).start();
            } else {
                ClassPathArchive classPathArchive = new ClassPathArchive(
                    launchCommand.getEntryClassName(), launchCommand.getEntryMethodName(),
                    launchCommand.getEntryMethodDescriptor(), launchCommand.getClasspath());
                return new ArkContainer(classPathArchive, launchCommand).start();
            }
        } catch (IOException e) {
            throw new ArkException(String.format("SOFAArk startup failed, commandline=%s",
                LaunchCommand.toString(args)), e);
        }

    }

    public ArkContainer(ExecutableArchive executableArchive) throws Exception {
        this(executableArchive, new LaunchCommand().setExecutableArkBizJar(executableArchive
            .getUrl()));
    }

    public ArkContainer(ExecutableArchive executableArchive, LaunchCommand launchCommand) {
        arkServiceContainer = new ArkServiceContainer();
        pipelineContext = new PipelineContext();
        pipelineContext.setExecutableArchive(executableArchive);
        pipelineContext.setLaunchCommand(launchCommand);
    }

    /**
     * Start Ark Container
     *
     * @throws ArkException
     * @since 0.1.0
     */
    public Object start() throws ArkException {
        AssertUtils.assertNotNull(arkServiceContainer, "arkServiceContainer is null !");
        if (started.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    stop();
                }
            }));
            arkServiceContainer.start();

            Pipeline pipeline = arkServiceContainer.getService(Pipeline.class);
            pipeline.process(pipelineContext);

            System.out.println("Ark container started in " + (System.currentTimeMillis() - start) //NOPMD
                               + " ms.");
        }
        return this;
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
     * @throws ArkException
     */
    public void stop() throws ArkException {
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