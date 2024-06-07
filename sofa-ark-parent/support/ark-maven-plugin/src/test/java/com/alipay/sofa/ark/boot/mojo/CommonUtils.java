/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.ark.boot.mojo;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: CommonUtils.java, v 0.1 2024年06月07日 15:33 立蓬 Exp $
 */
public class CommonUtils {

    public static File getResourceFile(String resourcePath) throws URISyntaxException {
        URL url = CommonUtils.class.getClassLoader().getResource(resourcePath);
        return new File(url.toURI());
    }

    public static boolean resourceExists(String resourcePath){
        return null != CommonUtils.class.getClassLoader().getResource(resourcePath);
    }
}