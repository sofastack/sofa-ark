package com.alipay.sofa.ark.common.log;

import com.alipay.sofa.common.log.LoggerSpaceManager;

/**
 * LoggerFactory for SOFAArk
 *
 * @author ruoshan
 * @since 0.1.0
 */
public class ArkLoggerFactory {

    public static final String  SOFA_ARK_LOGGER_SPACE        = "com.alipay.sofa.ark";

    private static final String SOFA_ARK_DEFAULT_LOGGER_NAME = "com.alipay.sofa.ark";

    public static ArkLogger     defaultLogger                = getLogger(SOFA_ARK_DEFAULT_LOGGER_NAME);

    public static ArkLogger getLogger(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        return getLogger(clazz.getCanonicalName());
    }

    public static ArkLogger getLogger(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return new ArkLogger(LoggerSpaceManager.getLoggerBySpace(name, SOFA_ARK_LOGGER_SPACE));
    }

    public static ArkLogger getDefaultLogger() {
        return defaultLogger;
    }

}