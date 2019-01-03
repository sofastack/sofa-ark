package com.alipay.sofa.ark.common.util;

import java.util.Properties;

/**
 * a utils class to get environment properties
 *
 * @author qilong.zql
 * @since 0.4.0
 */
public class EnvironmentUtils {

    private static Properties properties = new Properties();

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            return System.getProperty(key);
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return System.getProperty(key, defaultValue);
        }
        return value;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    public static void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }

    public static void clearProperty(String key) {
        System.clearProperty(key);
    }

}