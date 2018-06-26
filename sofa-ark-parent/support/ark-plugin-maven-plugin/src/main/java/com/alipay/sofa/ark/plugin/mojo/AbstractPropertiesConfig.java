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
package com.alipay.sofa.ark.plugin.mojo;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class AbstractPropertiesConfig {

    public static final String      KEY_PACKAGES  = "packages";
    public static final String      KEY_CLASSES   = "classes";
    public static final String      KEY_RESOURCES = "resources";
    public static final String      KEY_EXPORT    = "export";
    public static final String      KEY_IMPORT    = "import";
    public static final String      KEY_SPLIT     = "-";
    public static final String      VALUE_SPLIT   = ",";

    /**
     * imported or exported packages config
     */
    protected LinkedHashSet<String> packages;

    /**
     * imported or exported classes config
     */
    protected LinkedHashSet<String> classes;

    /**
     * imported or exported class config
     */
    protected LinkedHashSet<String> resources;

    public LinkedHashSet<String> getPackages() {
        return packages;
    }

    public void setPackages(LinkedHashSet<String> packages) {
        this.packages = packages;
    }

    public LinkedHashSet<String> getClasses() {
        return classes;
    }

    public void setClasses(LinkedHashSet<String> classes) {
        this.classes = classes;
    }

    /**
     * Getter method for property <tt>resources</tt>.
     *
     * @return property value of resources
     */
    public LinkedHashSet<String> getResources() {
        return resources;
    }

    public void setResources(LinkedHashSet<String> resources) {
        this.resources = resources;
    }

    public static void storeKeyValuePair(Properties prop, String name, Collection<String> value) {
        if (value == null) {
            value = new LinkedHashSet<>();
        }
        prop.setProperty(name, join(value.iterator(), VALUE_SPLIT));
    }

    public static String join(Iterator iterator, String separator) {
        if (separator == null) {
            separator = "";
        }
        StringBuffer buf = new StringBuffer(256);
        while (iterator.hasNext()) {
            buf.append(iterator.next());
            if (iterator.hasNext()) {
                buf.append(separator);
            }
        }
        return buf.toString();
    }

    /**
     * Store user configuration
     * @param props
     */
    public abstract void store(Properties props);

}