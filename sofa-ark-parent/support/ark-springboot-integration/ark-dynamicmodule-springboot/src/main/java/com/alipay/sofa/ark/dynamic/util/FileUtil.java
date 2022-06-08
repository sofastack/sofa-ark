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
package com.alipay.sofa.ark.dynamic.util;

import com.alipay.sofa.ark.common.util.EnvironmentUtils;

import java.io.File;
import java.net.URL;
import java.util.StringTokenizer;

/**
 * The type File util.
 *
 * @author hanyue
 * @version : FileUtils.java, v 0.1 2022年05月31日 1:13 PM hanyue Exp $
 */
public class FileUtil {

    /**
     * Gets path based on.
     *
     * @param basedir the basedir
     * @param path    the path
     * @return the path based on
     */
    public static String getPathBasedOn(String basedir, String path) {
        return getPathBasedOn(basedir, path, isOsWindows());
    }

    /**
     * To file file.
     *
     * @param url the url
     * @return the file
     */
    public static File toFile(URL url) {
        if (url == null) {
            return null;
        }

        if (url.getProtocol().equals("file")) {
            String path = url.getPath();

            if (path != null) {
                return new File(path);
            }
        }

        return null;
    }

    private static String getPathBasedOn(String basedir, String path, boolean isWindows) {
        if (path == null) {
            return null;
        } else {
            path = path.trim();
            path = path.replace('\\', '/');
            String prefix = getSystemDependentPrefix(path, isWindows);
            if (prefix == null) {
                return null;
            } else if (prefix.length() > 0 || path.length() > prefix.length()
                    && path.charAt(prefix.length()) == '/') {
                return normalizePath(path, isWindows);
            } else if (basedir == null) {
                return null;
            } else {
                StringBuffer buffer = new StringBuffer();
                buffer.append(basedir.trim());
                if (basedir.length() > 0 && path.length() > 0
                        && basedir.charAt(basedir.length() - 1) != '/') {
                    buffer.append('/');
                }

                buffer.append(path);
                return normalizePath(buffer.toString(), isWindows);
            }
        }
    }

    private static String normalizePath(String path, boolean isWindows) {
        if (path == null) {
            return null;
        } else {
            path = path.trim();
            path = path.replace('\\', '/');
            String prefix = getSystemDependentPrefix(path, isWindows);
            if (prefix == null) {
                return null;
            } else {
                path = path.substring(prefix.length());
                if (prefix.length() > 0 || path.startsWith("/")) {
                    prefix = prefix + '/';
                }

                boolean endsWithSlash = path.endsWith("/");
                StringTokenizer tokenizer = new StringTokenizer(path, "/");
                StringBuffer buffer = new StringBuffer(prefix.length() + path.length());
                int level = 0;
                buffer.append(prefix);

                while (true) {
                    while (true) {
                        while (true) {
                            String element;
                            do {
                                if (!tokenizer.hasMoreTokens()) {
                                    if (buffer.length() == 0) {
                                        buffer.append(".").append('/');
                                    }

                                    if (!endsWithSlash && buffer.length() > prefix.length()
                                            && buffer.charAt(buffer.length() - 1) == '/') {
                                        buffer.setLength(buffer.length() - 1);
                                    }

                                    return buffer.toString();
                                }

                                element = tokenizer.nextToken();
                            } while (".".equals(element));

                            if ("..".equals(element)) {
                                if (level == 0) {
                                    if (prefix.length() > 0) {
                                        return null;
                                    }

                                    buffer.append("..").append('/');
                                } else {
                                    --level;
                                    boolean found = false;

                                    for (int i = buffer.length() - 2; i >= prefix.length(); --i) {
                                        if (buffer.charAt(i) == '/') {
                                            buffer.setLength(i + 1);
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        buffer.setLength(prefix.length());
                                    }
                                }
                            } else {
                                buffer.append(element).append('/');
                                ++level;
                            }
                        }
                    }
                }
            }
        }
    }

    private static String getSystemDependentPrefix(String path, boolean isWindows) {
        if (isWindows) {
            if (path.startsWith("//")) {
                if (path.length() == "//".length()) {
                    return null;
                }

                int index = path.indexOf("/", "//".length());
                if (index != -1) {
                    return path.substring(0, index);
                }

                return path;
            }

            if (path.length() > 1 && path.charAt(1) == ':') {
                return path.substring(0, 2).toUpperCase();
            }
        }

        return "";
    }

    private static final boolean isOsWindows() {
        String osName = EnvironmentUtils.getProperty("os.name", null);
        if (osName == null) {
            return false;
        }

        return osName.startsWith("Windows");
    }
}