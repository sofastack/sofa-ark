/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package com.alipay.sofa.ark.spi.service.classloader;

import com.alipay.sofa.ark.spi.service.extension.Extensible;

import java.io.FileNotFoundException;
import java.net.URL;

/**
 * @author qilong.zql
 * @since 0.6.0
 * @param <T> {@link com.alipay.sofa.ark.spi.model.Plugin} or {@link com.alipay.sofa.ark.spi.model.Biz}
 */
@Extensible
public interface ClassLoaderHook<T> {
    /**
     * @param name class name
     * @param classLoaderService {@link ClassLoaderService}
     * @param t plugin or biz instance
     * @return
     * @throws ClassNotFoundException
     */
    Class<?> hookBeforeFindClass(String name, ClassLoaderService classLoaderService, T t) throws ClassNotFoundException;

    Class<?> hookAfterFindClass(String name, ClassLoaderService classLoaderService, T t) throws ClassNotFoundException;

    URL hookBeforeFindResource(String name, ClassLoaderService classLoaderService, T t) throws FileNotFoundException;

    URL hookAfterFindResource(String name, ClassLoaderService classLoaderService, T t) throws FileNotFoundException;
}