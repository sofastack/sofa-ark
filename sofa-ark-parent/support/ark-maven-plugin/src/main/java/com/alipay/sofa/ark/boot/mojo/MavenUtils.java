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

import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.tools.ArtifactItem;
import org.apache.maven.project.MavenProject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MavenUtils {
    public static boolean isRootProject(MavenProject project) {
        if (project == null) {
            return true;
        }

        if (project.hasParent() && project.getParent().getBasedir() != null) {
            return false;
        }
        return true;
    }

    public static MavenProject getRootProject(MavenProject project) {
        if (project == null) {
            return null;
        }
        MavenProject parent = project;
        while (parent.hasParent() && parent.getParent().getBasedir() != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * @param depTreeContent
     * @return
     */
    public static Set<ArtifactItem> convert(String depTreeContent) {
        Set<ArtifactItem> artifactItems = new HashSet<>();
        String[] contents = depTreeContent.split("\n");

        for (String content : contents) {
            ArtifactItem artifactItem = getArtifactItem(content);
            if (artifactItem != null && !"test".equals(artifactItem.getScope())) {
                artifactItems.add(artifactItem);
            }
        }

        return artifactItems;
    }

    private static ArtifactItem getArtifactItem(String lineContent) {
        if (StringUtils.isEmpty(lineContent)) {
            return null;
        }
        lineContent = StringUtils.removeCR(lineContent);
        String[] contentInfos = lineContent.split(" ");
        if (contentInfos.length == 0) {
            return null;
        }
        Optional<String> artifactStrOp = Arrays.stream(contentInfos).filter(c -> c.contains(":")).findFirst();
        if (!artifactStrOp.isPresent()) {
            return null;
        }
        String[] artifactInfos = artifactStrOp.get().split(":");

        ArtifactItem artifactItem = new ArtifactItem();
        if (artifactInfos.length == 5) {
            // like "com.alipay.sofa:healthcheck-sofa-boot-starter:jar:3.11.1:provided"

            artifactItem.setGroupId(artifactInfos[0]);
            artifactItem.setArtifactId(artifactInfos[1]);
            artifactItem.setType(artifactInfos[2]);
            artifactItem.setVersion(artifactInfos[3]);
            artifactItem.setScope(artifactInfos[4]);
        } else if (artifactInfos.length == 6) {
            // like "io.sofastack:dynamic-stock-mng:jar:ark-biz:1.0.0:compile"

            artifactItem.setGroupId(artifactInfos[0]);
            artifactItem.setArtifactId(artifactInfos[1]);
            artifactItem.setType(artifactInfos[2]);
            artifactItem.setClassifier(artifactInfos[3]);
            artifactItem.setVersion(artifactInfos[4]);
            artifactItem.setScope(artifactInfos[5]);
        } else {
            return null;
        }
        return artifactItem;
    }
}
