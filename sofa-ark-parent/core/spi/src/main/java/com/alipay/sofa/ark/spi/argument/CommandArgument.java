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
package com.alipay.sofa.ark.spi.argument;

/**
 * SOFAArk command-line arguments
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface CommandArgument {

    /**
     * command-line arguments received by ark container.
     * pattern: -A[key]=[value]
     */
    String ARK_CONTAINER_ARGUMENTS_MARK          = "-A";

    String CLASSPATH_ARGUMENT_KEY                = "classpath";

    String FAT_JAR_ARGUMENT_KEY                  = "jar";

    String CLASSPATH_SPLIT                       = ",";

    String KEY_VALUE_PAIR_SPLIT                  = "&&";

    /**
     * command-line arguments received by ark biz when execute in IDE.
     * pattern: -B[key]=[value]
     */
    String ARK_BIZ_ARGUMENTS_MARK                = "-B";

    String ENTRY_CLASS_NAME_ARGUMENT_KEY         = "className";

    String ENTRY_METHOD_NAME_ARGUMENT_KEY        = "methodName";

    String ENTRY_METHOD_DESCRIPTION_ARGUMENT_KEY = "methodDescription";

    /**
     * run mode in which ark container startup. {@literal MAIN} or {@literal TEST}.
     * pattern: -B[runMode]=[MAIN|TEST]
     */
    String BIZ_RUN_MODE                          = "runMode";

    String TEST_RUN_MODE                         = "TEST";

}