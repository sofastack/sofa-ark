/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package com.alipay.sofa.ark.boot.mojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.LinkedHashSet;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ModuleSlimConfig.java, v 0.1 2024年07月12日 16:28 立蓬 Exp $
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ModuleSlimConfig {

    private String                 packExcludesConfig;


    private String                 packExcludesUrl;

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    private LinkedHashSet<String> excludes = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    private LinkedHashSet<String>  excludeGroupIds            = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    private LinkedHashSet<String>  excludeArtifactIds         = new LinkedHashSet<>();

    /**
     * Colon separated groupId, artifactId [and classifier] to include (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includes                   = new LinkedHashSet<>();

    /**
     * list of groupId names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includeGroupIds            = new LinkedHashSet<>();

    /**
     * list of artifact names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String>  includeArtifactIds         = new LinkedHashSet<>();

    /**
     * 基座依赖标识，以 ${groupId}:${artifactId}:${version} 标识
     */
    private String baseDependencyParentIdentity;
}