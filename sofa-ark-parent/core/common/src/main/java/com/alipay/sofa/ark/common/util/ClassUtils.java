package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.spi.constant.Constants;

/**
 * @author qilong.zql
 * @since 0.3.0
 */
public class ClassUtils {

    /**
     * Check whether a package is adapted to a pattern. e.g. the package {@literal com.alipay.sofa}
     * would be adapted to the pattern {@literal com.*} or the pattern {@literal com.alipay}, but not
     * the pattern {@literal com}.
     *
     * @param pkg
     * @param pkgPattern
     * @return
     */
    public static boolean isAdaptedToPackagePattern(String pkg, String pkgPattern) {
        if (pkgPattern.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
            return pkg.startsWith(getPackageName(pkgPattern));
        } else {
            return pkg.equals(pkgPattern);
        }
    }

    /**
     * Get package name from a specified class name.
     *
     * @param className
     * @return
     */
    public static String getPackageName(String className) {
        AssertUtils.isFalse(StringUtils.isEmpty(className), "ClassName should not be empty!");
        int index = className.lastIndexOf('.');
        if (index > 0) {
            return className.substring(0, index);
        }
        return Constants.DEFAULT_PACKAGE;
    }

}