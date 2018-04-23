package com.alipay.sofa.ark.spi.tools;


import java.net.URL;
import java.net.URLClassLoader;

/**
 * 运行时工具
 *
 * @author joe
 * @version 2018.04.23 10:46
 */
public class RuntimeUtil {
    /**
     * 获取classpath
     *
     * @param classLoader ClassLoader
     * @return ClassLoader对应的classpath
     */
    public static URL[] getClasspath(ClassLoader classLoader) {
        if (classLoader == null) {
            return new URL[0];
        }
        if (classLoader instanceof URLClassLoader) {
            return ((URLClassLoader) classLoader).getURLs();
        } else {
            return getClasspath(classLoader.getParent());
        }
    }
}

