/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.boot.mojo;

import java.util.LinkedHashSet;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ModuleSlimConfig.java, v 0.1 2024年07月12日 16:28 立蓬 Exp $
 */
public class ModuleSlimConfig {

    private String                packExcludesConfig;

    private String                packExcludesUrl;

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    private LinkedHashSet<String> excludes                                          = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    private LinkedHashSet<String> excludeGroupIds                                   = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    private LinkedHashSet<String> excludeArtifactIds                                = new LinkedHashSet<>();

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    private LinkedHashSet<String> includes                                          = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    private LinkedHashSet<String> includeGroupIds                                   = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    private LinkedHashSet<String> includeArtifactIds                                = new LinkedHashSet<>();

    /**
     * 基座依赖标识，以 ${groupId}:${artifactId}:${version} 标识
     */
    private String                baseDependencyParentIdentity;

    /**
     * 在排除依赖时，是否同时排除依赖及间接依赖。如：A依赖B，B依赖C，当 excludes 只配置了 A 时，B 和 C 都会被排除
     */
    private boolean               excludeWithIndirectDependencies                   = true;

    /**
     * 在排除依赖时，是否根据基座依赖标识（baseDependencyParentIdentity）排除与基座相同的依赖（GAV 均相同）
     */
    private boolean               excludeSameBaseDependency                         = true;

    /**
     * 在排除依赖时，如果排除的依赖与基座不一致，是否构建失败
     */
    private boolean               buildFailWhenExcludeBaseDependencyWithDiffVersion = false;

    public LinkedHashSet<String> getExcludeArtifactIds() {
        return excludeArtifactIds;
    }

    public LinkedHashSet<String> getExcludeGroupIds() {
        return excludeGroupIds;
    }

    public LinkedHashSet<String> getExcludes() {
        return excludes;
    }

    public String getPackExcludesConfig() {
        return packExcludesConfig;
    }

    public ModuleSlimConfig setPackExcludesConfig(String packExcludesConfig) {
        this.packExcludesConfig = packExcludesConfig;
        return this;
    }

    public String getPackExcludesUrl() {
        return packExcludesUrl;
    }

    public ModuleSlimConfig setPackExcludesUrl(String packExcludesUrl) {
        this.packExcludesUrl = packExcludesUrl;
        return this;
    }

    public String getBaseDependencyParentIdentity() {
        return baseDependencyParentIdentity;
    }

    public ModuleSlimConfig setBaseDependencyParentIdentity(String baseDependencyParentIdentity) {
        this.baseDependencyParentIdentity = baseDependencyParentIdentity;
        return this;
    }

    public ModuleSlimConfig setExcludes(LinkedHashSet<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    public ModuleSlimConfig setExcludeGroupIds(LinkedHashSet<String> excludeGroupIds) {
        this.excludeGroupIds = excludeGroupIds;
        return this;
    }

    public ModuleSlimConfig setExcludeArtifactIds(LinkedHashSet<String> excludeArtifactIds) {
        this.excludeArtifactIds = excludeArtifactIds;
        return this;
    }

    public LinkedHashSet<String> getIncludes() {
        return includes;
    }

    public ModuleSlimConfig setIncludes(LinkedHashSet<String> includes) {
        this.includes = includes;
        return this;
    }

    public LinkedHashSet<String> getIncludeGroupIds() {
        return includeGroupIds;
    }

    public ModuleSlimConfig setIncludeGroupIds(LinkedHashSet<String> includeGroupIds) {
        this.includeGroupIds = includeGroupIds;
        return this;
    }

    public LinkedHashSet<String> getIncludeArtifactIds() {
        return includeArtifactIds;
    }

    public ModuleSlimConfig setIncludeArtifactIds(LinkedHashSet<String> includeArtifactIds) {
        this.includeArtifactIds = includeArtifactIds;
        return this;
    }

    public boolean isExcludeWithIndirectDependencies() {
        return excludeWithIndirectDependencies;
    }

    public void setExcludeWithIndirectDependencies(boolean excludeWithIndirectDependencies) {
        this.excludeWithIndirectDependencies = excludeWithIndirectDependencies;
    }

    public boolean isExcludeSameBaseDependency() {
        return excludeSameBaseDependency;
    }

    public void setExcludeSameBaseDependency(boolean excludeSameBaseDependency) {
        this.excludeSameBaseDependency = excludeSameBaseDependency;
    }

    public boolean isBuildFailWhenExcludeBaseDependencyWithDiffVersion() {
        return buildFailWhenExcludeBaseDependencyWithDiffVersion;
    }

    public void setBuildFailWhenExcludeBaseDependencyWithDiffVersion(boolean buildFailWhenExcludeBaseDependencyWithDiffVersion) {
        this.buildFailWhenExcludeBaseDependencyWithDiffVersion = buildFailWhenExcludeBaseDependencyWithDiffVersion;
    }
}