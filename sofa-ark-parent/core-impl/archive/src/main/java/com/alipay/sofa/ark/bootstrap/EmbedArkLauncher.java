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

import com.alipay.sofa.ark.loader.archive.ExplodedArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.loader.embed.EmbedExecutableArkBizJar;
import com.alipay.sofa.ark.loader.exploded.ExplodedDirectoryArchive;
import com.alipay.sofa.ark.spi.archive.ExecutableArchive;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.File;
import java.net.URL;

import static com.alipay.sofa.ark.spi.constant.Constants.*;

public class EmbedArkLauncher extends AbstractLauncher {
    public final String             SOFA_ARK_MAIN = "com.alipay.sofa.ark.container.EmbedArkContainer";
    private final ExecutableArchive executableArchive;

    public EmbedArkLauncher() {
        try {
            this.executableArchive = createArchive();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    public EmbedArkLauncher(ExecutableArchive executableArchive) {
        this.executableArchive = executableArchive;
    }

    public static void main(String[] args) throws Exception {
        System.setProperty(Constants.CONTAINER_EMBED_ENABLE, "true");
        getOrSetDefault(CONTAINER_DIR, new File("").getAbsolutePath());
        getOrSetDefault(BIZ_CLASS_LOADER_HOOK_DIR,
            "com.alipay.sofa.ark.container.service.classloader.MasterBizClassLoaderHookAll");
        getOrSetDefault(
            BIZ_EXPORT_RESOURCES,
            "META-INF/spring.*,META-INF/services/*,META-INF/com/aipay/boot/middleware/service/config/*,org/springframework/boot/logging/*,*.xsd,*/sql-map-2.dtd,*/sql-map-config-2.dtd,*/mybatis-3-config.dtd,*/mybatis-3-mapper.dtd");
        new EmbedArkLauncher().launch(args);
    }

    private static void getOrSetDefault(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }

    @Override
    protected ExecutableArchive getExecutableArchive() throws Exception {
        return this.executableArchive;
    }

    @Override
    protected String getMainClass() throws Exception {
        return SOFA_ARK_MAIN;
    }

    protected ExecutableArchive createArchive() throws Exception {
        String path = System.getProperty(CONTAINER_DIR);
        File root = new File(path);
        if (!root.exists()) {
            throw new IllegalStateException("Unable to determine code source archive from " + root);
        }
        return root.isDirectory() ? new EmbedExecutableArkBizJar(new ExplodedArchive(root), root
            .toURI().toURL()) : new EmbedExecutableArkBizJar(new JarFileArchive(root), root.toURI()
            .toURL());
    }

    /**
     * Create a classloader for the specified URLs.
     *
     * @param urls   the URLs
     * @param parent the parent
     * @return the classloader load ark container
     */
    protected ClassLoader createContainerClassLoader(URL[] urls, ClassLoader parent) {
        return new ContainerClassLoader(urls, parent, this.getClass().getClassLoader());
    }
}
