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
package com.alipay.sofa.ark.spi.constant;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public abstract class Constants {

    public final static String ARK_CONTAINER_MARK_ENTRY  = "com/alipay/sofa/ark/container/mark";

    public final static String ARK_PLUGIN_MARK_ENTRY     = "com/alipay/sofa/ark/plugin/mark";

    public final static String ARK_MODULE_MARK_ENTRY     = "com/alipay/sofa/ark/biz/mark";

    public final static String PRIORITY_ATTRIBUTE        = "priority";
    public final static String GROUP_ID_ATTRIBUTE        = "groupId";
    public final static String ARTIFACT_ID_ATTRIBUTE     = "artifactId";
    public final static String PLUGIN_NAME_ATTRIBUTE     = "pluginName";
    public final static String PLUGIN_VERSION_ATTRIBUTE  = "version";
    public final static String ACTIVATOR_ATTRIBUTE       = "activator";
    public final static String IMPORT_CLASSES_ATTRIBUTE  = "import-classes";
    public final static String IMPORT_PACKAGES_ATTRIBUTE = "import-packages";
    public final static String EXPORT_CLASSES_ATTRIBUTE  = "export-classes";
    public final static String EXPORT_PACKAGES_ATTRIBUTE = "export-packages";

    public final static String MAIN_CLASS_ATTRIBUTE      = "Main-Class";
    public final static String ARK_BIZ_NAME              = "Ark-Biz-Name";

}