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
}
