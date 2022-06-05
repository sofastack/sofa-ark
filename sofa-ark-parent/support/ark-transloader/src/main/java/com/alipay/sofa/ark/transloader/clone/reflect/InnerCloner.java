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
package com.alipay.sofa.ark.transloader.clone.reflect;

/**
 * The interface Inner cloner.
 *
 * @author hanyue
 * @version : InnerCloner.java, v 0.1 2022年06月04日 9:30 AM hanyue Exp $
 */
public interface InnerCloner {

    /**
     * Instantiate clone object.
     *
     * @param original          the original
     * @param targetClassLoader the target class loader
     * @return the object
     * @throws Exception the exception
     */
    Object instantiateClone(Object original, ClassLoader targetClassLoader) throws Exception;

    /**
     * Clone content.
     *
     * @param original          the original
     * @param clone             the clone
     * @param targetClassLoader the target class loader
     * @throws Exception the exception
     */
    void cloneContent(Object original, Object clone, ClassLoader targetClassLoader)
                                                                                   throws Exception;
}