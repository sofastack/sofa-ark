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
package com.alipay.sofa.ark.loader.jar;

import java.lang.reflect.Method;

/**
 * Helper class to access private methods in JarUtils for testing purposes.
 */
public class JarUtilsTestHelper {

    /**
     * Call the private parseArtifactIdFromUnpackedDir method for testing.
     * 
     * @param unpackDirPath the path to the unpacked directory
     * @return the parsed artifactId or null if not found
     */
    public static String parseArtifactIdFromUnpackedDir(String unpackDirPath) {
        try {
            Method method = JarUtils.class.getDeclaredMethod("parseArtifactIdFromUnpackedDir",
                String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, unpackDirPath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke parseArtifactIdFromUnpackedDir", e);
        }
    }
}