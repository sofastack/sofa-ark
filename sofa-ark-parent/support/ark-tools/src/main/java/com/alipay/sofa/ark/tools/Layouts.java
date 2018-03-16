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
package com.alipay.sofa.ark.tools;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class Layouts {

    private Layouts() {
    }

    /**
     * Executable JAR layout.
     */
    public static class Jar implements RepackagingLayout {

        @Override
        public String getArkContainerLocation() {
            return "SOFA-ARK/container/";
        }

        @Override
        public String getArkPluginLocation() {
            return "SOFA-ARK/plugin/";
        }

        @Override
        public String getArkModuleLocation() {
            return "SOFA-ARK/biz/";
        }

        @Override
        public String getLauncherClassName() {
            return "com.alipay.sofa.ark.bootstrap.ArkLauncher";
        }

        @Override
        public String getLibraryDestination(String libraryName, LibraryScope scope) {
            if (scope.equals(LibraryScope.PLUGIN)) {
                return getArkPluginLocation();
            } else if (scope.equals(LibraryScope.MODULE)) {
                return getArkModuleLocation();
            }
            return "";
        }

        @Override
        public boolean isExecutable() {
            return true;
        }

        public static Jar jar() {
            return new Jar();
        }
    }

    /**
     * Module layout (designed to be used as a "plug-in")
     */
    public static class Module implements Layout {

        @Override
        public String getLauncherClassName() {
            return "";
        }

        @Override
        public String getLibraryDestination(String libraryName, LibraryScope scope) {
            return "lib/";
        }

        @Override
        public boolean isExecutable() {
            return false;
        }

        public static Module module() {
            return new Module();
        }
    }

}