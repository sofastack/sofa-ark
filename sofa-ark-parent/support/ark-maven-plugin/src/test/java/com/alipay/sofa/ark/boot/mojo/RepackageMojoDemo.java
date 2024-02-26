package com.alipay.sofa.ark.boot.mojo;

/**
 * Description:
 * Author: chanyang
 * Date: 2024/2/26 8:33 下午
 */
public class RepackageMojoDemo extends RepackageMojo{
    public void extensionExcludeArtifacts(String extraResources) {
        super.extensionExcludeArtifactsInProperties(extraResources);
    }
}
