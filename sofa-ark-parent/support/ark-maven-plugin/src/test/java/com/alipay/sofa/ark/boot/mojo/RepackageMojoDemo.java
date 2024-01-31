package com.alipay.sofa.ark.boot.mojo;

import java.util.LinkedHashSet;

/**
 * Description:
 * Author: chanyang
 * Date: 2024/1/31 6:03 下午
 */
public class RepackageMojoDemo extends RepackageMojo{
    public void extensionExcludeArtifacts(String extraResources) {
        super.extensionExcludeArtifactsInProperties(extraResources);
    }
}
