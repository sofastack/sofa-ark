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
package com.alipay.sofa.ark.web.embed.tomcat;

import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import org.apache.catalina.loader.ParallelWebappClassLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Extension of Tomcat's {@link ParallelWebappClassLoader} that does not consider the
 * {@link ClassLoader#getSystemClassLoader() system classloader}. This is required to
 * ensure that any custom context class loader is always used (as is the case with some
 * executable archives).
 *
 * There are two case to initialize tomcat for multi biz model:
 *
 * 1. When included this Tomcat ClassLoader by
 * {@link com.alipay.sofa.ark.web.embed.tomcat.ArkTomcatEmbeddedWebappClassLoader},
 * this will ensure there should always be one tomcat instance with only one port.
 * In this case, each module use same web port, must define different web context path.
 *
 *   --------------
 *  │ Biz Module A │\
 *   --------------  \
 *                    \----→ Tomcat 1: Port 1
 *   --------------   /
 *  │ Biz Module B │/
 *   --------------
 *
 * 2. If not included, then each biz module with mvc will create a tomcat,
 * In this case, each module can define any web context path, but must define different web server port.
 *
 *   --------------
 *  │ Biz Module A │ ----→ Tomcat 1: Port 1
 *   --------------
 *
 *   --------------
 *  │ Biz Module B │ ----→ Tomcat 2: Port2
 *   --------------
 *
 * Actually, the mostly used in prod env is the first case, so we need to include a web plugin.
 *
 * @author qilong.zql
 * @author Phillip Webb
 * @since 0.6.0
 */
public class ArkTomcatEmbeddedWebappClassLoader extends ParallelWebappClassLoader {
    private static final Logger LOGGER = ArkLoggerFactory
                                           .getLogger(ArkTomcatEmbeddedWebappClassLoader.class);

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public ArkTomcatEmbeddedWebappClassLoader() {
    }

    /**
     * NOTE: super web class loader will set parent to systemClassLoader if 'parent' param value is null.
     * So 'parent' class loader usually would not be null.
     *
     * @param parent
     */
    public ArkTomcatEmbeddedWebappClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public URL findResource(String name) {
        return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        return Collections.emptyEnumeration();
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> result = findExistingLoadedClass(name);
            result = (result != null) ? result : doLoadClass(name);
            if (result == null) {
                throw new ClassNotFoundException(name);
            }
            return resolveIfNecessary(result, resolve);
        }
    }

    /**
     * We try to load class from cache in current class loader chain.
     *
     * @param name
     * @return
     */
    private Class<?> findExistingLoadedClass(String name) {
        Class<?> resultClass = findLoadedClass0(name);
        resultClass = (resultClass != null) ? resultClass : findLoadedClass(name);
        return resultClass;
    }

    /**
     * doLoadClass is used to handle class not found from cache, so doLoadClass try to load class from class loader chain and put it into class cache.
     *
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    private Class<?> doLoadClass(String name) throws ClassNotFoundException {
        checkPackageAccess(name);
        // Note: set delegate TRUE will load class from parent class loader first, otherwise from current class loader first.
        // If class is javax or apache class, filter will return true, otherwise filter return false.
        if ((this.delegate || filter(name, true))) {
            Class<?> result = loadFromParent(name);
            return (result != null) ? result : findClassIgnoringNotFound(name);
        }
        Class<?> result = findClassIgnoringNotFound(name);
        return (result != null) ? result : loadFromParent(name);
    }

    private Class<?> resolveIfNecessary(Class<?> resultClass, boolean resolve) {
        if (resolve) {
            resolveClass(resultClass);
        }
        return (resultClass);
    }

    @Override
    protected void addURL(URL url) {
        // Ignore URLs added by the Tomcat 8 implementation (see gh-919)
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Ignoring request to add " + url + " to the tomcat classloader");
        }
    }

    private Class<?> loadFromParent(String name) {
        if (this.parent == null) {
            return null;
        }
        try {
            return Class.forName(name, false, this.parent);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     * Invoke findClass in tomcat web class loader.
     * Note: After Tomcat stopped, Tomcat will construct a IllegalStateException, and then put it into a ClassNotFoundException.
     * SOFAArk will always treat this inner IllegalStateException as a ClassNotFoundException and return null,
     * which means this Class was not found and then find this class through parent class loader (e.g. ApplicationClassLoader).
     *
     * @param name
     * @return
     */
    private Class<?> findClassIgnoringNotFound(String name) {
        try {
            return findClass(name);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    void checkPackageAccess(String name) throws ClassNotFoundException {
        if (this.securityManager != null && name.lastIndexOf('.') >= 0) {
            try {
                this.securityManager.checkPackageAccess(name.substring(0, name.lastIndexOf('.')));
            } catch (SecurityException ex) {
                throw new ClassNotFoundException("Security Violation, attempt to use "
                                                 + "Restricted Class: " + name, ex);
            }
        }
    }
}
