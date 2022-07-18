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

import com.alipay.sofa.ark.loader.data.RandomAccessData.ResourceAccess;

import java.io.*;
import java.net.*;
import java.security.Permission;

/**
 * {@link java.net.JarURLConnection} used to support {@link JarFile#getUrl()}.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @author Rostyslav Dudka
 */
final public class JarURLConnection extends java.net.JarURLConnection {

    private static ThreadLocal<Boolean>        useFastExceptions              = new ThreadLocal<>();

    private static final FileNotFoundException FILE_NOT_FOUND_EXCEPTION       = new FileNotFoundException(
                                                                                  "Jar file or entry not found");

    private static final IllegalStateException NOT_FOUND_CONNECTION_EXCEPTION = new IllegalStateException(
                                                                                  FILE_NOT_FOUND_EXCEPTION);

    private static final String                SEPARATOR                      = "!/";

    private static final URL                   EMPTY_JAR_URL;

    static {
        try {
            EMPTY_JAR_URL = new URL("jar:", null, 0, "file:!/", new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) {
                    // Stub URLStreamHandler to prevent the wrong JAR Handler from being
                    // Instantiated and cached.
                    return null;
                }
            });
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static final JarEntryName          EMPTY_JAR_ENTRY_NAME           = new JarEntryName("");

    private static final String                READ_ACTION                    = "read";

    private static final JarURLConnection      NOT_FOUND_CONNECTION           = JarURLConnection
                                                                                  .notFound();

    private final JarFile                      jarFile;

    private Permission                         permission;

    private URL                                jarFileUrl;

    private final JarEntryName                 jarEntryName;

    private JarEntry                           jarEntry;

    private JarURLConnection(URL url, JarFile jarFile, JarEntryName jarEntryName)
                                                                                 throws IOException {
        // What we pass to super is ultimately ignored
        super(EMPTY_JAR_URL);
        this.url = url;
        this.jarFile = jarFile;
        this.jarEntryName = jarEntryName;
    }

    @Override
    public void connect() throws IOException {
        if (this.jarFile == null) {
            throw FILE_NOT_FOUND_EXCEPTION;
        }
        if (!this.jarEntryName.isEmpty() && this.jarEntry == null) {
            this.jarEntry = this.jarFile.getJarEntry(getEntryName());
            if (this.jarEntry == null) {
                throwFileNotFound(this.jarEntryName, this.jarFile);
            }
        }
        this.connected = true;
    }

    @Override
    public JarFile getJarFile() throws IOException {
        connect();
        return this.jarFile;
    }

    @Override
    public URL getJarFileURL() {
        if (this.jarFile == null) {
            throw NOT_FOUND_CONNECTION_EXCEPTION;
        }
        if (this.jarFileUrl == null) {
            this.jarFileUrl = buildJarFileUrl();
        }
        return this.jarFileUrl;
    }

