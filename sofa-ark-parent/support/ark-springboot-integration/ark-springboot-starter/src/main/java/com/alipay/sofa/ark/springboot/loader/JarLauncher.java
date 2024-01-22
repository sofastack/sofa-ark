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
package com.alipay.sofa.ark.springboot.loader;

import java.net.URL;
import java.util.Collection;

/**
 * A JarLauncher to load classes with CachedLaunchedURLClassLoader
 *
 * @author bingjie.lbj
 */
public class JarLauncher extends org.springframework.boot.loader.launch.JarLauncher {
    public JarLauncher() throws Exception {
    }

    public static void main(String[] args) throws Exception {
        new JarLauncher().launch(args);
    }

    @Override
    protected ClassLoader createClassLoader(Collection<URL> urls) throws Exception {
        return new CachedLaunchedURLClassLoader(isExploded(), getArchive(),
            urls.toArray(new URL[0]), getClass().getClassLoader());
    }
}
