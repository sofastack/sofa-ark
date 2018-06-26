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
public interface LibraryScope {

    /**
     * The library is used at compile time and runtime
     */
    LibraryScope COMPILE  = new LibraryScope() {

                              @Override
                              public String toString() {
                                  return "compile";
                              }

                          };

    /**
     * The library is used at runtime but not needed for compile.
     */
    LibraryScope RUNTIME  = new LibraryScope() {

                              @Override
                              public String toString() {
                                  return "runtime";
                              }

                          };

    /**
     * The library is needed for compile but is usually provided when running.
     */
    LibraryScope PROVIDED = new LibraryScope() {

                              @Override
                              public String toString() {
                                  return "provided";
                              }

                          };

    /**
     * Marker for sofa-ark plugin scope when custom configuration is used.
     */
    LibraryScope PLUGIN   = new LibraryScope() {

                              @Override
                              public String toString() {
                                  return "plugin";
                              }

                          };

    /**
     * Marker for sofa-ark module scope when custom configuration is used.
     */
    LibraryScope MODULE   = new LibraryScope() {

                              @Override
                              public String toString() {
                                  return "module";
                              }

                          };

}