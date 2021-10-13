package com.alipay.sofa.ark.spi.ext;

public interface ExtServiceProvider {

    ExtResponse invokeService(String action, String param);

    String getType();

}
