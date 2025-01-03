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

import com.alipay.sofa.ark.boot.mojo.model.ArkConfigHolder;
import com.alipay.sofa.ark.common.util.ParseUtils;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.tools.ArtifactItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.graph.DependencyNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alipay.sofa.ark.boot.mojo.MavenUtils.getArtifactIdentity;
import static com.alipay.sofa.ark.boot.mojo.MavenUtils.getArtifactIdentityWithoutVersion;
import static com.alipay.sofa.ark.boot.mojo.MavenUtils.getDependencyIdentity;
import static com.alipay.sofa.ark.boot.mojo.MavenUtils.getGAVIdentity;
import static com.alipay.sofa.ark.boot.mojo.MavenUtils.inUnLogScopes;
import static com.alipay.sofa.ark.boot.mojo.utils.ParseUtils.getBooleanWithDefault;
import static com.alipay.sofa.ark.boot.mojo.utils.ParseUtils.getStringSet;
import static com.alipay.sofa.ark.spi.constant.Constants.ARK_CONF_BASE_DIR;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES_ARTIFACTIDS;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_EXCLUDES_GROUPIDS;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_INCLUDES;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_INCLUDES_ARTIFACTIDS;
import static com.alipay.sofa.ark.spi.constant.Constants.EXTENSION_INCLUDES_GROUPIDS;
import static com.alipay.sofa.ark.spi.constant.Constants.STRING_COLON;

/**
 * @author lianglipeng.llp@alibaba-inc.com
 * @version $Id: ModuleSlimStrategy.java, v 0.1 2024年07月12日 16:21 立蓬 Exp $
 */

public class ModuleSlimStrategy {
    private MavenProject        project;

    private DependencyNode      projDependencyGraph;
    private ModuleSlimConfig    config;

    private Log                 log;

    private File                baseDir;

    private static final String EXTENSION_EXCLUDE_WITH_INDIRECT_DEPENDENCIES           = "excludeWithIndirectDependencies";

    private static final String EXTENSION_EXCLUDE_SAME_BASE_DEPENDENCY                 = "excludeSameBaseDependency";

    private static final String EXTENSION_BUILD_FAIL_WHEN_EXCLUDE_DIFF_BASE_DEPENDENCY = "buildFailWhenExcludeDiffBaseDependency";

    private static final String DEFAULT_EXCLUDE_RULES                                  = "rules.txt";

    ModuleSlimStrategy(MavenProject project, DependencyNode projDependencyGraph,
                       ModuleSlimConfig config, File baseDir, Log log) {
        this.project = project;
        this.projDependencyGraph = projDependencyGraph;
        this.config = config;
        this.baseDir = baseDir;
        this.log = log;
    }

    public Set<Artifact> getSlimmedArtifacts() throws MojoExecutionException, IOException {
        initSlimStrategyConfig();

        Set<Artifact> toFilterByBase = getArtifactsToFilterByParentIdentity(project.getArtifacts());
        Set<Artifact> toFilterByExclude = getArtifactsToFilterByExcludeConfig(project
            .getArtifacts());
        Set<Artifact> toAddByInclude = getArtifactsToAddByIncludeConfig(project.getArtifacts());

        checkExcludeByParentIdentity(toFilterByExclude, toAddByInclude);

        Set<Artifact> filteredArtifacts = new HashSet<>(project.getArtifacts());
        filteredArtifacts.removeAll(toFilterByBase);
        filteredArtifacts.removeAll(toFilterByExclude);
        filteredArtifacts.addAll(toAddByInclude);
        return filteredArtifacts;
    }

