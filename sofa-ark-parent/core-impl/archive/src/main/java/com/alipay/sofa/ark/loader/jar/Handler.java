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

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.common.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link URLStreamHandler} for Spring Boot loader {@link JarFile}s.
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 * @see JarFile#registerUrlProtocolHandler()
 */
public class Handler extends URLStreamHandler {

    // NOTE: in order to be found as a URL protocol handler, this class must be public,
    // must be named Handler and must be in a package ending '.jar'

    private static final String                      JAR_PROTOCOL      = "jar:";

    private static final String                      FILE_PROTOCOL     = "file:";

    private static final String                      SEPARATOR         = "!/";

    private static final String[]                    FALLBACK_HANDLERS = { "sun.net.www.protocol.jar.Handler" };

    private static final Method                      OPEN_CONNECTION_METHOD;

    static {
        Method method = null;
        try {
            method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
        } catch (Exception ex) {
            // Swallow and ignore
        }
        OPEN_CONNECTION_METHOD = method;
    }

    private static SoftReference<Map<File, JarFile>> rootFileCache;

    static {
        rootFileCache = new SoftReference<>(null);
    }

    private final JarFile                            jarFile;

    private URLStreamHandler                         fallbackHandler;

    public Handler() {
        this(null);
    }

    public Handler(JarFile jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        if (this.jarFile != null) {
            return JarURLConnection.get(url, this.jarFile);
        }
        try {
            return JarURLConnection.get(url, getRootJarFileFromUrl(url));
        } catch (Exception ex) {
            return openFallbackConnection(url, ex);
        }
    }

    private URLConnection openFallbackConnection(URL url, Exception reason) throws IOException {
        try {
            return openConnection(getFallbackHandler(), url);
        } catch (Exception ex) {
            if (reason instanceof IOException) {
                log(false, ex);
                throw (IOException) reason;
            }
            log(true, ex);
            if (reason instanceof RuntimeException) {
                throw (RuntimeException) reason;
            }
            throw new IllegalStateException(reason);
        }
    }

    private void log(boolean warning, Exception cause) {
        try {
            Logger.getLogger(getClass().getName()).log((warning ? Level.WARNING : Level.FINEST),
                "Unable to open fallback handler", cause);
        } catch (Exception ex) {
            if (warning) {
                System.err.println("WARNING: " + "Unable to open fallback handler"); //NOPMD
            }
        }
    }

    private URLStreamHandler getFallbackHandler() {
        if (this.fallbackHandler != null) {
            return this.fallbackHandler;
        }
        for (String handlerClassName : FALLBACK_HANDLERS) {
            try {
                Class<?> handlerClass = Class.forName(handlerClassName);
                this.fallbackHandler = (URLStreamHandler) handlerClass.newInstance();
                return this.fallbackHandler;
            } catch (Exception ex) {
                // Ignore
            }
        }
        throw new IllegalStateException("Unable to find fallback handler");
    }

    private URLConnection openConnection(URLStreamHandler handler, URL url) throws Exception {
        if (OPEN_CONNECTION_METHOD == null) {
            throw new IllegalStateException("Unable to invoke fallback open connection method");
        }
        OPEN_CONNECTION_METHOD.setAccessible(true);
        return (URLConnection) OPEN_CONNECTION_METHOD.invoke(handler, url);
    }

    @Override
    protected void parseURL(URL context, String spec, int start, int limit) {
        if (StringUtils.startWithToLowerCase(spec, JAR_PROTOCOL)) {
            setFile(context, getFileFromSpec(spec.substring(start, limit)));
        } else {
            setFile(context, getFileFromContext(context, spec.substring(start, limit)));
        }
    }

