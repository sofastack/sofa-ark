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

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarInputStream;

/**
 * Writer to write classes into repackaged JAR.
 *
 * @author qilong.zql
 * @since 0.1.0
 */
public interface LoaderClassesWriter {

    /**
     * Write custom required SOFA-ark-loader classes to the JAR.
     * @param jarInputStream the inputStream of the resource containing the loader classes
     * to be written
     * @throws IOException if the classes cannot be written
     */
    void writeLoaderClasses(JarInputStream jarInputStream) throws IOException;

    /**
     * Write a single entry to the JAR.
     * @param name the name of the entry
     * @param inputStream the input stream content
     * @throws IOException if the entry cannot be written
     */
    void writeEntry(String name, InputStream inputStream) throws IOException;

}