    protected void checkExcludeByParentIdentity(Set<Artifact> toFilterByExclude, Set<Artifact> toAddByInclude) throws MojoExecutionException {
        if (StringUtils.isEmpty(config.getBaseDependencyParentIdentity())) {
            return;
        }

        Set<Artifact> toFilter = new HashSet<>(toFilterByExclude);
        toFilter.removeAll(toAddByInclude);

        Set<Artifact> excludedButNoDependencyInBase = getExcludedButNoDependencyInBase(toFilter);
        Set<Artifact> excludedButDifferentVersionDependencyInBase = getExcludedButDifferentVersionDependencyInBase(toFilter);

        if(excludedButNoDependencyInBase.isEmpty() && excludedButDifferentVersionDependencyInBase.isEmpty()){
            getLog().info(String.format("check excludeWithBaseDependencyParentIdentity success with base: %s",config.getBaseDependencyParentIdentity()));
            return;
        }

        // Dependency not found in base; please add it to the base or do not exclude it in the module
        excludedButNoDependencyInBase.forEach(artifact -> {
            getLog().error(
                    String.format(
                            "error to exclude package jar: %s because no such jar in base, please keep the jar or add it to base",
                            getArtifactIdentity(artifact)));
        });

        if(!excludedButNoDependencyInBase.isEmpty()){
            throw new MojoExecutionException(String.format("check excludeWithBaseDependencyParentIdentity failed with base: %s",config.getBaseDependencyParentIdentity()));
        }

        // The base contains this dependency, but the version and module are inconsistent; Please use the same dependency version as the base in the module.
        List<Dependency> baseDependencies = getAllBaseDependencies();
        Map<String,Dependency> baseDependencyIdentityWithoutVersion = baseDependencies.stream().collect(Collectors.toMap(MavenUtils::getDependencyIdentityWithoutVersion, it -> it));
        excludedButDifferentVersionDependencyInBase.forEach(artifact -> {
            Dependency baseDependency = baseDependencyIdentityWithoutVersion.get(getArtifactIdentityWithoutVersion(artifact));
            getLog().error(
                    String.format(
                            "error to exclude package jar: %s because it has different version with: %s in base, please keep the jar or set same version with base",
                            getArtifactIdentity(artifact), getDependencyIdentity(baseDependency)));
        });

        if(config.isBuildFailWhenExcludeBaseDependencyWithDiffVersion()){
            throw new MojoExecutionException(String.format("check excludeWithBaseDependencyParentIdentity failed with base: %s",config.getBaseDependencyParentIdentity()));
        }
    }

    protected Set<Artifact> getArtifactsToFilterByParentIdentity(Set<Artifact> artifacts)
                                                                                         throws MojoExecutionException {
        if (StringUtils.isEmpty(config.getBaseDependencyParentIdentity())) {
            return Collections.emptySet();
        }

        if (!config.isExcludeSameBaseDependency()) {
            return Collections.emptySet();
        }

        // 过滤出模块和基座版本一致的依赖，即需要瘦身的依赖
        return getSameVersionArtifactsWithBase(artifacts);
    }

    private Set<Artifact> getSameVersionArtifactsWithBase(Set<Artifact> artifacts) throws MojoExecutionException {
        List<Dependency> baseDependencies = getAllBaseDependencies();

        Set<String> dependencyIdentities = baseDependencies.stream().map(MavenUtils::getDependencyIdentity).collect(Collectors.toSet());

        return artifacts.stream().filter(it -> dependencyIdentities.contains(getArtifactIdentity(it))).collect(Collectors.toSet());
    }

    private Set<Artifact> getExcludedButNoDependencyInBase(Set<Artifact> toFilter) throws MojoExecutionException {
        List<Dependency> baseDependencies = getAllBaseDependencies();

        Map<String,Dependency> baseDependencyIdentityWithoutVersion = baseDependencies.stream().collect(Collectors.toMap(MavenUtils::getDependencyIdentityWithoutVersion, it -> it));

        return toFilter.stream().filter(it -> !baseDependencyIdentityWithoutVersion.containsKey(getArtifactIdentityWithoutVersion(it))).collect(Collectors.toSet());
    }

    private Set<Artifact> getExcludedButDifferentVersionDependencyInBase(Set<Artifact> toFilter) throws MojoExecutionException {
        List<Dependency> baseDependencies = getAllBaseDependencies();
        Map<String,Dependency> baseDependencyIdentityWithoutVersion = baseDependencies.stream().collect(Collectors.toMap(MavenUtils::getDependencyIdentityWithoutVersion, it -> it));
        return toFilter.stream().filter(artifact ->
                {
                    String identityWithoutVersion = getArtifactIdentityWithoutVersion(artifact);
                    return baseDependencyIdentityWithoutVersion.containsKey(identityWithoutVersion)
                            && (!artifact.getBaseVersion().equals(baseDependencyIdentityWithoutVersion.get(identityWithoutVersion).getVersion()));
                }
        ).collect(Collectors.toSet());
    }

