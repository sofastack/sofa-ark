package com.alipay.sofa.ark.common.util;

import com.alipay.sofa.ark.spi.constant.Constants;

public class ArkConfigUtils {
    public static boolean isEmbedEnable() {
        return Boolean.getBoolean(Constants.EMBED_ENABLE);
    }

    public static void setEmbedEnable(boolean enable) {
        System.setProperty(Constants.EMBED_ENABLE, enable ? "true" : "false");
    }
}
