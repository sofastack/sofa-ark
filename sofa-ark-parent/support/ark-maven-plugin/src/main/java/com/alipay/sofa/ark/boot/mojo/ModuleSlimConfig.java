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

    private String                packExcludesConfig;

    private String                packExcludesUrl;

    /**
     * Colon separated groupId, artifactId [and classifier] to exclude (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    private LinkedHashSet<String> excludes           = new LinkedHashSet<>();

    /**
     * list of groupId names to exclude (exact match).
     */
    private LinkedHashSet<String> excludeGroupIds    = new LinkedHashSet<>();

    /**
     * list of artifact names to exclude (exact match).
     */
    private LinkedHashSet<String> excludeArtifactIds = new LinkedHashSet<>();

    /**
     * Colon separated groupId, artifactId [and classifier] to include (exact match). e.g:
     * group-a:tracer-core:3.0.10
     * group-b:tracer-core:3.0.10:jdk17
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String> includes           = new LinkedHashSet<>();

    /**
     * list of groupId names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String> includeGroupIds    = new LinkedHashSet<>();

    /**
     * list of artifact names to include (exact match).
     */
    @Parameter(defaultValue = "")
    private LinkedHashSet<String> includeArtifactIds = new LinkedHashSet<>();

    /**
     * 基座依赖标识，以 ${groupId}:${artifactId}:${version} 标识
     */
    private String                baseDependencyParentIdentity;
}