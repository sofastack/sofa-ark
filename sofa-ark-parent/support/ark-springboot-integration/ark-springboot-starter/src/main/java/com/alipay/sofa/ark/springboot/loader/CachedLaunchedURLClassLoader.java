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

import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cached LaunchedURLClassLoader to accelerate load classes and resources
 *
 * @author bingjie.lbj
 */
public class CachedLaunchedURLClassLoader extends LaunchedURLClassLoader {
    private final Map<String, LoadClassResult>            classCache        = new ConcurrentHashMap<>(
            3000);
    private final Map<String, Optional<URL>>              resourceUrlCache  = new ConcurrentHashMap<>(
            3000);
    private final Map<String, Optional<Enumeration<URL>>> resourcesUrlCache = new ConcurrentHashMap<>(
            300);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public CachedLaunchedURLClassLoader(boolean exploded, Archive rootArchive, URL[] urls,
                                        ClassLoader parent) {
        super(exploded, rootArchive, urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        return loadClassWithCache(name, resolve);
    }

    @Override
    public URL findResource(String name) {
        Optional<URL> urlOptional = resourceUrlCache.get(name);
        if (urlOptional != null) {
            return urlOptional.orElse(null);
        }
        URL url = super.findResource(name);
        resourceUrlCache.put(name, url != null ? Optional.of(url) : Optional.empty());
        return url;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Optional<Enumeration<URL>> urlOptional = resourcesUrlCache.get(name);
        if (urlOptional != null) {
            return urlOptional.orElse(null);
        }
        Enumeration<URL> enumeration = super.findResources(name);
        if (enumeration == null || !enumeration.hasMoreElements()) {
            resourcesUrlCache.put(name, Optional.empty());
        }
        return enumeration;
    }

    protected Class<?> loadClassWithCache(String name, boolean resolve)
            throws ClassNotFoundException {
        LoadClassResult resultInCache = classCache.get(name);
        if (resultInCache != null) {
            if (resultInCache.getEx() != null) {
                throw resultInCache.getEx();
            }
            return resultInCache.getClazz();
        }
        try {
            Class<?> clazz = super.findLoadedClass(name);
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);
            }
            if(clazz == null) {
                classCache.put(name, LoadClassResult.NOT_FOUND);
            }
            return clazz;
        } catch (ClassNotFoundException exception) {
            classCache.put(name, new LoadClassResult(exception));
            throw exception;
        }
    }

    protected static class LoadClassResult {
        private Class<?>               clazz;
        private ClassNotFoundException ex;
        protected static LoadClassResult NOT_FOUND = new LoadClassResult();

        public LoadClassResult() {
        }

        public LoadClassResult(ClassNotFoundException ex) {
            this.ex = ex;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public ClassNotFoundException getEx() {
            return ex;
        }

        public void setEx(ClassNotFoundException ex) {
            this.ex = ex;
        }
    }
}
