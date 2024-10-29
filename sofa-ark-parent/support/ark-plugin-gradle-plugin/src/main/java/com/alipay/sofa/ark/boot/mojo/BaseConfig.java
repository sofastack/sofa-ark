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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class BaseConfig {
    private List<String> packages = new ArrayList<>();
    private List<String> classes = new ArrayList<>();
    private List<String> resources = new ArrayList<>();

    public List<String> getPackages() {
        return packages;
    }

    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public List<String> getResources() {
        return resources;
    }

    public void setResources(List<String> resources) {
        this.resources = resources;
    }


    public Map<String, String> toAttributes(String prefix) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(prefix + "-packages", packages != null ? String.join(",", packages) : "");
        attributes.put(prefix + "-classes", classes != null ? String.join(",", classes) : "");
        attributes.put(prefix + "-resources", resources != null ? String.join(",", resources) : "");
        return attributes;
    }
}
