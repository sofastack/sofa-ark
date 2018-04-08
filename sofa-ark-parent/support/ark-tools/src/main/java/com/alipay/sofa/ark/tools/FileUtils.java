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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilities for manipulating files and directories in ark tooling.
 *
 * @author Dave Syer
 * @author Phillip Webb
 */
public class FileUtils {

    /**
     * Generate a SHA.1 Hash for a given file.
     * @param file the file to hash
     * @return the hash value as a String
     * @throws IOException if the file cannot be read
     */
    public static String sha1Hash(File file) throws IOException {
        try {
            DigestInputStream inputStream = new DigestInputStream(new FileInputStream(file),
                MessageDigest.getInstance("SHA-1"));
            try {
                byte[] buffer = new byte[4098];
                while (inputStream.read(buffer) != -1) { //NOPMD
                    // Read the entire stream
                }
                return bytesToHex(inputStream.getMessageDigest().digest());
            } finally {
                inputStream.close();
            }
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

}