    private URL buildJarFileUrl() {
        try {
            String spec = this.jarFile.getUrl().getFile();
            if (spec.endsWith(SEPARATOR)) {
                spec = spec.substring(0, spec.length() - SEPARATOR.length());
            }
            if (!spec.contains(SEPARATOR)) {
                return new URL(spec);
            }
            return new URL("jar:" + spec);
        } catch (MalformedURLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public JarEntry getJarEntry() throws IOException {
        if (this.jarEntryName == null || this.jarEntryName.isEmpty()) {
            return null;
        }
        connect();
        return this.jarEntry;
    }

    @Override
    public String getEntryName() {
        if (this.jarFile == null) {
            throw NOT_FOUND_CONNECTION_EXCEPTION;
        }
        return this.jarEntryName.toString();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (this.jarFile == null) {
            throw FILE_NOT_FOUND_EXCEPTION;
        }
        if (this.jarEntryName.isEmpty() && this.jarFile.getType() == JarFile.JarFileType.DIRECT) {
            throw new IOException("no entry name specified");
        }
        connect();
        InputStream inputStream = (this.jarEntryName.isEmpty() ? this.jarFile.getData()
            .getInputStream(ResourceAccess.ONCE) : this.jarFile.getInputStream(this.jarEntry));
        if (inputStream == null) {
            throwFileNotFound(this.jarEntryName, this.jarFile);
        }
        return inputStream;
    }

    private void throwFileNotFound(Object entry, JarFile jarFile) throws FileNotFoundException {
        if (Boolean.TRUE.equals(useFastExceptions.get())) {
            throw FILE_NOT_FOUND_EXCEPTION;
        }
        throw new FileNotFoundException("JAR entry " + entry + " not found in " + jarFile.getName());
    }

    @Override
    public int getContentLength() {
        long length = getContentLengthLong();
        if (length > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) length;
    }

    @Override
    public long getContentLengthLong() {
        if (this.jarFile == null) {
            return -1;
        }
        try {
            if (this.jarEntryName.isEmpty()) {
                return this.jarFile.size();
            }
            JarEntry entry = getJarEntry();
            return (entry == null ? -1 : (int) entry.getSize());
        } catch (IOException ex) {
            return -1;
        }
    }

    @Override
    public Object getContent() throws IOException {
        connect();
        return (this.jarEntryName.isEmpty() ? this.jarFile : super.getContent());
    }

    @Override
    public String getContentType() {
        return (this.jarEntryName == null ? null : this.jarEntryName.getContentType());
    }

    @Override
    public Permission getPermission() throws IOException {
        if (this.jarFile == null) {
            throw FILE_NOT_FOUND_EXCEPTION;
        }
        if (this.permission == null) {
            this.permission = new FilePermission(this.jarFile.getRootJarFile().getFile().getPath(),
                READ_ACTION);
        }
        return this.permission;
    }

    @Override
    public long getLastModified() {
        if (this.jarFile == null || this.jarEntryName.isEmpty()) {
            return 0;
        }
        try {
            JarEntry entry = getJarEntry();
            return (entry == null ? 0 : entry.getTime());
        } catch (IOException ex) {
            return 0;
        }
    }

    static void setUseFastExceptions(boolean useFastExceptions) {
        JarURLConnection.useFastExceptions.set(useFastExceptions);
    }

    static JarURLConnection get(URL url, JarFile jarFile) throws IOException {
        String spec = extractFullSpec(url, jarFile.getPathFromRoot());
        int separator;
        int index = 0;
        while ((separator = spec.indexOf(SEPARATOR, index)) > 0) {
            String entryName = spec.substring(index, separator);
            JarEntry jarEntry = jarFile.getJarEntry(entryName);
            if (jarEntry == null) {
                return JarURLConnection.notFound(jarFile, JarEntryName.get(entryName));
            }
            jarFile = jarFile.getNestedJarFile(jarEntry);
            index = separator + SEPARATOR.length();
        }
        JarEntryName jarEntryName = JarEntryName.get(spec, index);
        if (Boolean.TRUE.equals(useFastExceptions.get())) {
            if (!jarEntryName.isEmpty() && !jarFile.containsEntry(jarEntryName.toString())) {
                return NOT_FOUND_CONNECTION;
            }
        }
        return new JarURLConnection(url, jarFile, jarEntryName);
    }

    private static String extractFullSpec(URL url, String pathFromRoot) {
        String file = url.getFile();
        int separatorIndex = file.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            return "";
        }
        int specIndex = separatorIndex + SEPARATOR.length() + pathFromRoot.length();
        return file.substring(specIndex);
    }

    private static JarURLConnection notFound() {
        try {
            return notFound(null, null);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static JarURLConnection notFound(JarFile jarFile, JarEntryName jarEntryName)
                                                                                        throws IOException {
        if (Boolean.TRUE.equals(useFastExceptions.get())) {
            return NOT_FOUND_CONNECTION;
        }
        return new JarURLConnection(null, jarFile, jarEntryName);
    }

    /**
     * A JarEntryName parsed from a URL String.
     */
    static class JarEntryName {

        private final String name;

        private String       contentType;

        JarEntryName(String spec) {
            this.name = decode(spec);
        }

        private String decode(String source) {
            if (source.indexOf('%') < 0) {
                return source;
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream(source.length());
            write(source, bos);
            // AsciiBytes is what is used to store the JarEntries so make it symmetric
            return AsciiBytes.toString(bos.toByteArray());
        }

        private void write(String source, ByteArrayOutputStream outputStream) {
            int length = source.length();
            for (int i = 0; i < length; i++) {
                int c = source.charAt(i);
                if (c > 127) {
                    try {
                        String encoded = URLEncoder.encode(String.valueOf((char) c), "UTF-8");
                        write(encoded, outputStream);
                    } catch (UnsupportedEncodingException ex) {
                        throw new IllegalStateException(ex);
                    }
                } else {
                    if (c == '%') {
                        if ((i + 2) >= length) {
                            throw new IllegalArgumentException("Invalid encoded sequence \""
                                                               + source.substring(i) + "\"");
                        }
                        c = decodeEscapeSequence(source, i);
                        i += 2;
                    }
                    outputStream.write(c);
                }
            }
        }

        private char decodeEscapeSequence(String source, int i) {
            int hi = Character.digit(source.charAt(i + 1), 16);
            int lo = Character.digit(source.charAt(i + 2), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException("Invalid encoded sequence \""
                                                   + source.substring(i) + "\"");
            }
            return ((char) ((hi << 4) + lo));
        }

        @Override
        public String toString() {
            return this.name;
        }

        public boolean isEmpty() {
            return this.name.isEmpty();
        }

        public String getContentType() {
            if (this.contentType == null) {
                this.contentType = deduceContentType();
            }
            return this.contentType;
        }

        private String deduceContentType() {
            // Guess the content type, don't bother with streams as mark is not supported
            String type = (isEmpty() ? "x-java/jar" : null);
            type = (type != null ? type : guessContentTypeFromName(toString()));
            type = (type != null ? type : "content/unknown");
            return type;
        }

        public static JarEntryName get(String spec) {
            return get(spec, 0);
        }

        public static JarEntryName get(String spec, int beginIndex) {
            if (spec.length() <= beginIndex) {
                return EMPTY_JAR_ENTRY_NAME;
            }
            return new JarEntryName(spec.substring(beginIndex));
        }

    }

}
