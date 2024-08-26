package com.alipay.sofa.ark.spi.model;

import java.net.URL;

public class PluginConfig {

    /**
     * Plugin部署参数指定name
     */
    private String specifiedName;

    /**
     * Plugin部署参数指定version
     */
    private String specifiedVersion;

    /**
     * Plugin扩展classpath
     */
    private URL[] extensionUrls;

    public String getSpecifiedName() {
        return specifiedName;
    }

    public void setSpecifiedName(String specifiedName) {
        this.specifiedName = specifiedName;
    }

    public String getSpecifiedVersion() {
        return specifiedVersion;
    }

    public void setSpecifiedVersion(String specifiedVersion) {
        this.specifiedVersion = specifiedVersion;
    }

    public URL[] getExtensionUrls() {
        return extensionUrls;
    }

    public void setExtensionUrls(URL[] extensionUrls) {
        this.extensionUrls = extensionUrls;
    }
}
