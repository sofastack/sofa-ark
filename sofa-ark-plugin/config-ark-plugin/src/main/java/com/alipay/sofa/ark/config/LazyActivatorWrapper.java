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
package com.alipay.sofa.ark.config;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.service.PluginActivator;

/**
 * @author zsk
 * @version $Id: lazyInitActivator.java, v 0.1 2023年10月11日 17:57 zsk Exp $
 */
public class LazyActivatorWrapper<A extends PluginActivator> {

    /**
     * activator类型
     */
    private final Class<A> activatorClass;

    /**
     * 延时产生的实例
     */
    private volatile A     lazyActivator;

    public LazyActivatorWrapper(Class<A> activatorClass) {
        this.activatorClass = activatorClass;
    }

    public A getLazyActivator() {
        if (null != lazyActivator) {
            return lazyActivator;
        }

        synchronized (this) {
            if (null == lazyActivator) {
                try {
                    lazyActivator = activatorClass.newInstance();
                } catch (InstantiationException instantE) {
                    throw new ArkRuntimeException(
                        "InstantiationException in create plugin activator "
                                + activatorClass.getName(), instantE);
                } catch (IllegalAccessException illegalE) {
                    throw new ArkRuntimeException(
                        "IllegalAccessException in create plugin activator "
                                + activatorClass.getName(), illegalE);
                } catch (Throwable e) {
                    throw e;
                }
            }
        }
        return lazyActivator;
    }
}
