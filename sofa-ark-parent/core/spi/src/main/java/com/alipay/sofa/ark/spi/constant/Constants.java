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

import java.util.ArrayList;
import java.util.List;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class Constants {
    /**
     * String Constants
     */
    public final static String       SPACE_SPLIT                                   = "\\s+";
    public final static String       STRING_COLON                                  = ":";
    public final static String       STRING_SEMICOLON                              = ";";
    public final static String       TELNET_STRING_END                             = new String(
                                                                                       new byte[] {
            (byte) 13, (byte) 10                                                      });
    public final static String       COMMA_SPLIT                                   = ",";
    public final static String       EMPTY_STR                                     = "";
    public final static String       AMPERSAND_SPLIT                               = "&";
    public final static String       EQUAL_SPLIT                                   = "=";
    public final static String       QUESTION_MARK_SPLIT                           = "?";
    public final static String       ROOT_WEB_CONTEXT_PATH                         = "/";

    /**
     * ark conf
     */
    public final static String       CONF_BASE_DIR                                 = "conf/";
    public final static String       ARK_CONF_BASE_DIR                             = "conf/ark";
    public final static String       ARK_CONF_FILE                                 = "bootstrap.properties";
    public final static String       ARK_CONF_FILE_FORMAT                          = "bootstrap-%s.properties";

    public final static String       ARK_CONF_YAML_FILE                            = "bootstrap.yml";

    public final static String       PLUGIN_EXTENSION_FORMAT                       = "PLUGIN-EXPORT[%s]";

    public final static String       DEFAULT_PROFILE                               = EMPTY_STR;

    public final static String       LOCAL_HOST                                    = "localhost";

    /**
     * ark classloader cache conf
     */
    public final static String       ARK_CLASSLOADER_CACHE_CLASS_SIZE_INITIAL      = "ark.classloader.cache.class.size.initial";
    public final static String       ARK_CLASSLOADER_CACHE_CLASS_SIZE_MAX          = "ark.classloader.cache.class.size.max";
    public final static String       ARK_CLASSLOADER_CACHE_CONCURRENCY_LEVEL       = "ark.classloader.cache.concurrencylevel";
    /**
     * plugin conf, multi value is split by comma.
     */
    public final static String       PLUGIN_ACTIVE_INCLUDE                         = "ark.plugin.active.include";
    public final static String       PLUGIN_ACTIVE_EXCLUDE                         = "ark.plugin.active.exclude";

    /**
     * biz conf, multi value is split by comma.
     */
    public final static String       BIZ_ACTIVE_INCLUDE                            = "ark.biz.active.include";
    public final static String       BIZ_ACTIVE_EXCLUDE                            = "ark.biz.active.exclude";

    /**
     * Archiver Marker
     */
    public final static String       ARK_CONTAINER_MARK_ENTRY                      = "com/alipay/sofa/ark/container/mark";

    public final static String       ARK_PLUGIN_MARK_ENTRY                         = "com/alipay/sofa/ark/plugin/mark";

    public final static String       ARK_BIZ_MARK_ENTRY                            = "com/alipay/sofa/ark/biz/mark";

    /**
     * Ark Plugin Attribute
     */
    public final static String       PRIORITY_ATTRIBUTE                            = "priority";
    public final static String       GROUP_ID_ATTRIBUTE                            = "groupId";
    public final static String       ARTIFACT_ID_ATTRIBUTE                         = "artifactId";
    public final static String       PLUGIN_NAME_ATTRIBUTE                         = "pluginName";
    public final static String       PLUGIN_VERSION_ATTRIBUTE                      = "version";
    public final static String       ACTIVATOR_ATTRIBUTE                           = "activator";
    public final static String       WEB_CONTEXT_PATH                              = "web-context-path";
    public final static String       IMPORT_CLASSES_ATTRIBUTE                      = "import-classes";
    public final static String       IMPORT_PACKAGES_ATTRIBUTE                     = "import-packages";

    public final static String       EXPORT_MODE                                   = "export-mode";
    public final static String       EXPORT_CLASSES_ATTRIBUTE                      = "export-classes";
    public final static String       EXPORT_PACKAGES_ATTRIBUTE                     = "export-packages";

    /**
     * Ark Biz Attribute
     */
    public final static String       MAIN_CLASS_ATTRIBUTE                          = "Main-Class";
    public final static String       START_CLASS_ATTRIBUTE                         = "Start-Class";
    public final static String       ARK_BIZ_NAME                                  = "Ark-Biz-Name";
    public final static String       ARK_BIZ_VERSION                               = "Ark-Biz-Version";
    public final static String       DENY_IMPORT_CLASSES                           = "deny-import-classes";
    public final static String       DENY_IMPORT_PACKAGES                          = "deny-import-packages";
    public final static String       DENY_IMPORT_RESOURCES                         = "deny-import-resources";
    public final static String       INJECT_PLUGIN_DEPENDENCIES                    = "inject-plugin-dependencies";
    public final static String       INJECT_EXPORT_PACKAGES                        = "inject-export-packages";
    public final static String       DECLARED_LIBRARIES                            = "declared-libraries";

    public static final String       BRANCH                                        = "commit-branch";
    public static final String       COMMIT_ID                                     = "commit-id";
    public static final String       BUILD_USER                                    = "build-user";
    public static final String       BUILD_EMAIL                                   = "build-email";
    public static final String       BUILD_TIME                                    = "build-time";
    public static final String       COMMIT_AUTHOR_NAME                            = "commit-user-name";
    public static final String       COMMIT_AUTHOR_EMAIL                           = "commit-user-email";
    public static final String       COMMIT_TIMESTAMP                              = "commit-timestamp";
    public static final String       COMMIT_TIME                                   = "commit-time";
    public static final String       REMOTE_ORIGIN_URL                             = "remote-origin-url";

    public static final String       DATE_FORMAT                                   = "yyyy-MM-dd'T'HH:mm:ssZ";

    public final static String       PACKAGE_PREFIX_MARK                           = "*";

    public final static String       PACKAGE_PREFIX_MARK_2                         = ".*";
    public final static String       DEFAULT_PACKAGE                               = ".";
    public final static String       MANIFEST_VALUE_SPLIT                          = COMMA_SPLIT;
    public final static String       RESOURCE_STEM_MARK                            = "*";

    public final static String       IMPORT_RESOURCES_ATTRIBUTE                    = "import-resources";
    public final static String       EXPORT_RESOURCES_ATTRIBUTE                    = "export-resources";

    public final static String       SUREFIRE_BOOT_CLASSPATH                       = "Class-Path";
    public final static String       SUREFIRE_BOOT_CLASSPATH_SPLIT                 = " ";
    public final static String       SUREFIRE_BOOT_JAR                             = "surefirebooter";

    /**
     * Telnet Server
     */
    public final static String       TELNET_SERVER_ENABLE                          = "sofa.ark.telnet.server.enable";
    public final static String       TELNET_SERVER_SECURITY_ENABLE                 = "sofa.ark.telnet.security.enable";
    public final static String       CONFIG_SERVER_ENABLE                          = "sofa.ark.config.server.enable";

    /**
     * 配置中心支持, 默认使用zookeeper
     * value值为com.alipay.sofa.ark.config.ConfigTypeEnum枚举的name()
     */
    public final static String       CONFIG_SERVER_TYPE                            = "sofa.ark.config.server.type";
    /**
     * 使用apollo的namespace
     */
    public final static String       CONFIG_APOLLO_NAMESPACE                       = "sofa-ark";
    /**
     * apollo的namespace下动态命名对应的key
     */
    public final static String       APOLLO_MASTER_BIZ_KEY                         = "masterBiz";
    public final static String       TELNET_PORT_ATTRIBUTE                         = "sofa.ark.telnet.port";
    public final static int          DEFAULT_TELNET_PORT                           = 1234;
    public final static int          DEFAULT_SELECT_PORT_SIZE                      = 100;
    public final static String       TELNET_SERVER_WORKER_THREAD_POOL_NAME         = "telnet-server-worker";
    public final static String       TELNET_SESSION_PROMPT                         = "sofa-ark>";
    public final static String       TELNET_COMMAND_THREAD_POOL_NAME               = "telnet-command";

    /**
     * Event
     */
    public final static String       BIZ_EVENT_TOPIC_AFTER_INVOKE_ALL_BIZ_START    = "AFTER-INVOKE-ALL-BIZ-START";
    public final static String       BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_START        = "AFTER-INVOKE-BIZ-START";
    public final static String       BIZ_EVENT_TOPIC_AFTER_BIZ_FAILED              = "BIZ_EVENT_TOPIC_AFTER_BIZ_FAILED";
    public final static String       BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_STOP         = "AFTER-INVOKE-BIZ-STOP";
    public final static String       BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_STOP_FAILED  = "AFTER-INVOKE-BIZ-STOP-FAILED";

    public final static String       BIZ_EVENT_TOPIC_BEFORE_RECYCLE_BIZ            = "BEFORE-RECYCLE-BIZ";
    public final static String       BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_START       = "BEFORE-INVOKE-BIZ-START";
    public final static String       BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_STOP        = "BEFORE-INVOKE-BIZ-STOP";

    public final static String       PLUGIN_EVENT_TOPIC_AFTER_INVOKE_PLUGIN_START  = "AFTER-INVOKE-PLUGIN-START";
    public final static String       PLUGIN_EVENT_TOPIC_AFTER_INVOKE_PLUGIN_STOP   = "AFTER-INVOKE-PLUGIN-STOP";
    public final static String       PLUGIN_EVENT_TOPIC_BEFORE_INVOKE_PLUGIN_START = "BEFORE-INVOKE-PLUGIN-START";
    public final static String       PLUGIN_EVENT_TOPIC_BEFORE_INVOKE_PLUGIN_STOP  = "BEFORE-INVOKE-PLUGIN-STOP";

    public final static String       BIZ_EVENT_TOPIC_BEFORE_INVOKE_BIZ_SWITCH      = "BEFORE-INVOKE-BIZ-SWITCH";
    public final static String       BIZ_EVENT_TOPIC_AFTER_INVOKE_BIZ_SWITCH       = "AFTER-INVOKE-BIZ-SWITCH";

    public final static String       ARK_EVENT_TOPIC_AFTER_FINISH_STARTUP_STAGE    = "AFTER-FINISH-STARTUP-STAGE";
    public final static String       ARK_EVENT_TOPIC_AFTER_FINISH_DEPLOY_STAGE     = "AFTER-FINISH-DEPLOY-STAGE";

    /**
     * Environment Properties
     */
    public final static String       SPRING_BOOT_ENDPOINTS_JMX_ENABLED             = "endpoints.jmx.enabled";
    public final static String       LOG4J_IGNORE_TCL                              = "log4j.ignoreTCL";
    public final static String       RESOLVE_PARENT_CONTEXT_SERIALIZER_FACTORY     = "hessian.parent.context.create";
    public final static String       EMBED_ENABLE                                  = "sofa.ark.embed.enable";
    public final static String       PLUGIN_EXPORT_CLASS_ENABLE                    = "sofa.ark.plugin.export.class.enable";
    public final static String       EMBED_STATIC_BIZ_ENABLE                       = "sofa.ark.embed.static.biz.enable";

    public final static String       EMBED_STATIC_BIZ_IN_RESOURCE_ENABLE           = "sofa.ark.embed.static.biz.in.resource.enable";
    public final static String       ACTIVATE_NEW_MODULE                           = "activate.new.module";
    public final static String       BIZ_MAIN_CLASS                                = "sofa.ark.biz.main.class";

    /**
     * uninstall the biz if it starts up failed
     */
    public final static String       AUTO_UNINSTALL_WHEN_FAILED_ENABLE             = "sofa.ark.auto.uninstall.when.failed.enable";

    /**
     * unpack the biz when install
     */
    public final static String       UNPACK_BIZ_WHEN_INSTALL                       = "sofa.ark.unpack.biz.when.install";

    /**
     * support multiple version biz as activated
     */
    public final static String       ACTIVATE_MULTI_BIZ_VERSION_ENABLE             = "sofa.ark.activate.multi.biz.version.enable";

    /**
     * auto remove the biz instance in BizManagerService if it stops failed
     */
    public final static String       REMOVE_BIZ_INSTANCE_AFTER_STOP_FAILED         = "sofa.ark.remove.biz.instance.when.stop.failed.enable";

    /**
     * Command Provider
     */
    public final static String       PLUGIN_COMMAND_UNIQUE_ID                      = "plugin-command-provider";
    public final static String       BIZ_COMMAND_UNIQUE_ID                         = "biz-command-provider";

    /**
     * Ark SPI extension
     */
    public final static String       EXTENSION_FILE_DIR                            = "META-INF/services/sofa-ark/";
    public final static String       PLUGIN_CLASS_LOADER_HOOK                      = "plugin-classloader-hook";
    public final static String       BIZ_CLASS_LOADER_HOOK                         = "biz-classloader-hook";
    public final static String       BIZ_CLASS_LOADER_HOOK_DIR                     = "com.alipay.sofa.ark.biz.classloader.hook.dir";
    public final static String       BIZ_TEMP_WORK_DIR_RECYCLE_FILE_SUFFIX         = "deleted";

    /**
     * Multiply biz name
     */
    public final static String       MASTER_BIZ                                    = "com.alipay.sofa.ark.master.biz";

    public final static String       SOFA_ARK_MODULE                               = "SOFA-ARK/biz/";

    /**
     * Config Server
     */
    public final static String       CONFIG_SERVER_ADDRESS                         = "com.alipay.sofa.ark.config.address";
    public final static String       CONFIG_SERVER_ENVIRONMENT                     = "com.alipay.sofa.ark.config.env";
    public final static String       CONFIG_PROTOCOL_ZOOKEEPER                     = "zookeeper";
    public final static String       CONFIG_PROTOCOL_ZOOKEEPER_HEADER              = "zookeeper://";
    public final static String       ZOOKEEPER_CONTEXT_SPLIT                       = "/";
    public final static String       CONFIG_INSTALL_BIZ_DIR                        = "com.alipay.sofa.ark.biz.dir";
    public final static String       CONFIG_BIZ_URL                                = "bizUrl";

    public final static String       CONFIG_CONNECT_TIMEOUT                        = "com.alipay.sofa.ark.config.connect.timeout";
    public final static int          DEFAULT_CONFIG_CONNECT_TIMEOUT                = 20000;

    /**
     * Test ClassLoader
     */
    public final static String       FORCE_DELEGATE_TO_TEST_CLASSLOADER            = "com.alipay.sofa.ark.delegate.to.testClassLoader";
    public final static String       FORCE_DELEGATE_TO_APP_CLASSLOADER             = "com.alipay.sofa.ark.delegate.to.appClassLoader";

    public final static String       EXTENSION_EXCLUDES                            = "excludes";
    public final static String       EXTENSION_EXCLUDES_GROUPIDS                   = "excludeGroupIds";
    public final static String       EXTENSION_EXCLUDES_ARTIFACTIDS                = "excludeArtifactIds";

    public final static String       EXTENSION_INCLUDES                            = "includes";
    public final static String       EXTENSION_INCLUDES_GROUPIDS                   = "includeGroupIds";
    public final static String       EXTENSION_INCLUDES_ARTIFACTIDS                = "includeArtifactIds";

    public final static String       DECLARED_LIBRARIES_WHITELIST                  = "declared.libraries.whitelist";

    public static final List<String> CHANNEL_QUIT                                  = new ArrayList<>();

    static {
        CHANNEL_QUIT.add("quit");
        CHANNEL_QUIT.add("q");
        CHANNEL_QUIT.add("exit");
    }
}
