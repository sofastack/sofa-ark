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
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.loader.ExecutableArkBizJar;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Base class for executable archive {@link AbstractLauncher}s.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class BaseExecutableArchiveLauncher extends AbstractLauncher {

    private final ExecutableArchive executableArchive;

    public BaseExecutableArchiveLauncher() {
        try {
            this.executableArchive = createArchive();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected BaseExecutableArchiveLauncher(ExecutableArchive executableArchive) {
        this.executableArchive = executableArchive;
    }

    @Override
    protected ExecutableArchive getExecutableArchive() {
        return this.executableArchive;
    }

    /**
     * Returns the executable file archive
     * @return executable file archive
     * @throws Exception
     */
    protected ExecutableArchive createArchive() throws Exception {
        ProtectionDomain protectionDomain = getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        URI location = (codeSource == null ? null : codeSource.getLocation().toURI());
        String path = (location == null ? null : location.getSchemeSpecificPart());
        if (path == null) {
            throw new IllegalStateException("Unable to determine code source archive");
        }
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        return root.isDirectory() ? null : new ExecutableArkBizJar(new JarFileArchive(root), root
            .toURI().toURL());
    }

}