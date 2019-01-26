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
package com.alipay.sofa.ark.spi.service.classloader;

import com.alipay.sofa.ark.spi.service.extension.Extensible;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * implementation of ClassLoaderHook is used to customize the strategy of loading class and resources.
 *
 * @author qilong.zql
 * @since 0.6.0
 * @param <T> {@link com.alipay.sofa.ark.spi.model.Plugin} or {@link com.alipay.sofa.ark.spi.model.Biz}
 */
@Extensible
public interface ClassLoaderHook<T> {
    /**
     * invoke this method before load class using biz classLoader or plugin classLoader. If this method
     * returns null then normal loading process is done.  If this method returns a non-null value then the
     * rest of the loading process is skipped and the returned value is used.
     *
     * @param name class name
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the class found by this hook or null if normal loading process should continue
     * @throws ClassNotFoundException to terminate the hook and throw an exception
     */
    Class<?> preFindClass(String name, ClassLoaderService classLoaderService, T t)
                                                                                  throws ClassNotFoundException;

    /**
     * This method will only be called if no class was found from the normal loading process.
     *
     * @param name class name
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the class found by this hook or null if normal loading process should continue
     * @throws ClassNotFoundException to terminate the hook and throw an exception
     */
    Class<?> postFindClass(String name, ClassLoaderService classLoaderService, T t)
                                                                                   throws ClassNotFoundException;

    /**
     * invoke this method before load resource using biz classLoader or plugin classLoader. If this method
     * returns null then normal loading process is done.  If this method returns a non-null value then the
     * rest of the loading process is skipped and the returned value is used.
     *
     * @param name resource name
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the resource found by this hook or null if normal loading process should continue
     */
    URL preFindResource(String name, ClassLoaderService classLoaderService, T t);

    /**
     * This method will only be called if no resource was found from the normal loading process.
     *
     * @param name resource name
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the resource found by this hook or null if normal loading process should continue
     */
    URL postFindResource(String name, ClassLoaderService classLoaderService, T t);

    /**
     * If this method returns null then normal loading process is done.
     * If this method returns  a non-null value then the rest of the loading process is skipped and the returned
     * value is used.
     * If this method throws an <code>FileNotFoundException</code> then the loading process is terminated
     * @param name the name of the resource to find
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the resources found by this hook or empty if normal loading process should continue
     * @throws IOException throw an exception when error occurs.
     */
    Enumeration<URL> preFindResources(String name, ClassLoaderService classLoaderService, T t)
                                                                                              throws IOException;

    /**
     * This method will only be called if no resources were found from the normal loading process.
     *
     * @param name the name of the resource to find
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance where this hook is invoked
     * @return the resources found by this hook or empty if normal loading process should continue
     * @throws IOException throw an exception when error occurs.
     */
    Enumeration<URL> postFindResources(String name, ClassLoaderService classLoaderService, T t)
                                                                                               throws IOException;
}