    private List<Dependency> getAllBaseDependencies() throws MojoExecutionException {
        // 获取基座DependencyParent的原始Model
        Model baseDependencyPom = getBaseDependencyParentOriginalModel();
        if (null == baseDependencyPom) {
            throw new MojoExecutionException(
                String.format("can not find base dependency parent: %s",
                    config.getBaseDependencyParentIdentity()));
        }

        if (null == baseDependencyPom.getDependencyManagement()) {
            return Collections.emptyList();
        }

        return baseDependencyPom.getDependencyManagement().getDependencies();
    }

    protected Model getBaseDependencyParentOriginalModel() {
        MavenProject proj = project;
        while (null != proj) {
            if (getGAIdentity(proj.getArtifact()).equals(config.getBaseDependencyParentIdentity())
                || getGAVIdentity(proj.getArtifact()).equals(
                    config.getBaseDependencyParentIdentity())) {
                return proj.getOriginalModel();
            }
            proj = proj.getParent();
        }
        return null;
    }

    private String getGAVIdentity(Artifact artifact) {
        return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
               + artifact.getBaseVersion();
    }

    private String getGAIdentity(Artifact artifact) {
        return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId();
    }

    protected String getArtifactIdentity(Artifact artifact) {
        if (artifact.hasClassifier()) {
            return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
                   + artifact.getBaseVersion() + STRING_COLON + artifact.getClassifier()
                   + STRING_COLON + artifact.getType();
        } else {
            return artifact.getGroupId() + STRING_COLON + artifact.getArtifactId() + STRING_COLON
                   + artifact.getBaseVersion() + STRING_COLON + artifact.getType();
        }

    }

    private String getDependencyIdentity(Dependency dependency) {
        if (StringUtils.isNotEmpty(dependency.getClassifier())) {
            return dependency.getGroupId() + STRING_COLON + dependency.getArtifactId()
                   + STRING_COLON + dependency.getVersion() + STRING_COLON
                   + dependency.getClassifier() + STRING_COLON + dependency.getType();
        } else {
            return dependency.getGroupId() + STRING_COLON + dependency.getArtifactId()
                   + STRING_COLON + dependency.getVersion() + STRING_COLON + dependency.getType();
        }
    }

    protected void initSlimStrategyConfig() throws IOException {
        Map<String, Object> arkYaml = ArkConfigHolder.getArkYaml(baseDir.getAbsolutePath());
        Properties prop = ArkConfigHolder.getArkProperties(baseDir.getAbsolutePath());

        config.setExcludeWithIndirectDependencies(getBooleanWithDefault(prop,
            EXTENSION_EXCLUDE_WITH_INDIRECT_DEPENDENCIES, true));
        config.setExcludeWithIndirectDependencies(getBooleanWithDefault(arkYaml,
            EXTENSION_EXCLUDE_WITH_INDIRECT_DEPENDENCIES, true));

        config.setExcludeSameBaseDependency(getBooleanWithDefault(prop,
            EXTENSION_EXCLUDE_SAME_BASE_DEPENDENCY, true));
        config.setExcludeSameBaseDependency(getBooleanWithDefault(arkYaml,
            EXTENSION_EXCLUDE_SAME_BASE_DEPENDENCY, true));

        config.setBuildFailWhenExcludeBaseDependencyWithDiffVersion(getBooleanWithDefault(prop,
            EXTENSION_BUILD_FAIL_WHEN_EXCLUDE_DIFF_BASE_DEPENDENCY, false));
        config.setBuildFailWhenExcludeBaseDependencyWithDiffVersion(getBooleanWithDefault(arkYaml,
            EXTENSION_BUILD_FAIL_WHEN_EXCLUDE_DIFF_BASE_DEPENDENCY, false));

        initExcludeAndIncludeConfig();
    }

