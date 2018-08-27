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
public class Constants {

    /**
     * Archiver Marker
     */
    public final static String ARK_CONTAINER_MARK_ENTRY              = "com/alipay/sofa/ark/container/mark";

    public final static String ARK_PLUGIN_MARK_ENTRY                 = "com/alipay/sofa/ark/plugin/mark";

    public final static String ARK_BIZ_MARK_ENTRY                    = "com/alipay/sofa/ark/biz/mark";

    /**
     * Ark Plugin Attribute
     */
    public final static String PRIORITY_ATTRIBUTE                    = "priority";
    public final static String GROUP_ID_ATTRIBUTE                    = "groupId";
    public final static String ARTIFACT_ID_ATTRIBUTE                 = "artifactId";
    public final static String PLUGIN_NAME_ATTRIBUTE                 = "pluginName";
    public final static String PLUGIN_VERSION_ATTRIBUTE              = "version";
    public final static String ACTIVATOR_ATTRIBUTE                   = "activator";
    public final static String IMPORT_CLASSES_ATTRIBUTE              = "import-classes";
    public final static String IMPORT_PACKAGES_ATTRIBUTE             = "import-packages";
    public final static String EXPORT_CLASSES_ATTRIBUTE              = "export-classes";
    public final static String EXPORT_PACKAGES_ATTRIBUTE             = "export-packages";

    /**
     * Ark Biz Attribute
     */
    public final static String MAIN_CLASS_ATTRIBUTE                  = "Main-Class";
    public final static String ARK_BIZ_NAME                          = "Ark-Biz-Name";
    public final static String ARK_BIZ_VERSION                       = "Ark-Biz-Version";
    public final static String DENY_IMPORT_CLASSES                   = "deny-import-classes";
    public final static String DENY_IMPORT_PACKAGES                  = "deny-import-packages";
    public final static String DENY_IMPORT_RESOURCES                 = "deny-import-resources";

    public final static String PACKAGE_PREFIX_MARK                   = "*";
    public final static String DEFAULT_PACKAGE                       = ".";
    public final static String MANIFEST_VALUE_SPLIT                  = ",";

    public final static String IMPORT_RESOURCES_ATTRIBUTE            = "import-resources";
    public final static String EXPORT_RESOURCES_ATTRIBUTE            = "export-resources";

    public final static String SUREFIRE_BOOT_CLASSPATH               = "Class-Path";
    public final static String SUREFIRE_BOOT_CLASSPATH_SPLIT         = " ";

    /**
     * Telnet Server
     */
    public final static String TELNET_SERVER_ENABLE                  = "sofa.ark.telnet.server.enable";
    public final static String TELNET_PORT_ATTRIBUTE                 = "sofa.ark.telnet";
    public final static int    DEFAULT_TELNET_PORT                   = 1234;
    public final static int    DEFAULT_SELECT_PORT_SIZE              = 100;
    public final static String TELNET_SERVER_WORKER_THREAD_POOL_NAME = "telnet-server-worker";
    public final static String TELNET_SESSION_PROMPT                 = "sofa-ark>";
    public final static int    BUFFER_CHUNK                          = 128;

    /**
     * String Constants
     */
    public final static String SPACE_SPLIT                           = "\\s+";
    public final static String STRING_COLON                          = ":";
    public final static String TELNET_STRING_END                     = new String(new byte[] {
            (byte) 13, (byte) 10                                    });

    /**
     * Event
     */
    public final static String BIZ_EVENT_TOPIC_UNINSTALL             = "UnInstall-Ark-Biz";
    public final static String BIZ_EVENT_TOPIC_HEALTH_CHECK          = "Health-Check-Ark-Biz";

    /**
     * Environment Properties
     */
    public final static String SPRING_BOOT_ENDPOINTS_JMX_ENABLED     = "endpoints.jmx.enabled";
    public final static String LOG4J_IGNORE_TCL                      = "log4j.ignoreTCL";

}