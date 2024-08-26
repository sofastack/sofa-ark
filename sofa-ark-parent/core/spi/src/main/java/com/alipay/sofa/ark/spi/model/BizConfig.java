package com.alipay.sofa.ark.spi.model;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class BizConfig {

    /**
     * Biz部署参数指定version
     */
    private String specifiedVersion;

    /**
     * Biz扩展classpath
     */
    private URL[] extensionUrls;

    /**
     * Biz依赖的plugins
     */
    private List<String> dependentPlugins;


    String[] args;

    Map<String, String> envs;



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

    public List<String> getDependentPlugins() {
        return dependentPlugins;
    }

    public void setDependentPlugins(List<String> dependentPlugins) {
        this.dependentPlugins = dependentPlugins;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public Map<String, String> getEnvs() {
        return envs;
    }

    public void setEnvs(Map<String, String> envs) {
        this.envs = envs;
    }
}