    private String getFileFromSpec(String spec) {
        int separatorIndex = spec.lastIndexOf("!/");
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("No !/ in spec '" + spec + "'");
        }
        try {
            new URL(spec.substring(0, separatorIndex));
            return spec;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Invalid spec URL '" + spec + "'", ex);
        }
    }

    private String getFileFromContext(URL context, String spec) {
        String file = context.getFile();
        StringBuilder sb = new StringBuilder(file.length() + spec.length());
        if (spec.startsWith("/")) {
            return sb.append(trimToJarRoot(file)).append(SEPARATOR).append(spec.substring(1))
                .toString();
        }
        if (file.endsWith("/")) {
            return sb.append(file).append(spec).toString();
        }
        int lastSlashIndex = file.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("No / found in context URL's file '" + file + "'");
        }
        return sb.append(file.substring(0, lastSlashIndex + 1)).append(spec).toString();
    }

    private String trimToJarRoot(String file) {
        int lastSeparatorIndex = file.lastIndexOf(SEPARATOR);
        if (lastSeparatorIndex == -1) {
            throw new IllegalArgumentException("No !/ found in context URL's file '" + file + "'");
        }
        return file.substring(0, lastSeparatorIndex);
    }

    private void setFile(URL context, String file) {
        setURL(context, JAR_PROTOCOL, null, -1, null, null, normalize(file), null, null);
    }

    private String normalize(String file) {
        int afterLastSeparatorIndex = file.lastIndexOf(SEPARATOR) + SEPARATOR.length();
        String afterSeparator = file.substring(afterLastSeparatorIndex);
        afterSeparator = replaceParentDir(afterSeparator);
        afterSeparator = replaceCurrentDir(afterSeparator);
        return new StringBuilder(afterLastSeparatorIndex + afterSeparator.length())
            .append(file.substring(0, afterLastSeparatorIndex)).append(afterSeparator).toString();
    }

    String replaceParentDir(String file) {
        int parentDirIndex;
        while ((parentDirIndex = file.indexOf("/../")) >= 0) {
            int precedingSlashIndex = file.lastIndexOf('/', parentDirIndex - 1);
            if (precedingSlashIndex >= 0) {
                file = file.substring(0, precedingSlashIndex) + file.substring(parentDirIndex + 3);
            } else {
                file = file.substring(parentDirIndex + 4);
            }
        }
        return file;
    }

    private String replaceCurrentDir(String file) {
        return file.replace("/./", "/");
    }

    @Override
    protected int hashCode(URL u) {
        return hashCode(u.getProtocol(), u.getFile());
    }

    private int hashCode(String protocol, String file) {
        int result = (protocol == null ? 0 : protocol.hashCode());
        int separatorIndex = file.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            return result + file.hashCode();
        }
        String source = file.substring(0, separatorIndex);
        String entry = canonicalize(file.substring(separatorIndex + 2));
        try {
            result += new URL(source).hashCode();
        } catch (MalformedURLException ex) {
            result += source.hashCode();
        }
        result += entry.hashCode();
        return result;
    }

    @Override
    protected boolean sameFile(URL u1, URL u2) {
        if (!"jar".equals(u1.getProtocol()) || !"jar".equals(u2.getProtocol())) {
            return false;
        }
        int separator1 = u1.getFile().indexOf(SEPARATOR);
        int separator2 = u2.getFile().indexOf(SEPARATOR);
        if (separator1 == -1 || separator2 == -1) {
            return super.sameFile(u1, u2);
        }
        String nested1 = u1.getFile().substring(separator1 + SEPARATOR.length());
        String nested2 = u2.getFile().substring(separator2 + SEPARATOR.length());
        if (!nested1.equals(nested2)) {
            String canonical1 = canonicalize(nested1);
            String canonical2 = canonicalize(nested2);
            if (!canonical1.equals(canonical2)) {
                return false;
            }
        }
        String root1 = u1.getFile().substring(0, separator1);
        String root2 = u2.getFile().substring(0, separator2);
        try {
            return super.sameFile(new URL(root1), new URL(root2));
        } catch (MalformedURLException ex) {
            // Continue
        }
        return super.sameFile(u1, u2);
    }

    private String canonicalize(String path) {
        return path.replace(SEPARATOR, "/");
    }

    public JarFile getRootJarFileFromUrl(URL url) throws IOException {
        String spec = url.getFile();
        int separatorIndex = spec.indexOf(SEPARATOR);
        if (separatorIndex == -1) {
            throw new MalformedURLException("Jar URL does not contain !/ separator");
        }
        String name = spec.substring(0, separatorIndex);
        return getRootJarFile(name);
    }

    private JarFile getRootJarFile(String name) throws IOException {
        try {
            if (!name.startsWith(FILE_PROTOCOL)) {
                throw new IllegalStateException("Not a file URL");
            }
            String path = name.substring(FILE_PROTOCOL.length());
            File file = FileUtils.file(path);
            Map<File, JarFile> cache = rootFileCache.get();
            JarFile result = (cache == null ? null : cache.get(file));
            if (result == null) {
                result = new JarFile(file);
                addToRootFileCache(file, result);
            }
            return result;
        } catch (Exception ex) {
            throw new IOException("Unable to open root Jar file '" + name + "'", ex);
        }
    }

    /**
     * Add the given {@link JarFile} to the root file cache.
     * @param sourceFile the source file to add
     * @param jarFile the jar file.
     */
    static void addToRootFileCache(File sourceFile, JarFile jarFile) {
        Map<File, JarFile> cache = rootFileCache.get();
        if (cache == null) {
            cache = new ConcurrentHashMap<>(16);
            rootFileCache = new SoftReference<>(cache);
        }
        cache.put(sourceFile, jarFile);
    }

    /**
     * Set if a generic static exception can be thrown when a URL cannot be connected.
     * This optimization is used during class loading to save creating lots of exceptions
     * which are then swallowed.
     * @param useFastConnectionExceptions if fast connection exceptions can be used.
     */
    public static void setUseFastConnectionExceptions(boolean useFastConnectionExceptions) {
        JarURLConnection.setUseFastExceptions(useFastConnectionExceptions);
    }

}