    /**
     * exclude and include config comes from 3 parts:
     * 1. extension from config file that configured by user in sofa-ark-maven-plugin
     * 2. extension from default bootstrap.properties or bootstrap.yml
     * 3. extension from url
     * @throws IOException
     */
    protected void initExcludeAndIncludeConfig() throws IOException {
        // extension from other resource
        if (!StringUtils.isEmpty(config.getPackExcludesConfig())) {
            extensionExcludeAndIncludeArtifacts(baseDir + File.separator + ARK_CONF_BASE_DIR
                                                + File.separator + config.getPackExcludesConfig());
        } else {
            extensionExcludeAndIncludeArtifacts(baseDir + File.separator + ARK_CONF_BASE_DIR
                                                + File.separator + DEFAULT_EXCLUDE_RULES);
        }

        // extension from default bootstrap.properties or bootstrap.yml
        configExcludeArtifactsByDefault();

        // extension from url
        if (StringUtils.isNotBlank(config.getPackExcludesUrl())) {
            extensionExcludeArtifactsFromUrl(config.getPackExcludesUrl(), project.getArtifacts());
        }
    }

    protected Set<Artifact> getArtifactsToFilterByExcludeConfig(Set<Artifact> artifacts) {
        Set<Artifact> literalArtifactsToExclude = getLiteralArtifactsToFilterByExcludeConfig(artifacts);
        if (config.isExcludeWithIndirectDependencies()) {
            return excludeWithIndirectDependencies(literalArtifactsToExclude, artifacts);
        }
        return literalArtifactsToExclude;
    }

    private Set<Artifact> excludeWithIndirectDependencies(Set<Artifact> literalArtifactsToExclude, Set<Artifact> artifacts) {
        Set<String> excludeArtifactIdentities = literalArtifactsToExclude.stream().map(MavenUtils::getArtifactIdentity).collect(Collectors.toSet());
        Map<String,Artifact> artifactMap = artifacts.stream().collect(Collectors.toMap(MavenUtils::getArtifactIdentity,it->it));
        return getExcludeWithIndirectDependencies(projDependencyGraph,excludeArtifactIdentities,artifactMap);
    }

    private Set<Artifact> getExcludeWithIndirectDependencies(DependencyNode node,
                                                             Set<String> literalArtifactsToExclude,
                                                             Map<String, Artifact> artifacts) {
        if (null == node) {
            return Collections.emptySet();
        }

        Set<Artifact> result = new LinkedHashSet<>();

        String artifactIdentity = getArtifactIdentity(node.getArtifact());
        if (literalArtifactsToExclude.contains(artifactIdentity)) {
            // 排除当前依赖
            result.add(artifacts.get(artifactIdentity));

            // 排除当前依赖的所有依赖
            result.addAll(getAllDependencies(node, artifacts));
            return result;
        }

        for (DependencyNode child : node.getChildren()) {
            result.addAll(getExcludeWithIndirectDependencies(child, literalArtifactsToExclude,
                artifacts));
        }

        return result;
    }

    private Set<Artifact> getAllDependencies(DependencyNode node, Map<String, Artifact> artifacts) {
        if (null == node) {
            return Collections.emptySet();
        }

        Set<Artifact> result = new HashSet<>();
        for (DependencyNode child : node.getChildren()) {
            result.add(artifacts.get(getArtifactIdentity(child.getArtifact())));
            result.addAll(getAllDependencies(child, artifacts));
        }
        return result;
    }

    protected Set<Artifact> getLiteralArtifactsToFilterByExcludeConfig(Set<Artifact> artifacts) {
        List<ArtifactItem> excludeList = new ArrayList<>();
        if (config != null
            && (config.getExcludes().contains("*") || config.getExcludes().contains(".*"))) {
            return artifacts;
        }
        for (String exclude : config.getExcludes()) {
            ArtifactItem item = ArtifactItem.parseArtifactItem(exclude);
            excludeList.add(item);
        }

        Set<Artifact> result = new LinkedHashSet<>();
        for (Artifact e : artifacts) {
            if (checkMatchExclude(excludeList, e)) {
                result.add(e);
            }
        }

        return result;
    }

