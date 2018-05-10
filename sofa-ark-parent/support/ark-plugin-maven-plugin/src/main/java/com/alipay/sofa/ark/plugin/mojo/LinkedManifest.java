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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author qilong.zql
 * @since 0.1.0
 */
public class LinkedManifest extends Manifest {

    private LinkedAttributes attr = new LinkedAttributes();

    @Override
    public Attributes getMainAttributes() {
        return attr;
    }

    @Override
    public void write(OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        // Write out the main attributes for the manifest
        attr.writeMain(dos);
        dos.flush();
    }

    /**
     * Adds line breaks to enforce a maximum 72 bytes per line.
     */
    public static void make72Safe(StringBuffer line) {
        int length = line.length();
        if (length > 72) {
            int index = 70;
            while (index < length - 2) {
                line.insert(index, "\r\n ");
                index += 72;
                length += 3;
            }
        }
    }

}