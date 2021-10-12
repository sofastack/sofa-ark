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

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.ExplodedArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.embed.EmbedExecutableArkBizJar;
import com.alipay.sofa.ark.loader.exploded.ExplodedDirectoryArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.argument.LaunchCommand;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;

public class EmbedArkContainer extends ArkContainer {
    /**
     * -Aclasspath or -Ajar is needed at lease. it specify the abstract executable ark archive,
     * default added by container itself
     */
    private static final int MINIMUM_ARGS_SIZE = 1;

    public EmbedArkContainer(ExecutableArchive executableArchive) throws Exception {
        super(executableArchive);
    }

    public EmbedArkContainer(ExecutableArchive executableArchive, LaunchCommand launchCommand) {
        super(executableArchive, launchCommand);
    }

    public static Object main(String[] args) throws ArkRuntimeException {
        if (args.length < MINIMUM_ARGS_SIZE) {
            throw new ArkRuntimeException("Please provide suitable arguments to continue !");
        }

        try {
            LaunchCommand launchCommand = LaunchCommand.parse(args);
            ExecutableArkBizJar executableArchive;
            File rootFile = new File(URLDecoder.decode(launchCommand.getExecutableArkBizJar()
                .getFile()));
            if (rootFile.isDirectory()) {
                executableArchive = new EmbedExecutableArkBizJar(new ExplodedArchive(rootFile),
                    rootFile.toURI().toURL());
            } else {
                executableArchive = new EmbedExecutableArkBizJar(new JarFileArchive(rootFile,
                    launchCommand.getExecutableArkBizJar()), rootFile.toURI().toURL());
            }
            return new EmbedArkContainer(executableArchive, launchCommand).start();
        } catch (IOException e) {
            throw new ArkRuntimeException(String.format("SOFAArk startup failed, commandline=%s",
                LaunchCommand.toString(args)), e);
        }
    }
}