    protected Set<Artifact> getArtifactsToAddByIncludeConfig(Set<Artifact> artifacts) {
        List<ArtifactItem> includeList = new ArrayList<>();
        if (config != null
            && (config.getIncludes().contains("*") || config.getIncludes().contains(".*"))) {
            return artifacts;
        }
        for (String include : config.getIncludes()) {
            ArtifactItem item = ArtifactItem.parseArtifactItem(include);
            includeList.add(item);
        }

        Set<Artifact> result = new LinkedHashSet<>();
        for (Artifact e : artifacts) {
            if (checkMatchInclude(includeList, e)) {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * This method is core method for excluding artifacts in sofa-ark-maven-plugin &lt;excludeGroupIds&gt;
     * and &lt;excludeArtifactIds&gt; config.
     *
     * @param excludeList
     * @param artifact
     * @return
     */
    private boolean checkMatchExclude(List<ArtifactItem> excludeList, Artifact artifact) {
        for (ArtifactItem exclude : excludeList) {
            if (exclude.isSameWithVersion(ArtifactItem.parseArtifactItem(artifact))) {
                return true;
            }
        }

        if (checkMatchGroupId(config.getExcludeGroupIds(), artifact)) {
            return true;
        }

        return checkMatchArtifactId(config.getExcludeArtifactIds(), artifact);
    }

    /**
     * This method is core method for including artifacts in sofa-ark-maven-plugin &lt;includeGroupIds&gt;
     * and &lt;includeArtifactIds&gt; config.
     *
     * @param includeList
     * @param artifact
     * @return
     */
    private boolean checkMatchInclude(List<ArtifactItem> includeList, Artifact artifact) {
        for (ArtifactItem include : includeList) {
            if (include.isSameWithVersion(ArtifactItem.parseArtifactItem(artifact))) {
                return true;
            }
        }

        if (checkMatchGroupId(config.getIncludeGroupIds(), artifact)) {
            return true;
        }

        return checkMatchArtifactId(config.getIncludeArtifactIds(), artifact);
    }

    private boolean checkMatchGroupId(Set<String> groupIds, Artifact artifact) {
        if (groupIds != null) {
            // 支持通配符
            for (String groupId : groupIds) {
                if (groupId.endsWith(Constants.PACKAGE_PREFIX_MARK)
                    || groupId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                    if (groupId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                        groupId = StringUtils.removeEnd(groupId, Constants.PACKAGE_PREFIX_MARK_2);
                    } else if (groupId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                        groupId = StringUtils.removeEnd(groupId, Constants.PACKAGE_PREFIX_MARK);
                    }

                    if (artifact.getGroupId().startsWith(groupId)) {
                        return true;
                    }
                } else {
                    if (artifact.getGroupId().equals(groupId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkMatchArtifactId(Set<String> artifactIds, Artifact artifact) {
        if (artifactIds != null) {
            // 支持通配符
            for (String artifactId : artifactIds) {
                if (artifactId.endsWith(Constants.PACKAGE_PREFIX_MARK)
                    || artifactId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                    if (artifactId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                        artifactId = StringUtils.removeEnd(artifactId,
                            Constants.PACKAGE_PREFIX_MARK_2);
                    } else if (artifactId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                        artifactId = StringUtils.removeEnd(artifactId,
                            Constants.PACKAGE_PREFIX_MARK);
                    }
                    if (artifact.getArtifactId().startsWith(artifactId)) {
                        return true;
                    }
                } else {
                    if (artifact.getArtifactId().equals(artifactId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void extensionExcludeAndIncludeArtifacts(String extraResources) {
        try {
            File configFile = com.alipay.sofa.ark.common.util.FileUtils.file(extraResources);
            if (configFile.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
                String dataLine;
                while ((dataLine = bufferedReader.readLine()) != null) {
                    if (dataLine.startsWith(EXTENSION_EXCLUDES)) {
                        ParseUtils.parseExcludeConf(config.getExcludes(), dataLine,
                            EXTENSION_EXCLUDES);
                    } else if (dataLine.startsWith(EXTENSION_EXCLUDES_GROUPIDS)) {
                        ParseUtils.parseExcludeConf(config.getExcludeGroupIds(), dataLine,
                            EXTENSION_EXCLUDES_GROUPIDS);
                    } else if (dataLine.startsWith(EXTENSION_EXCLUDES_ARTIFACTIDS)) {
                        ParseUtils.parseExcludeConf(config.getExcludeArtifactIds(), dataLine,
                            EXTENSION_EXCLUDES_ARTIFACTIDS);
                    } else if (dataLine.startsWith(EXTENSION_INCLUDES)) {
                        ParseUtils.parseExcludeConf(config.getIncludes(), dataLine,
                            EXTENSION_INCLUDES);
                    } else if (dataLine.startsWith(EXTENSION_INCLUDES_GROUPIDS)) {
                        ParseUtils.parseExcludeConf(config.getIncludeGroupIds(), dataLine,
                            EXTENSION_INCLUDES_GROUPIDS);
                    } else if (dataLine.startsWith(EXTENSION_INCLUDES_ARTIFACTIDS)) {
                        ParseUtils.parseExcludeConf(config.getIncludeArtifactIds(), dataLine,
                            EXTENSION_INCLUDES_ARTIFACTIDS);
                    }
                }
            }
        } catch (IOException ex) {
            getLog().error("failed to extension excludes artifacts.", ex);
        }
    }

    protected void configExcludeArtifactsByDefault() throws IOException {
        // extension from default ark.properties and ark.yml
        Map<String, Object> arkYaml = ArkConfigHolder.getArkYaml(baseDir.getAbsolutePath());
        Properties prop = ArkConfigHolder.getArkProperties(baseDir.getAbsolutePath());

        config.getExcludes().addAll(getStringSet(prop, EXTENSION_EXCLUDES));
        config.getExcludeGroupIds().addAll(getStringSet(prop, EXTENSION_EXCLUDES_GROUPIDS));
        config.getExcludeArtifactIds().addAll(getStringSet(prop, EXTENSION_EXCLUDES_ARTIFACTIDS));
        config.getIncludes().addAll(getStringSet(prop, EXTENSION_INCLUDES));
        config.getIncludeGroupIds().addAll(getStringSet(prop, EXTENSION_INCLUDES_GROUPIDS));
        config.getIncludeArtifactIds().addAll(getStringSet(prop, EXTENSION_INCLUDES_ARTIFACTIDS));

        config.getExcludes().addAll(getStringSet(arkYaml, EXTENSION_EXCLUDES));
        config.getExcludeGroupIds().addAll(getStringSet(arkYaml, EXTENSION_EXCLUDES_GROUPIDS));
        config.getExcludeArtifactIds()
            .addAll(getStringSet(arkYaml, EXTENSION_EXCLUDES_ARTIFACTIDS));
        config.getIncludes().addAll(getStringSet(arkYaml, EXTENSION_INCLUDES));
        config.getIncludeGroupIds().addAll(getStringSet(arkYaml, EXTENSION_INCLUDES_GROUPIDS));
        config.getIncludeArtifactIds()
            .addAll(getStringSet(arkYaml, EXTENSION_INCLUDES_ARTIFACTIDS));
    }

    protected void extensionExcludeArtifactsFromUrl(String packExcludesUrl, Set<Artifact> artifacts) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(packExcludesUrl);
            CloseableHttpResponse response = client.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200 && response.getEntity() != null) {
                String result = EntityUtils.toString(response.getEntity());
                getLog().info(
                    String.format("success to get excludes config from url: %s, response: %s",
                        packExcludesUrl, result));
                ObjectMapper objectMapper = new ObjectMapper();
                ExcludeConfigResponse excludeConfigResponse = objectMapper.readValue(result,
                    ExcludeConfigResponse.class);
                if (excludeConfigResponse.isSuccess() && excludeConfigResponse.getResult() != null) {
                    ExcludeConfig excludeConfig = excludeConfigResponse.getResult();
                    List<String> jarBlackGroupIds = excludeConfig.getJarBlackGroupIds();
                    List<String> jarBlackArtifactIds = excludeConfig.getJarBlackArtifactIds();
                    List<String> jarBlackList = excludeConfig.getJarBlackList();
                    List<String> jarWhiteGroupIds = excludeConfig.getJarWhiteGroupIds();
                    List<String> jarWhiteArtifactIds = excludeConfig.getJarWhiteArtifactIds();
                    List<String> jarWhiteList = excludeConfig.getJarWhiteList();
                    if (CollectionUtils.isNotEmpty(jarBlackGroupIds)) {
                        config.getExcludeGroupIds().addAll(jarBlackGroupIds);
                    }
                    if (CollectionUtils.isNotEmpty(jarBlackArtifactIds)) {
                        config.getExcludeArtifactIds().addAll(jarBlackArtifactIds);
                    }
                    if (CollectionUtils.isNotEmpty(jarBlackList)) {
                        config.getExcludes().addAll(jarBlackList);
                    }
                    if (CollectionUtils.isNotEmpty(jarWhiteGroupIds)) {
                        config.getIncludeGroupIds().addAll(jarWhiteGroupIds);
                    }
                    if (CollectionUtils.isNotEmpty(jarWhiteArtifactIds)) {
                        config.getIncludeArtifactIds().addAll(jarWhiteArtifactIds);
                    }
                    if (CollectionUtils.isNotEmpty(jarWhiteList)) {
                        config.getIncludes().addAll(jarWhiteList);
                    }
                    logExcludeMessage(jarBlackGroupIds, jarBlackArtifactIds, jarBlackList,
                        artifacts, true);

                    List<String> jarWarnGroupIds = excludeConfig.getJarWarnGroupIds();
                    List<String> jarWarnArtifactIds = excludeConfig.getJarWarnArtifactIds();
                    List<String> jarWarnList = excludeConfig.getJarWarnList();
                    logExcludeMessage(jarWarnGroupIds, jarWarnArtifactIds, jarWarnList, artifacts,
                        false);
                }
            }
            response.close();
            client.close();
        } catch (Exception e) {
            getLog().error(
                String.format("failed to get excludes config from url: %s", packExcludesUrl), e);
        }
    }

    protected void logExcludeMessage(List<String> jarGroupIds, List<String> jarArtifactIds,
                                     List<String> jarList, Set<Artifact> artifacts, boolean error) {
        if (CollectionUtils.isNotEmpty(jarGroupIds)) {
            for (Artifact artifact : artifacts) {
                if (inUnLogScopes(artifact.getScope())) {
                    continue;
                }
                for (String jarBlackGroupId : jarGroupIds) {
                    if (jarBlackGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK)
                        || jarBlackGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                        if (jarBlackGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                            jarBlackGroupId = StringUtils.remove(jarBlackGroupId,
                                Constants.PACKAGE_PREFIX_MARK_2);
                        } else if (jarBlackGroupId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                            jarBlackGroupId = StringUtils.removeEnd(jarBlackGroupId,
                                Constants.PACKAGE_PREFIX_MARK);
                        }

                        if (artifact.getGroupId().startsWith(jarBlackGroupId)) {
                            if (error) {
                                getLog()
                                    .error(
                                        String
                                            .format(
                                                "Error to package jar: %s due to match groupId: %s, automatically exclude it.",
                                                artifact, jarBlackGroupId));
                            } else {
                                getLog().warn(
                                    String.format(
                                        "Warn to package jar: %s due to match groupId: %s",
                                        artifact, jarBlackGroupId));
                            }

                        }
                    } else {
                        if (artifact.getGroupId().equals(jarBlackGroupId)) {
                            if (error) {
                                getLog()
                                    .error(
                                        String
                                            .format(
                                                "Error to package jar: %s due to match groupId: %s, automatically exclude it.",
                                                artifact, jarBlackGroupId));
                            } else {
                                getLog().warn(
                                    String.format(
                                        "Warn to package jar: %s due to match groupId: %s",
                                        artifact, jarBlackGroupId));
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(jarArtifactIds)) {
            for (Artifact artifact : artifacts) {
                if (inUnLogScopes(artifact.getScope())) {
                    continue;
                }
                for (String jarBlackArtifactId : jarArtifactIds) {
                    if (jarBlackArtifactId.endsWith(Constants.PACKAGE_PREFIX_MARK)
                        || jarBlackArtifactId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                        if (jarBlackArtifactId.endsWith(Constants.PACKAGE_PREFIX_MARK_2)) {
                            jarBlackArtifactId = StringUtils.removeEnd(jarBlackArtifactId,
                                Constants.PACKAGE_PREFIX_MARK_2);
                        } else if (jarBlackArtifactId.endsWith(Constants.PACKAGE_PREFIX_MARK)) {
                            jarBlackArtifactId = StringUtils.removeEnd(jarBlackArtifactId,
                                Constants.PACKAGE_PREFIX_MARK);
                        }
                        if (artifact.getArtifactId().startsWith(jarBlackArtifactId)) {
                            if (error) {
                                getLog()
                                    .error(
                                        String
                                            .format(
                                                "Error to package jar: %s due to match artifactId: %s, automatically exclude it.",
                                                artifact, jarBlackArtifactId));
                            } else {
                                getLog().warn(
                                    String.format(
                                        "Warn to package jar: %s due to match artifactId: %s",
                                        artifact, jarBlackArtifactId));
                            }
                        }
                    } else {
                        if (artifact.getArtifactId().equals(jarBlackArtifactId)) {
                            if (error) {
                                getLog()
                                    .error(
                                        String
                                            .format(
                                                "Error to package jar: %s due to match artifactId: %s, automatically exclude it.",
                                                artifact, jarBlackArtifactId));
                            } else {
                                getLog().warn(
                                    String.format(
                                        "Warn to package jar: %s due to match artifactId: %s",
                                        artifact, jarBlackArtifactId));
                            }
                        }
                    }
                }
            }
        }
        if (CollectionUtils.isNotEmpty(jarList)) {
            for (Artifact artifact : artifacts) {
                if (inUnLogScopes(artifact.getScope())) {
                    continue;
                }
                for (String jarBlack : jarList) {
                    if (jarBlack.equals(String.join(":", artifact.getGroupId(),
                        artifact.getArtifactId(), artifact.getVersion()))) {
                        if (error) {
                            getLog()
                                .error(
                                    String
                                        .format(
                                            "Error to package jar: %s due to match groupId:artifactId:version: %s, automatically exclude it.",
                                            artifact, jarBlack));
                        } else {
                            getLog()
                                .warn(
                                    String
                                        .format(
                                            "Warn to package jar: %s due to match groupId:artifactId:version: %s",
                                            artifact, jarBlack));
                        }
                    }
                }
            }
        }
    }

    private Log getLog() {
        return log;
    }

    public static class ExcludeConfigResponse {

        private boolean       success;

        private ExcludeConfig result;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public ExcludeConfig getResult() {
            return result;
        }

        public void setResult(ExcludeConfig result) {
            this.result = result;
        }
    }

    public static class ExcludeConfig {

        private String       app;

        private List<String> jarBlackGroupIds;

        private List<String> jarBlackArtifactIds;

        private List<String> jarBlackList;

        private List<String> JarWhiteGroupIds;

        private List<String> jarWhiteArtifactIds;

        private List<String> jarWhiteList;

        private List<String> jarWarnGroupIds;

        private List<String> jarWarnArtifactIds;

        private List<String> jarWarnList;

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public List<String> getJarBlackGroupIds() {
            return jarBlackGroupIds;
        }

        public void setJarBlackGroupIds(List<String> jarBlackGroupIds) {
            this.jarBlackGroupIds = jarBlackGroupIds;
        }

        public List<String> getJarBlackArtifactIds() {
            return jarBlackArtifactIds;
        }

        public void setJarBlackArtifactIds(List<String> jarBlackArtifactIds) {
            this.jarBlackArtifactIds = jarBlackArtifactIds;
        }

        public List<String> getJarBlackList() {
            return jarBlackList;
        }

        public void setJarBlackList(List<String> jarBlackList) {
            this.jarBlackList = jarBlackList;
        }

        public List<String> getJarWhiteGroupIds() {
            return JarWhiteGroupIds;
        }

        public void setJarWhiteGroupIds(List<String> jarWhiteGroupIds) {
            JarWhiteGroupIds = jarWhiteGroupIds;
        }

        public List<String> getJarWhiteArtifactIds() {
            return jarWhiteArtifactIds;
        }

        public void setJarWhiteArtifactIds(List<String> jarWhiteArtifactIds) {
            this.jarWhiteArtifactIds = jarWhiteArtifactIds;
        }

        public List<String> getJarWhiteList() {
            return jarWhiteList;
        }

        public void setJarWhiteList(List<String> jarWhiteList) {
            this.jarWhiteList = jarWhiteList;
        }

        public List<String> getJarWarnGroupIds() {
            return jarWarnGroupIds;
        }

        public void setJarWarnGroupIds(List<String> jarWarnGroupIds) {
            this.jarWarnGroupIds = jarWarnGroupIds;
        }

        public List<String> getJarWarnArtifactIds() {
            return jarWarnArtifactIds;
        }

        public void setJarWarnArtifactIds(List<String> jarWarnArtifactIds) {
            this.jarWarnArtifactIds = jarWarnArtifactIds;
        }

        public List<String> getJarWarnList() {
            return jarWarnList;
        }

        public void setJarWarnList(List<String> jarWarnList) {
            this.jarWarnList = jarWarnList;
        }
    }

}
